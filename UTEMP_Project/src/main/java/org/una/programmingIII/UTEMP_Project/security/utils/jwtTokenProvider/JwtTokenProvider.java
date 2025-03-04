package org.una.programmingIII.UTEMP_Project.security.utils.jwtTokenProvider;

import org.una.programmingIII.UTEMP_Project.dtos.TokenDTOs.TokenResponseDTO;

public interface JwtTokenProvider {
    String generateAccessToken(String identificationNumber);

    String generateRefreshToken(String identificationNumber);

    boolean isTokenExpired(String token);

    boolean validateToken(String token, String identificationNumber);

    TokenResponseDTO refreshTokens(String refreshToken);

    Long getIdFromToken(String token);

    String getIdentificationNumberFromToken(String token);
}
