package org.una.programmingIII.UTEMP_Project.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.una.programmingIII.UTEMP_Project.dtos.UserDTO;
import org.una.programmingIII.UTEMP_Project.exceptions.InvalidDataException;
import org.una.programmingIII.UTEMP_Project.models.User;
import org.una.programmingIII.UTEMP_Project.repositories.UserRepository;
import org.una.programmingIII.UTEMP_Project.services.UserServiceImplementation;
import org.una.programmingIII.UTEMP_Project.services.passwordEncryption.PasswordEncryptionService;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.una.programmingIII.UTEMP_Project.models.UserRole.STUDENT;

public class UserServiceImplementationTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncryptionService passwordEncryptionService;
    @Mock
    private Pageable pageable;

    @InjectMocks
    private UserServiceImplementation userService;

    private UserDTO userDTO;
    private User user;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setName("John Doe");
        userDTO.setEmail("johndoe@example.com");
        userDTO.setIdentificationNumber("123456");
        userDTO.setPassword("password");
        userDTO.setRole(STUDENT);

        user = new User();
        user.setId(1L);
        user.setName("John Doe");
        user.setEmail("johndoe@example.com");
        user.setIdentificationNumber("123456");
    }

    @Test
    public void testCreateUserSuccess() {
        when(userRepository.existsByIdentificationNumber(userDTO.getIdentificationNumber())).thenReturn(false);
        when(passwordEncryptionService.encryptPassword(userDTO.getPassword())).thenReturn("encryptedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserDTO result = userService.createUser(userDTO);

        assertNotNull(result);
        assertEquals(userDTO.getName(), result.getName());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    public void testCreateUserAlreadyExists() {
        when(userRepository.existsByIdentificationNumber(userDTO.getIdentificationNumber())).thenReturn(true);

        assertThrows(InvalidDataException.class, () -> userService.createUser(userDTO));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void testGetUserByIdSuccess() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        Optional<UserDTO> result = userService.getUserById(1L);

        assertTrue(result.isPresent());
        assertEquals(userDTO.getName(), result.get().getName());
    }

    @Test
    public void testGetUserByIdNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(InvalidDataException.class, () -> userService.getUserById(1L));
    }

    @Test
    public void testGetAllUsersSuccess() {
        Page<User> userPage = new PageImpl<>(List.of(user));
        when(userRepository.findAll(pageable)).thenReturn(userPage);

        Page<UserDTO> result = userService.getAllUsers(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(userDTO.getName(), result.getContent().getFirst().getName());
    }

    @Test
    public void testUpdateUserSuccess() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        userDTO.setName("Updated Name");
        Optional<UserDTO> result = userService.updateUser(1L, userDTO);

        assertTrue(result.isPresent());
        assertEquals("Updated Name", result.get().getName());
    }

    @Test
    public void testDeleteUserSuccess() {
//        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
//        doNothing().when(userRepository).delete(any(User.class));
//
//        boolean result = userService.deleteUser(1L, true);
//
//        assertTrue(result);
//        verify(userRepository, times(1)).delete(any(User.class));
    }

    @Test
    public void testDeleteUserNotFound() {
//        when(userRepository.findById(1L)).thenReturn(Optional.empty());
//
//        boolean result = userService.deleteUser(1L, true);
//
//        assertFalse(result);
//        verify(userRepository, never()).delete(any(User.class));
    }
}
