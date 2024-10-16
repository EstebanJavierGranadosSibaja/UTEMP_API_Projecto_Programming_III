package org.una.programmingIII.UTEMP_Project.filters;

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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.una.programmingIII.UTEMP_Project.exceptions.InvalidDataException;
import org.una.programmingIII.UTEMP_Project.services.JwtServices.JwtService;
import org.una.programmingIII.UTEMP_Project.utilities.JwtUtil;

import java.io.IOException;
import java.util.Optional;

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
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authorizationHeader == null) {
            logger.warn("Authorization header is missing");
            filterChain.doFilter(request, response);
            return;
        }

        if (!authorizationHeader.startsWith("Bearer ")) {
            logger.warn("Authorization header is malformed");
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorizationHeader.substring(7);

        if (token.isEmpty()) {
            logger.warn("Authorization token is empty");
            throw new InvalidDataException("Empty JWT token");
        }

        try {
            Optional<String> usernameOpt = Optional.ofNullable(jwtService.getUsernameFromToken(token));

            if (usernameOpt.isPresent() && jwtService.validateToken(token, usernameOpt.get())) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(usernameOpt.get());
                Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                logger.error("Invalid JWT Token");
                throw new InvalidDataException("Invalid JWT Token");
            }
        } catch (ExpiredJwtException e) {
            logger.error("Token has expired: {}", e.getMessage());
            throw new InvalidDataException("Token has expired");
        } catch (SignatureException e) {
            logger.error("Invalid JWT signature: {}", e.getMessage());
            throw new InvalidDataException("Invalid JWT signature");
        } catch (InvalidDataException e) {
            logger.error("Invalid data error: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Authentication error: {}", e.getMessage());
            throw new InvalidDataException("Authentication failed");
        }

        filterChain.doFilter(request, response);
    }
}