package org.una.programmingIII.UTEMP_Project.configs.security.filters;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.una.programmingIII.UTEMP_Project.models.User;
import org.una.programmingIII.UTEMP_Project.models.UserPermission;
import org.una.programmingIII.UTEMP_Project.security.filters.JwtRequestFilter;
import org.una.programmingIII.UTEMP_Project.services.jwtTokenProvider.JwtTokenProviderService;
import org.una.programmingIII.UTEMP_Project.services.user.CustomUserDetails;
import org.una.programmingIII.UTEMP_Project.services.user.CustomUserDetailsService;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtRequestFilterTest {

    @InjectMocks
    private JwtRequestFilter jwtRequestFilter;

    @Mock
    private JwtTokenProviderService jwtTokenProviderService;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private String validToken = "validToken";
    private String identificationNumber = "000000000";
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setIdentificationNumber(identificationNumber);
        testUser.setPermissions(new ArrayList<>(List.of(UserPermission.MANAGE_USERS)));
    }

    @Test
    void doFilterInternal_ValidToken() throws Exception {
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + validToken);
        when(jwtTokenProviderService.getIdentificationNumberFromToken(validToken)).thenReturn(identificationNumber);
        when(jwtTokenProviderService.validateToken(validToken, identificationNumber)).thenReturn(true);
        when(userDetailsService.loadUserByUsername(identificationNumber)).thenReturn(new CustomUserDetails(testUser));

        jwtRequestFilter.doFilterInternal(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertEquals(identificationNumber, ((CustomUserDetails) authentication.getPrincipal()).getUsername());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_TokenMissing() throws Exception {
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(null);

        jwtRequestFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_InvalidToken() throws Exception {
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + validToken);
        when(jwtTokenProviderService.getIdentificationNumberFromToken(validToken)).thenReturn(identificationNumber);
        when(jwtTokenProviderService.validateToken(validToken, identificationNumber)).thenReturn(false);

        jwtRequestFilter.doFilterInternal(request, response, filterChain);

        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT Token");
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_ExpiredToken() throws Exception {
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + validToken);
        when(jwtTokenProviderService.getIdentificationNumberFromToken(validToken)).thenReturn(identificationNumber);
        when(jwtTokenProviderService.validateToken(validToken, identificationNumber)).thenThrow(new ExpiredJwtException(null, null, "Token has expired"));

        jwtRequestFilter.doFilterInternal(request, response, filterChain);

        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token has expired");
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_InvalidSignature() throws Exception {
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + validToken);
        when(jwtTokenProviderService.getIdentificationNumberFromToken(validToken)).thenReturn(identificationNumber);
        when(jwtTokenProviderService.validateToken(validToken, identificationNumber)).thenThrow(new SignatureException("Invalid JWT signature"));

        jwtRequestFilter.doFilterInternal(request, response, filterChain);

        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT signature");
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}

