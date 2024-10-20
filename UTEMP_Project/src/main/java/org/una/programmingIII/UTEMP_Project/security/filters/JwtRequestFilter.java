package org.una.programmingIII.UTEMP_Project.security.filters;

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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.una.programmingIII.UTEMP_Project.exceptions.InvalidDataException;
import org.una.programmingIII.UTEMP_Project.services.JwtServices.JwtService;

import java.io.IOException;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtRequestFilter.class);

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Autowired
    public JwtRequestFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
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
            String identificationNumber = jwtService.getIdFromToken(token);
            if (identificationNumber != null && jwtService.validateToken(token, identificationNumber)) {
                authenticateUser(identificationNumber);
            } else {
                throw new InvalidDataException("Invalid JWT Token");
            }
        } catch (ExpiredJwtException e) {
            handleException(response, "Token has expired", e);
        } catch (SignatureException e) {
            handleException(response, "Invalid JWT signature", e);
        } catch (Exception e) {
            handleException(response, "Authentication failed", e);
        }

        filterChain.doFilter(request, response);
    }

    private void authenticateUser(String identificationNumber) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(identificationNumber);
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }


    private String extractToken(HttpServletRequest request) {
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        return null;
    }

    private void handleException(HttpServletResponse response, String message, Exception e) {
        logger.error("{}: {}", message, e.getMessage());
        // Configura el código de estado HTTP apropiado
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        throw new InvalidDataException(message);
    }
}