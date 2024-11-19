package org.una.programmingIII.UTEMP_Project.security.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.*;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.una.programmingIII.UTEMP_Project.dtos.TokenDTOs.TokenResponseDTO;
import org.una.programmingIII.UTEMP_Project.exceptions.InvalidTokenException;
import org.una.programmingIII.UTEMP_Project.exceptions.TokenExpiredException;
import org.una.programmingIII.UTEMP_Project.models.User;
import org.una.programmingIII.UTEMP_Project.models.UserPermission;
import org.una.programmingIII.UTEMP_Project.repositories.UserRepository;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Setter
@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final UserRepository userRepository;
    @Value("${jwt.secret}")
    private String secretKey;
    //@Value("${jwt.expiration.access}")
    private long accessTokenValidity = 999999999999999999L;
    //@Value("${jwt.expiration.refresh}")
    private long refreshTokenValidity = 999999999999999999L;;

    @Autowired
    public JwtTokenProvider(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String generateToken(User user, boolean isRefreshToken) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        long validityDuration = isRefreshToken ? refreshTokenValidity : accessTokenValidity;
        return Jwts.builder()
                .setSubject(user.getIdentificationNumber())
                .claim("id", user.getId())
                .claim("name", user.getName())
                .claim("email", user.getEmail())
                .claim("permissions", serializePermissions(user.getPermissions()))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + validityDuration))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    private String serializePermissions(List<UserPermission> permissions) {
        return handleJsonProcessing(() -> objectMapper.writeValueAsString(permissions), "Error serializing permissions.");
    }

    private <T> T handleJsonProcessing(JsonProcessingFunction<T> function, String errorMessage) {
        try {
            return function.apply();
        } catch (JsonProcessingException e) {
            logger.error(errorMessage, e);
            throw new InvalidTokenException(errorMessage);
        }
    }

    public String getIdentificationNumberFromToken(String token) {
        return parseClaims(token, Claims::getSubject);
    }

    public Long getIdFromToken(String token) {
        return parseClaims(token, claims -> claims.get("id", Long.class)); // Cambiado a Long
    }

    public String getNameFromToken(String token) {
        return parseClaims(token, claims -> claims.get("name", String.class));
    }

    public String getEmailFromToken(String token) {
        return parseClaims(token, claims -> claims.get("email", String.class));
    }

    public List<UserPermission> getPermissionsFromToken(String token) {
        return parseClaims(token, claims -> handleJsonProcessing(
                () -> objectMapper.readValue(claims.get("permissions", String.class), new TypeReference<List<UserPermission>>() {
                }),
                "Invalid permissions in token."
        ));
    }

    public boolean validateToken(String token, String identificationNumber) {
        if (token == null || identificationNumber == null) {
            throw new IllegalArgumentException("Token and identification number cannot be null");
        }
        String tokenIdentificationNumber = getIdentificationNumberFromToken(token);
        boolean isValid = identificationNumber.equals(tokenIdentificationNumber) && !isTokenExpired(token);
        logger.info("Validating token for identification number {}: {}", identificationNumber, isValid);
        return isValid;
    }

    public boolean isTokenExpired(String token) {
        return parseClaims(token, Claims::getExpiration).before(new Date());
    }

    private <T> T parseClaims(String token, Function<Claims, T> claimsResolver) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(secretKey)
                    .setAllowedClockSkewSeconds(60)
                    .parseClaimsJws(token)
                    .getBody();
            return claimsResolver.apply(claims);
        } catch (ExpiredJwtException e) {
            logger.error("Token has expired: ", e);
            throw new TokenExpiredException("The token has expired.");
        } catch (JwtException | IllegalArgumentException e) {
            logger.error("Error while parsing JWT token: ", e);
            throw new InvalidTokenException("Invalid token.");
        }
    }

    public TokenResponseDTO refreshTokens(String refreshToken) {
        validateRefreshToken(refreshToken);

        Long id = getIdFromToken(refreshToken); // Asegúrate de que el ID se extraiga como Long

        Optional<User> user = userRepository.findById(id);
        if (user.isEmpty()) {
            throw new InvalidTokenException("User not found for the given ID: " + id);
        }

        String newAccessToken = generateAccessToken(user.get());
        String newRefreshToken = generateRefreshToken(user.get());

        logger.info("Tokens refreshed for user: {}", getNameFromToken(refreshToken));
        return new TokenResponseDTO(newAccessToken, newRefreshToken);
    }

    private void validateRefreshToken(String refreshToken) {
        if (isTokenExpired(refreshToken)) {
            logger.error("Refresh token expired.");
            throw new TokenExpiredException("Refresh token has expired.");
        }

        if (getIdFromToken(refreshToken) == null) {
            throw new InvalidTokenException("Invalid refresh token.");
        }
    }

    public String generateAccessToken(User user) {
        return generateToken(user, false);
    }

    public String generateRefreshToken(User user) {
        return generateToken(user, true);
    }

    @FunctionalInterface
    private interface JsonProcessingFunction<T> {
        T apply() throws JsonProcessingException;
    }
}
