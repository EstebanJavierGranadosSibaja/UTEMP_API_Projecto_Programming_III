package org.una.programmingIII.UTEMP_Project.services.JwtServices;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.una.programmingIII.UTEMP_Project.dtos.TokenDTOs.TokenResponseDTO;
import org.una.programmingIII.UTEMP_Project.exceptions.CustomServiceException;
import org.una.programmingIII.UTEMP_Project.models.User;
import org.una.programmingIII.UTEMP_Project.repositories.UserRepository;
import org.una.programmingIII.UTEMP_Project.security.utils.JwtUtil;

import java.util.Objects;

@Service
public class JwtServiceImplementation implements JwtService {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Autowired
    public JwtServiceImplementation(JwtUtil jwtUtil, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    private static final Logger logger = LoggerFactory.getLogger(JwtServiceImplementation.class);

    public String generateAccessToken(String identificationNumber) {
        Objects.requireNonNull(identificationNumber, "Identification number must not be null");
        User user = userRepository.findByIdentificationNumber(identificationNumber);
        if (user == null) {
            logger.error("User not found for identification number: {}", identificationNumber);
            throw new CustomServiceException("User not found for identification number: " + identificationNumber);
        }
        logger.info("Generating access token for user: {}", identificationNumber);
        return jwtUtil.generateAccessToken(user);
    }

    public String generateRefreshToken(String identificationNumber) {
        Objects.requireNonNull(identificationNumber, "Identification number must not be null");
        User user = userRepository.findByIdentificationNumber(identificationNumber);
        if (user == null) {
            throw new CustomServiceException("User not found for identification number: " + identificationNumber);
        }
        return jwtUtil.generateRefreshToken(user);
    }

    public boolean isTokenExpired(String token) {
        Objects.requireNonNull(token, "Token must not be null");
        return jwtUtil.isTokenExpired(token);
    }

    public boolean validateToken(String token, String identificationNumber) {
        Objects.requireNonNull(token, "Token must not be null");
        Objects.requireNonNull(identificationNumber, "Identification number must not be null");
        return jwtUtil.validateToken(token, identificationNumber);
    }

    public TokenResponseDTO refreshTokens(String refreshToken) {
        Objects.requireNonNull(refreshToken, "Refresh token must not be null");
        return jwtUtil.refreshTokens(refreshToken);
    }

    @Override
    public String getIdFromToken(String token) {
        Objects.requireNonNull(token, "Token must not be null");
        return jwtUtil.getIdFromToken(token);
    }

    public String getUsernameFromToken(String token) {
        Objects.requireNonNull(token, "Token must not be null");
        return jwtUtil.getNameFromToken(token);
    }
}