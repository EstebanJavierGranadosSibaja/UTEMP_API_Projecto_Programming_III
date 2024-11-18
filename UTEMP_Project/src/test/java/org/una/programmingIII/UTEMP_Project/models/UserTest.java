package org.una.programmingIII.UTEMP_Project.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    private User user;

    @BeforeEach
    void setUp() {
        // Creamos el usuario para las pruebas
        user = User.builder()
                .name("John Doe")
                .email("johndoe@example.com")
                .password("securepassword123")
                .role(UserRole.TEACHER)
                .state(UserState.ACTIVE)
                .permissions(Arrays.asList(UserPermission.MANAGE_COURSES, UserPermission.ADD_TEACHER_COURSES))
                .createdAt(LocalDateTime.now())
                .lastUpdate(LocalDateTime.now())
                .build();
    }

    // Test: Verificar la creación del usuario
    @Test
    void testUserCreation() {
        assertNotNull(user);
        assertEquals("John Doe", user.getName());
        assertEquals("johndoe@example.com", user.getEmail());
        assertEquals(UserRole.TEACHER, user.getRole());
        assertEquals(UserState.ACTIVE, user.getState());
        assertNotNull(user.getCreatedAt());
        assertNotNull(user.getLastUpdate());
    }

    // Test: Verificar el cambio de nombre del usuario
    @Test
    void testSetName() {
        user.setName("Jane Doe");
        assertEquals("Jane Doe", user.getName());
    }

    // Test: Verificar el cambio de correo electrónico del usuario
    @Test
    void testSetEmail() {
        user.setEmail("janedoe@example.com");
        assertEquals("janedoe@example.com", user.getEmail());
    }

    // Test: Verificar la actualización de la contraseña del usuario
    @Test
    void testSetPassword() {
        user.setPassword("newpassword123");
        assertEquals("newpassword123", user.getPassword());
    }

    // Test: Verificar los permisos del usuario
    @Test
    void testUserPermissions() {
        assertTrue(user.getPermissions().contains(UserPermission.MANAGE_COURSES));
        assertTrue(user.getPermissions().contains(UserPermission.ADD_TEACHER_COURSES));
    }

    // Test: Verificar que el rol del usuario sea correcto
    @Test
    void testSetRole() {
        user.setRole(UserRole.ADMIN);
        assertEquals(UserRole.ADMIN, user.getRole());
    }

    // Test: Verificar el estado del usuario
    @Test
    void testSetState() {
        user.setState(UserState.INACTIVE);
        assertEquals(UserState.INACTIVE, user.getState());
    }

    // Test: Verificar las marcas de tiempo (timestamps) al crear el usuario
    @Test
    void testTimestampsOnCreate() {
        assertNotNull(user.getCreatedAt());
        assertNotNull(user.getLastUpdate());
        assertEquals(user.getCreatedAt(), user.getLastUpdate()); // Inicialmente, deberían ser iguales
    }

    // Test: Verificar que lastUpdate se cambia después de la actualización
    @Test
    void testOnUpdate() {
        LocalDateTime initialUpdate = user.getLastUpdate();
        user.onUpdate(); // Simular una actualización
        assertNotEquals(initialUpdate, user.getLastUpdate()); // Asegurarse de que lastUpdate se actualizó
    }

    // Test: Verificar el valor de 'getAuthorities' según el rol
    @Test
    void testGetAuthorities() {
        user.setRole(UserRole.ADMIN);
        var authorities = user.getAuthorities();
        assertNotNull(authorities);
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("ADMIN")));
    }
}
