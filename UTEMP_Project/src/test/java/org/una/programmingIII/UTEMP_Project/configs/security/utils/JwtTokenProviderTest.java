package org.una.programmingIII.UTEMP_Project.configs.security.utils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.una.programmingIII.UTEMP_Project.dtos.TokenDTOs.TokenResponseDTO;
import org.una.programmingIII.UTEMP_Project.exceptions.InvalidTokenException;
import org.una.programmingIII.UTEMP_Project.exceptions.TokenExpiredException;
import org.una.programmingIII.UTEMP_Project.models.User;
import org.una.programmingIII.UTEMP_Project.models.UserPermission;
import org.una.programmingIII.UTEMP_Project.repositories.UserRepository;
import org.una.programmingIII.UTEMP_Project.security.utils.JwtTokenProvider;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtTokenProviderTest {

    @InjectMocks
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UserRepository userRepository;

    private String secretKey = "secretKey";
    private long accessTokenValidity = 3600000; // 1 hour
    private long refreshTokenValidity = 86400000; // 1 day

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setIdentificationNumber("000000000");
        testUser.setName("yo");
        testUser.setEmail("yo@example.com");
        List<UserPermission> permissions = new ArrayList<>();
        permissions.add(UserPermission.MANAGE_USERS);
        testUser.setPermissions(permissions);

        jwtTokenProvider.setSecretKey(secretKey);
        jwtTokenProvider.setAccessTokenValidity(accessTokenValidity);
        jwtTokenProvider.setRefreshTokenValidity(refreshTokenValidity);
    }

    @Test
    void testRefreshTokensUserNotFound() {
        String refreshToken = jwtTokenProvider.generateRefreshToken(testUser);

        when(userRepository.findById(testUser.getId())).thenReturn(Optional.empty());

        InvalidTokenException exception = assertThrows(InvalidTokenException.class, () -> {
            jwtTokenProvider.refreshTokens(refreshToken);
        });

        assertEquals("User not found for the given ID: " + testUser.getId(), exception.getMessage());
        verify(userRepository).findById(testUser.getId());
    }

    @Test
    void testRefreshTokens() {
        String refreshToken = jwtTokenProvider.generateRefreshToken(testUser);

        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));

        TokenResponseDTO tokenResponse = jwtTokenProvider.refreshTokens(refreshToken);

        assertNotNull(tokenResponse);
        assertNotNull(tokenResponse.getAccessToken());
        assertNotNull(tokenResponse.getRefreshToken());

        assertTrue(jwtTokenProvider.validateToken(tokenResponse.getAccessToken(), testUser.getIdentificationNumber()));
        assertTrue(jwtTokenProvider.validateToken(tokenResponse.getRefreshToken(), testUser.getIdentificationNumber()));
    }

    @Test
    void testGenerateToken() {
        String accessToken = jwtTokenProvider.generateAccessToken(testUser);
        assertNotNull(accessToken);

        String refreshToken = jwtTokenProvider.generateRefreshToken(testUser);
        assertNotNull(refreshToken);
    }

    @Test
    void testValidateToken() {
        String token = jwtTokenProvider.generateAccessToken(testUser);
        assertTrue(jwtTokenProvider.validateToken(token, testUser.getIdentificationNumber()));
    }

    @Test
    void testValidateTokenExpired() {
        String token = jwtTokenProvider.generateAccessToken(testUser);

        String expiredToken = Jwts.builder()
                .setSubject(testUser.getIdentificationNumber())
                .setIssuedAt(new Date(System.currentTimeMillis() - 100000))
                .setExpiration(new Date(System.currentTimeMillis() - 1000))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();

        assertFalse(jwtTokenProvider.validateToken(expiredToken, testUser.getIdentificationNumber()));
    }

    @Test
    void testRefreshTokensExpired() {
        String expiredRefreshToken = Jwts.builder()
                .setSubject(testUser.getIdentificationNumber())
                .setIssuedAt(new Date(System.currentTimeMillis() - 100000))
                .setExpiration(new Date(System.currentTimeMillis() - 1000))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();

        Exception exception = assertThrows(TokenExpiredException.class, () -> {
            jwtTokenProvider.refreshTokens(expiredRefreshToken);
        });
        assertEquals("Refresh token has expired.", exception.getMessage());
    }
}