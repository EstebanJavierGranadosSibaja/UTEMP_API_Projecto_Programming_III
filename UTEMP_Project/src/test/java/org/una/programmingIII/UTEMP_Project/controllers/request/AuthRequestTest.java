package org.una.programmingIII.UTEMP_Project.controllers.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.una.programmingIII.UTEMP_Project.controllers.request.AuthRequest;

import static org.junit.jupiter.api.Assertions.*;

class AuthRequestTest {

    private AuthRequest authRequest;

    @BeforeEach
    void setUp() {

        authRequest = new AuthRequest();
    }

    @Test
    void testIdentificationNumberGetterAndSetter() {
        String testIdentificationNumber = "123456789";

        authRequest.setIdentificationNumber(testIdentificationNumber);

        assertEquals(testIdentificationNumber, authRequest.getIdentificationNumber(), "Identification number should be set and retrieved correctly.");
    }

    @Test
    void testPasswordGetterAndSetter() {
        String testPassword = "password123";
        authRequest.setPassword(testPassword);
        assertEquals(testPassword, authRequest.getPassword(), "Password should be set and retrieved correctly.");
    }

    @Test
    void testNullValues() {
        authRequest.setIdentificationNumber(null);
        authRequest.setPassword(null);

        assertNull(authRequest.getIdentificationNumber(), "Identification number should be null.");
        assertNull(authRequest.getPassword(), "Password should be null.");
    }
}
