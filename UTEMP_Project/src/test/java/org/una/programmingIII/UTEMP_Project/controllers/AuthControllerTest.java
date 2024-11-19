package org.una.programmingIII.UTEMP_Project.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.una.programmingIII.UTEMP_Project.controllers.request.AuthRequest;
import org.una.programmingIII.UTEMP_Project.controllers.responses.ApiResponse;
import org.una.programmingIII.UTEMP_Project.controllers.responses.TokenResponse;
import org.una.programmingIII.UTEMP_Project.security.utils.JwtTokenProvider;
import org.una.programmingIII.UTEMP_Project.dtos.UserDTO;
import org.una.programmingIII.UTEMP_Project.services.CustomUserDetails;
import org.una.programmingIII.UTEMP_Project.services.CustomUserDetailsService;
import org.una.programmingIII.UTEMP_Project.services.UserService;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class AuthControllerTest {

    @InjectMocks
    private AuthController authController;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private UserService userService;

    private AuthRequest authRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        authRequest = new AuthRequest();
        authRequest.setIdentificationNumber("12345");
        authRequest.setPassword("password");
    }

    @Test
    void testCreateAuthenticationToken_Success() {
        // Mocking authentication manager
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);

        // Mocking user details and JWT generation
        CustomUserDetails customUserDetails = mock(CustomUserDetails.class);
        when(userDetailsService.loadUserByUsername(authRequest.getIdentificationNumber()))
                .thenReturn(customUserDetails);

        String jwt = "dummy-jwt-token";
        when(jwtTokenProvider.generateAccessToken(any())).thenReturn(jwt);

        // Mocking user retrieval by id
        UserDTO userDTO = mock(UserDTO.class);
        when(userService.getUserById(anyLong())).thenReturn(Optional.of(userDTO));

        // Call the method
        ResponseEntity<ApiResponse<TokenResponse>> response = authController.createAuthenticationToken(authRequest);

        // Assertions
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getData());
        assertEquals(jwt, response.getBody().getData().getToken());
        assertEquals("Bearer", response.getBody().getData().getTokenType());
        assertEquals(userDTO, response.getBody().getData().getUser());

        // Verifying interactions
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userDetailsService).loadUserByUsername(authRequest.getIdentificationNumber());
        verify(jwtTokenProvider).generateAccessToken(any());
        verify(userService).getUserById(anyLong());
    }

    @Test
    void testCreateAuthenticationToken_InvalidCredentials() {
        // Mocking authentication failure
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new RuntimeException("Invalid credentials"));

        // Call the method
        ResponseEntity<ApiResponse<TokenResponse>> response = authController.createAuthenticationToken(authRequest);

        // Assertions
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Invalid credentials", response.getBody().getMessage());

        // Verifying interactions
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userDetailsService, never()).loadUserByUsername(any());
        verify(jwtTokenProvider, never()).generateAccessToken(any());
        verify(userService, never()).getUserById(anyLong());
    }

    @Test
    void testCreateAuthenticationToken_UserNotFound() {
        // Mocking successful authentication
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);

        // Mocking user details and JWT generation
        CustomUserDetails customUserDetails = mock(CustomUserDetails.class);
        when(userDetailsService.loadUserByUsername(authRequest.getIdentificationNumber()))
                .thenReturn(customUserDetails);

        String jwt = "dummy-jwt-token";
        when(jwtTokenProvider.generateAccessToken(any())).thenReturn(jwt);

        // Mocking user not found scenario
        when(userService.getUserById(anyLong())).thenReturn(Optional.empty());

        // Call the method
        ResponseEntity<ApiResponse<TokenResponse>> response = authController.createAuthenticationToken(authRequest);

        // Assertions
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("User not found", response.getBody().getMessage());

        // Verifying interactions
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userDetailsService).loadUserByUsername(authRequest.getIdentificationNumber());
        verify(jwtTokenProvider).generateAccessToken(any());
        verify(userService).getUserById(anyLong());
    }
}
