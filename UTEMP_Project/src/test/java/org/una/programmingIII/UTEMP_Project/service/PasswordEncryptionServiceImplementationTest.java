package org.una.programmingIII.UTEMP_Project.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.una.programmingIII.UTEMP_Project.services.passwordEncryption.PasswordEncryptionServiceImplementation;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class PasswordEncryptionServiceImplementationTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PasswordEncryptionServiceImplementation passwordEncryptionService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testEncryptPassword() {

        String rawPassword = "password123";
        String encryptedPassword = "encryptedPassword";

        when(passwordEncoder.encode(rawPassword)).thenReturn(encryptedPassword);

        String result = passwordEncryptionService.encryptPassword(rawPassword);

        assertEquals(encryptedPassword, result);

        verify(passwordEncoder, times(1)).encode(rawPassword);
    }

    @Test
    public void testMatches() {

        String rawPassword = "password123";
        String encodedPassword = "encryptedPassword";

        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);

        boolean result = passwordEncryptionService.matches(rawPassword, encodedPassword);

        assertTrue(result);

        verify(passwordEncoder, times(1)).matches(rawPassword, encodedPassword);
    }

    @Test
    public void testMatchesWhenPasswordsDoNotMatch() {

        String rawPassword = "password123";
        String encodedPassword = "encryptedPassword";

        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(false);

        boolean result = passwordEncryptionService.matches(rawPassword, encodedPassword);

        assertFalse(result);

        verify(passwordEncoder, times(1)).matches(rawPassword, encodedPassword);
    }
}
