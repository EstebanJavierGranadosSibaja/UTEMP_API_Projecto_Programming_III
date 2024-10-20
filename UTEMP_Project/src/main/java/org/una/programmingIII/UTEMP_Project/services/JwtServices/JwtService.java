package org.una.programmingIII.UTEMP_Project.services.JwtServices;

import org.una.programmingIII.UTEMP_Project.dtos.TokenDTOs.TokenResponseDTO;

public interface JwtService {
    String generateAccessToken(String identificationNumber);

    String generateRefreshToken(String identificationNumber);

    boolean isTokenExpired(String token);

    boolean validateToken(String token, String identificationNumber);

    TokenResponseDTO refreshTokens(String refreshToken);

    String getIdFromToken(String token);
}
