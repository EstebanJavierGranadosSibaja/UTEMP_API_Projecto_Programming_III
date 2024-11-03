package org.una.programmingIII.UTEMP_Project.security.utils.jwtTokenProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.una.programmingIII.UTEMP_Project.dtos.TokenDTOs.TokenResponseDTO;
import org.una.programmingIII.UTEMP_Project.exceptions.CustomServiceException;
import org.una.programmingIII.UTEMP_Project.models.User;
import org.una.programmingIII.UTEMP_Project.repositories.UserRepository;

import java.util.Objects;

@Service
public class JwtTokenProviderImplementation implements JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProviderImplementation.class);

    private final org.una.programmingIII.UTEMP_Project.security.utils.JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Autowired
    public JwtTokenProviderImplementation(
            org.una.programmingIII.UTEMP_Project.security.utils.JwtTokenProvider jwtTokenProvider,
            UserRepository userRepository) {

        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
    }

    @Override
    public String generateAccessToken(String identificationNumber) {
        User user = getUserByIdentificationNumber(identificationNumber);
        logger.info("Generating access token for user: {}", identificationNumber);
        return jwtTokenProvider.generateAccessToken(user);
    }

    @Override
    public String generateRefreshToken(String identificationNumber) {
        User user = getUserByIdentificationNumber(identificationNumber);
        return jwtTokenProvider.generateRefreshToken(user);
    }

    @Override
    public boolean isTokenExpired(String token) {
        Objects.requireNonNull(token, "Token must not be null");
        return jwtTokenProvider.isTokenExpired(token);
    }

    @Override
    public boolean validateToken(String token, String identificationNumber) {
        Objects.requireNonNull(token, "Token must not be null");
        Objects.requireNonNull(identificationNumber, "Identification number must not be null");
        return jwtTokenProvider.validateToken(token, identificationNumber);
    }

    @Override
    public TokenResponseDTO refreshTokens(String refreshToken) {
        Objects.requireNonNull(refreshToken, "Refresh token must not be null");
        return jwtTokenProvider.refreshTokens(refreshToken);
    }

    @Override
    public Long getIdFromToken(String token) {
        Objects.requireNonNull(token, "Token must not be null");
        return jwtTokenProvider.getIdFromToken(token);
    }

    @Override
    public String getIdentificationNumberFromToken(String token) {
        Objects.requireNonNull(token, "Token must not be null");
        return jwtTokenProvider.getIdentificationNumberFromToken(token);
    }

    public String getUsernameFromToken(String token) {
        Objects.requireNonNull(token, "Token must not be null");
        return jwtTokenProvider.getNameFromToken(token);
    }

    private User getUserByIdentificationNumber(String identificationNumber) {
        Objects.requireNonNull(identificationNumber, "Identification number must not be null");
        User user = userRepository.findByIdentificationNumber(identificationNumber);
        if (user == null) {
            logger.error("User not found for identification number: {}", identificationNumber);
            throw new CustomServiceException("User not found for identification number: " + identificationNumber);
        }
        return user;
    }
}
