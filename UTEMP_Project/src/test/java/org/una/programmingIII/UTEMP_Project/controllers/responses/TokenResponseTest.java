package org.una.programmingIII.UTEMP_Project.controllers.responses;

import org.junit.jupiter.api.Test;
import org.una.programmingIII.UTEMP_Project.dtos.UserDTO;

import static org.junit.jupiter.api.Assertions.*;

class TokenResponseTest {

    @Test
    void testTokenResponseConstructor() {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setIdentificationNumber("12345");
        userDTO.setName("John Doe");

        TokenResponse tokenResponse = TokenResponse.builder()
                .token("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
                .tokenType("Bearer")
                .user(userDTO)
                .expiresIn(3600)
                .build();

        assertNotNull(tokenResponse.getToken(), "El token no debe ser nulo");
        assertNotNull(tokenResponse.getTokenType(), "El tipo de token no debe ser nulo");
        assertNotNull(tokenResponse.getUser(), "El usuario no debe ser nulo");
        assertEquals("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...", tokenResponse.getToken(), "El token no coincide");
        assertEquals("Bearer", tokenResponse.getTokenType(), "El tipo de token no coincide");
        assertEquals(3600, tokenResponse.getExpiresIn(), "El valor de 'expiresIn' no coincide");

        assertEquals("John Doe", tokenResponse.getUser().getName(), "El nombre del usuario no coincide");
        assertEquals("12345", tokenResponse.getUser().getIdentificationNumber(), "El número de identificación del usuario no coincide");
    }

    @Test
    void testTokenResponseNullFields() {
        TokenResponse tokenResponse = TokenResponse.builder()
                .token(null)
                .tokenType("Bearer")
                .user(null)
                .expiresIn(3600)
                .build();

        assertNull(tokenResponse.getToken(), "El token no debe ser nulo");
        assertNull(tokenResponse.getUser(), "El usuario no debe ser nulo");
    }

    @Test
    void testSettersAndGetters() {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setIdentificationNumber("12345");
        userDTO.setName("John Doe");

        TokenResponse tokenResponse = TokenResponse.builder()
                .token("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
                .tokenType("Bearer")
                .user(userDTO)
                .expiresIn(3600)
                .build();

        assertEquals("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...", tokenResponse.getToken(), "El token no coincide");
        assertEquals("Bearer", tokenResponse.getTokenType(), "El tipo de token no coincide");
        assertEquals(3600, tokenResponse.getExpiresIn(), "El valor de 'expiresIn' no coincide");
        assertNotNull(tokenResponse.getUser(), "El usuario no debe ser nulo");
        assertEquals("John Doe", tokenResponse.getUser().getName(), "El nombre del usuario no coincide");
    }



    @Test
    void testToString() {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setIdentificationNumber("12345");
        userDTO.setName("John Doe");

        TokenResponse tokenResponse = TokenResponse.builder()
                .token("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
                .tokenType("Bearer")
                .user(userDTO)
                .expiresIn(3600)
                .build();

        String expectedString = "TokenResponse(token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..., tokenType=Bearer, user=UserDTO{id=1, identificationNumber='12345', name='John Doe'}, expiresIn=3600)";
        assertEquals(expectedString, tokenResponse.toString(), "El método toString no devuelve la representación esperada");
    }
}

