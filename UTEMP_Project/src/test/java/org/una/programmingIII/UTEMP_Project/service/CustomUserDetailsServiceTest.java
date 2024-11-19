package org.una.programmingIII.UTEMP_Project.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.UserDetails;
import org.una.programmingIII.UTEMP_Project.models.User;
import org.una.programmingIII.UTEMP_Project.repositories.UserRepository;
import org.una.programmingIII.UTEMP_Project.services.user.CustomUserDetailsService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = new User();
        user.setIdentificationNumber("12345");
        user.setName("testUser");
        user.setPassword("password");
    }

    @Test
    void testLoadUserByUsername_UserFound() {

        when(userRepository.findByIdentificationNumber("12345")).thenReturn(user);

        UserDetails userDetails = customUserDetailsService.loadUserByUsername("12345");

        assertNotNull(userDetails);
        assertEquals("testUser", userDetails.getUsername());
        verify(userRepository, times(1)).findByIdentificationNumber("12345");
    }

    @Test
    void testLoadUserByUsername_UserNotFound() {

        when(userRepository.findByIdentificationNumber("99999")).thenReturn(null);

        assertThrows(UsernameNotFoundException.class, () -> customUserDetailsService.loadUserByUsername("99999"));

        verify(userRepository, times(1)).findByIdentificationNumber("99999");
    }
}
