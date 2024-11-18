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

    @Test
    void testSetName() {
        user.setName("Jane Doe");
        assertEquals("Jane Doe", user.getName());
    }

    @Test
    void testSetEmail() {
        user.setEmail("janedoe@example.com");
        assertEquals("janedoe@example.com", user.getEmail());
    }

    @Test
    void testSetPassword() {
        user.setPassword("newpassword123");
        assertEquals("newpassword123", user.getPassword());
    }

    @Test
    void testUserPermissions() {
        assertTrue(user.getPermissions().contains(UserPermission.MANAGE_COURSES));
        assertTrue(user.getPermissions().contains(UserPermission.ADD_TEACHER_COURSES));
    }

    @Test
    void testSetRole() {
        user.setRole(UserRole.ADMIN);
        assertEquals(UserRole.ADMIN, user.getRole());
    }

    @Test
    void testSetState() {
        user.setState(UserState.INACTIVE);
        assertEquals(UserState.INACTIVE, user.getState());
    }

    @Test
    void testTimestampsOnCreate() {
        assertNotNull(user.getCreatedAt());
        assertNotNull(user.getLastUpdate());
        assertEquals(user.getCreatedAt(), user.getLastUpdate());
    }

    @Test
    void testOnUpdate() {
        LocalDateTime initialUpdate = user.getLastUpdate();
        user.onUpdate();
        assertNotEquals(initialUpdate, user.getLastUpdate());
    }

    @Test
    void testGetAuthorities() {
        user.setRole(UserRole.ADMIN);
        var authorities = user.getAuthorities();
        assertNotNull(authorities);
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("ADMIN")));
    }
}
