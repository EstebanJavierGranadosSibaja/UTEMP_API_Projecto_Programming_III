package org.una.programmingIII.UTEMP_Project.utilities;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.una.programmingIII.UTEMP_Project.dtos.TokenDTOs.TokenResponseDTO;
import org.una.programmingIII.UTEMP_Project.exceptions.TokenExpiredException;
import org.una.programmingIII.UTEMP_Project.exceptions.InvalidTokenException;

import java.util.Date;

@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration.access}")
    private long accessTokenValidity;

    @Value("${jwt.expiration.refresh}")
    private long refreshTokenValidity;

    public String generateToken(String username, boolean isRefreshToken) {
        long validityDuration = isRefreshToken ? refreshTokenValidity : accessTokenValidity;
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + validityDuration))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    public String generateAccessToken(String username) {
        return generateToken(username, false);
    }

    public String generateRefreshToken(String username) {
        return generateToken(username, true);
    }

    public String getUsernameFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(secretKey)
                    .setAllowedClockSkewSeconds(60)
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getSubject();
        } catch (ExpiredJwtException e) {
            logger.error("Token has expired: ", e);
            throw new TokenExpiredException("The token has expired.");
        } catch (Exception e) {
            logger.error("Error while parsing JWT token: ", e);
            throw new InvalidTokenException("Invalid token.");
        }
    }

    public boolean validateToken(String token, String username) {
        final String tokenUsername = getUsernameFromToken(token);
        return (username.equals(tokenUsername) && !isTokenExpired(token));
    }

    public boolean isTokenExpired(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(secretKey)
                    .setAllowedClockSkewSeconds(60)
                    .parseClaimsJws(token)
                    .getBody();
            Date expiration = claims.getExpiration();
            return expiration.before(new Date());
        } catch (ExpiredJwtException e) {
            logger.error("Token has expired: ", e);
            return true;
        } catch (Exception e) {
            logger.error("Error while checking token expiration: ", e);
            throw new InvalidTokenException("Invalid token.");
        }
    }

    public TokenResponseDTO refreshTokens(String refreshToken) {
        Date now = new Date();
        if (isTokenExpired(refreshToken)) {
            logger.error("Refresh token expired. Current date: {}. Token expiration date: {}", now, getExpirationDate(refreshToken));
            throw new TokenExpiredException("Refresh token has expired.");
        }

        String username = getUsernameFromToken(refreshToken);
        if (username == null) {
            throw new InvalidTokenException("Invalid refresh token.");
        }

        String newAccessToken = generateAccessToken(username);
        String newRefreshToken = generateRefreshToken(username);

        logger.info("Tokens refreshed for user: {} at {}", username, now);
        return new TokenResponseDTO(newAccessToken, newRefreshToken);
    }

    private Date getExpirationDate(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody();
        return claims.getExpiration();
    }
}