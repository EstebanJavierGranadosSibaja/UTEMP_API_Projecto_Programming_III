package org.una.programmingIII.UTEMP_Project.services.JwtServices;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.una.programmingIII.UTEMP_Project.dtos.TokenDTOs.TokenResponseDTO;
import org.una.programmingIII.UTEMP_Project.utilities.JwtUtil;

@Service
public class JwtServiceImplementation implements JwtService {

    private final JwtUtil jwtUtil;

    @Autowired
    public JwtServiceImplementation(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    public String generateAccessToken(String username) {
        return jwtUtil.generateAccessToken(username);
    }

    public String generateRefreshToken(String username) {
        return jwtUtil.generateRefreshToken(username);
    }

    public boolean isTokenExpired(String token) {
        return jwtUtil.isTokenExpired(token);
    }

    public String getUsernameFromToken(String token) {
        return jwtUtil.getUsernameFromToken(token);
    }

    public boolean validateToken(String token, String username) {
        return jwtUtil.validateToken(token, username);
    }

    public TokenResponseDTO refreshTokens(String refreshToken) {
        return jwtUtil.refreshTokens(refreshToken);
    }
}
