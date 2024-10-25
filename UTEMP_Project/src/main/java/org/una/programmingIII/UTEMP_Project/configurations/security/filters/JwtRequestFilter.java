package org.una.programmingIII.UTEMP_Project.configurations.security.filters;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.una.programmingIII.UTEMP_Project.services.jwtTokenProvider.JwtTokenProviderService;
import org.una.programmingIII.UTEMP_Project.services.user.CustomUserDetails;
import org.una.programmingIII.UTEMP_Project.services.user.CustomUserDetailsService;

import java.io.IOException;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtRequestFilter.class);

    private final JwtTokenProviderService jwtTokenProviderService;
    private final CustomUserDetailsService userDetailsService;

    @Autowired
    public JwtRequestFilter(JwtTokenProviderService jwtTokenProviderService, CustomUserDetailsService userDetailsService) {
        this.jwtTokenProviderService = jwtTokenProviderService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String token = extractToken(request);
        if (token == null) {
            logger.warn("Authorization token is missing");
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String identificationNumber = jwtTokenProviderService.getIdentificationNumberFromToken(token);
            if (identificationNumber != null && jwtTokenProviderService.validateToken(token, identificationNumber)) {
                authenticateUser(identificationNumber);
            } else {
                handleException(response, "Invalid JWT Token");
                return;
            }
        } catch (ExpiredJwtException e) {
            handleException(response, "Token has expired", e);
            return;
        } catch (SignatureException e) {
            handleException(response, "Invalid JWT signature", e);
            return;
        } catch (Exception e) {
            handleException(response, "Authentication failed", e);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void authenticateUser(String identificationNumber) {
        CustomUserDetails customUserDetails = (CustomUserDetails) userDetailsService.loadUserByUsername(identificationNumber);
        Authentication authentication = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        logger.info("User {} authenticated successfully", identificationNumber);
    }

    private String extractToken(HttpServletRequest request) {
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        return null;
    }

    private void handleException(HttpServletResponse response, String message) throws IOException {
        handleException(response, message, null);
    }

    private void handleException(HttpServletResponse response, String message, Exception e) throws IOException {
        if (!response.isCommitted()) {
            logger.error("{}: {}", message, e != null ? e.getMessage() : "");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, message);
        }
    }
}