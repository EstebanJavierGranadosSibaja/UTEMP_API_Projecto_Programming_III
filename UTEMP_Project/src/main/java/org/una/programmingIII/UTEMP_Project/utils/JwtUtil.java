package org.una.programmingIII.UTEMP_Project.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
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

@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);
    private final ObjectMapper objectMapper = new ObjectMapper(); // Para la serialización/deserialización

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration.access}")
    private long accessTokenValidity;

    @Value("${jwt.expiration.refresh}")
    private long refreshTokenValidity;

    private final UserRepository userRepository;

    @Autowired
    public JwtUtil(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String generateToken(User user, boolean isRefreshToken) {
        long validityDuration = isRefreshToken ? refreshTokenValidity : accessTokenValidity;
        return Jwts.builder()
                .setSubject(user.getIdentificationNumber())
                .claim("id", user.getId())
                .claim("name", user.getName())
                .claim("email", user.getEmail())
                .claim("permissions", user.getPermissions()) // Asegúrate de que se pueda serializar correctamente
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + validityDuration))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    public String generateAccessToken(User user) {
        return generateToken(user, false);
    }

    public String generateRefreshToken(User user) {
        return generateToken(user, true);
    }

    public String getIdFromToken(String token) {
        return parseClaims(token, Claims::getId);
    }

    public String getIdentificationNumberFromToken(String token) {
        return parseClaims(token, Claims::getSubject);
    }

    public String getNameFromToken(String token) {
        return parseClaims(token, claims -> claims.get("name", String.class));
    }

    public String getEmailFromToken(String token) {
        return parseClaims(token, claims -> claims.get("email", String.class));
    }

    public List<UserPermission> getPermissionsFromToken(String token) {
        return parseClaims(token, claims -> {
            try {
                return objectMapper.readValue(claims.get("permissions", String.class), new TypeReference<>() {
                });
            } catch (Exception e) {
                logger.error("Error while deserializing permissions: ", e);
                throw new InvalidTokenException("Invalid permissions in token.");
            }
        });
    }

    public boolean validateToken(String token, String identificationNumber) {
        final String tokenIdentificationNumber = getIdentificationNumberFromToken(token);
        return identificationNumber.equals(tokenIdentificationNumber) && !isTokenExpired(token);
    }

    public boolean isTokenExpired(String token) {
        Date expiration = parseClaims(token, Claims::getExpiration);
        return expiration.before(new Date());
    }

    public TokenResponseDTO refreshTokens(String refreshToken) {
        if (isTokenExpired(refreshToken)) {
            logger.error("Refresh token expired.");
            throw new TokenExpiredException("Refresh token has expired.");
        }

        //TODO obtener el valor del token
        String id = getIdFromToken(refreshToken);
        String userName = getNameFromToken(refreshToken);
        if (id == null) {
            throw new InvalidTokenException("Invalid refresh token.");
        }

        // Aquí deberías obtener el usuario correspondiente al identificationNumber
        User user = userRepository.findByIdentificationNumber(id);
        if (user == null) {
            throw new InvalidTokenException("User not found for the given identification number.");
        }

        String newAccessToken = generateAccessToken(user);
        String newRefreshToken = generateRefreshToken(user);

        // TODO mensaje de error
        logger.info("Tokens refreshed for user: {}", userName);
        return new TokenResponseDTO(newAccessToken, newRefreshToken);
    }

    private <T> T parseClaims(String token, java.util.function.Function<Claims, T> claimsResolver) {
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
        } catch (Exception e) {
            logger.error("Error while parsing JWT token: ", e);
            throw new InvalidTokenException("Invalid token.");
        }
    }
}
