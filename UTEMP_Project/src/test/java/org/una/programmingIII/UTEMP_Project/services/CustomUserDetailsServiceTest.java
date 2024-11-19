package org.una.programmingIII.UTEMP_Project.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.una.programmingIII.UTEMP_Project.models.User;
import org.una.programmingIII.UTEMP_Project.models.UserState;
import org.una.programmingIII.UTEMP_Project.repositories.UserRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Mock
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setIdentificationNumber("000000000");
        testUser.setName("Test User");
        testUser.setEmail("testuser@example.com");
        testUser.setPassword("password");
        testUser.setState(UserState.ACTIVE);
        // Asigna permisos si es necesario
    }

    @Test
    void loadUserByUsername_UserFound() {
        when(userRepository.findByIdentificationNumber(testUser.getIdentificationNumber())).thenReturn(testUser);

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(testUser.getIdentificationNumber());

        assertNotNull(userDetails);
        assertEquals(testUser.getIdentificationNumber(), userDetails.getUsername());
        verify(userRepository).findByIdentificationNumber(testUser.getIdentificationNumber());
    }

    @Test
    void loadUserByUsername_UserNotFound() {
        // Mockear el repositorio para devolver null
        when(userRepository.findByIdentificationNumber(testUser.getIdentificationNumber())).thenReturn(null);

        // Probar la excepción
        Exception exception = assertThrows(UsernameNotFoundException.class, () -> {
            customUserDetailsService.loadUserByUsername(testUser.getIdentificationNumber());
        });

        // Verificar que el mensaje de la excepción es el esperado
        assertEquals("User not found with identification number: " + testUser.getIdentificationNumber(), exception.getMessage());
    }
}
