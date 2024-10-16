package org.una.programmingIII.UTEMP_Project.services.JwtServices;

import org.una.programmingIII.UTEMP_Project.dtos.TokenDTOs.TokenResponseDTO;

public interface JwtService {
    String generateAccessToken(String username);
    String generateRefreshToken(String username);
    boolean isTokenExpired(String token);
    String getUsernameFromToken(String token);
    boolean validateToken(String token, String username);
    TokenResponseDTO refreshTokens(String refreshToken);
}
