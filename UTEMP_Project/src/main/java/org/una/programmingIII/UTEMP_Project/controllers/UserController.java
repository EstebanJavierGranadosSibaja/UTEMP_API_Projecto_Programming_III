package org.una.programmingIII.UTEMP_Project.controllers;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.una.programmingIII.UTEMP_Project.dtos.CourseDTO;
import org.una.programmingIII.UTEMP_Project.dtos.EnrollmentDTO;
import org.una.programmingIII.UTEMP_Project.dtos.UserDTO;
import org.una.programmingIII.UTEMP_Project.controllers.responses.ApiResponse;
import org.una.programmingIII.UTEMP_Project.exceptions.InvalidDataException;
import org.una.programmingIII.UTEMP_Project.exceptions.ResourceNotFoundException;
import org.una.programmingIII.UTEMP_Project.services.user.CustomUserDetails;
import org.una.programmingIII.UTEMP_Project.services.user.UserService;

import java.util.Optional;

@RestController
@RequestMapping("/utemp/users")
public class UserController {

    private final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Obtener usuario actual
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserDTO>> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long authenticatedUserId = ((CustomUserDetails) authentication.getPrincipal()).getUser().getId();

        try {
            Optional<UserDTO> userDTO = userService.getUserById(authenticatedUserId);
            return getApiResponseResponseEntity(userDTO);
        } catch (Exception e) {
            logger.error("Error retrieving current user: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error retrieving current user"));
        }
    }

    // crud basico
    @GetMapping
    @PreAuthorize("hasAuthority('MANAGE_USERS')")
    public ResponseEntity<Page<UserDTO>> getAllUsers(@PageableDefault Pageable pageable) {
        logger.info("Fetching all users with pagination");
        try {
            Page<UserDTO> users = userService.getAllUsers(pageable);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            logger.error("Error fetching all users: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_USERS')")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        try {
            Optional<UserDTO> userDTO = userService.getUserById(id);
            return userDTO.map(ResponseEntity::ok)
                    .orElseGet(() -> {
                        logger.warn("User with ID {} not found", id);
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
                    });
        } catch (InvalidDataException e) {
            logger.error("Invalid data error while retrieving user by ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            logger.error("Error retrieving user by ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // Obtener usuario por número de identificación
    @GetMapping("/identification/{identificationNumber}")
    @PreAuthorize("hasAuthority('MANAGE_USERS') or hasAuthority('ADMIN')")
    public ResponseEntity<UserDTO> getUserByIdentificationNumber(@PathVariable String identificationNumber) {
        try {
            Optional<UserDTO> userDTO = userService.getUserByIdentificationNumber(identificationNumber);
            return userDTO.map(ResponseEntity::ok)
                    .orElseGet(() -> {
                        logger.warn("User with identification number {} not found", identificationNumber);
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
                    });
        } catch (Exception e) {
            logger.error("Error retrieving user by identification number {}: {}", identificationNumber, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // Crear usuario
    @PostMapping
    @PreAuthorize("hasAuthority('MANAGE_USERS')")
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody UserDTO userDTO) {
        try {
            UserDTO createdUser = userService.createUser(userDTO);
            return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
        } catch (InvalidDataException e) {
            logger.error("Error creating user: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(null);
        } catch (Exception e) {
            logger.error("Error creating user: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    // Actualizar usuario
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_USERS')")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id,
                                              @Valid @RequestBody UserDTO userDTO) {
        try {
            Optional<UserDTO> updatedUser = userService.updateUser(id, userDTO);
            return updatedUser.map(ResponseEntity::ok)
                    .orElseGet(() -> {
                        logger.warn("User with ID {} not found for update", id);
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
                    });
        } catch (InvalidDataException e) {
            logger.error("Error updating user: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(null);
        } catch (Exception e) {
            logger.error("Error updating user: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    // Eliminar usuario
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_USERS')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id,
                                           @RequestParam Boolean isPermanentDelete) {
        try {
            boolean deleted = userService.deleteUser(id, isPermanentDelete);
            return deleted ? ResponseEntity.noContent().build()
                    : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            logger.error("Error deleting user with ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}/courses")
    @PreAuthorize("hasAuthority('USER_INFO')")
    public ResponseEntity<Page<CourseDTO>> getCoursesTeachingByUserId(@PathVariable Long teacherId,
                                                                      Pageable pageable) {
        try {
            Page<CourseDTO> courses = userService.getCoursesTeachingByUserId(teacherId, pageable);
            return ResponseEntity.ok(courses);
        } catch (ResourceNotFoundException e) {
            logger.warn("Courses for teacher ID {} not found", teacherId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            logger.error("Error retrieving courses for teacher ID {}: {}", teacherId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Asignar curso a docente
    @PostMapping("/{userId}/courses/{courseId}")
    @PreAuthorize("hasAuthority('MANAGER_CURSE')")
    public ResponseEntity<Void> assignCourseToTeacher(@PathVariable Long userId,
                                                      @PathVariable Long courseId) {
        try {
            userService.assignCourseToTeacher(userId, courseId);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (ResourceNotFoundException e) {
            logger.warn("Failed to assign course ID {} to user ID {}", courseId, userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (InvalidDataException e) {
            logger.error("Invalid data when assigning course ID {} to user ID {}: {}", courseId, userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            logger.error("Error assigning course ID {} to user ID {}: {}", courseId, userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{userId}/courses/{courseId}")
    @PreAuthorize("hasAuthority('MANAGER_CURSE')")
    public ResponseEntity<Void> removeCourseFromTeacher(@PathVariable Long userId,
                                                        @PathVariable Long courseId) {
        try {
            userService.removeCourseFromTeacher(userId, courseId);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            logger.warn("Failed to remove course ID {} from user ID {}", courseId, userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            logger.error("Error removing course ID {} from user ID {}: {}", courseId, userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{userId}/enrollments")
    @PreAuthorize("hasAuthority('USER_INFO')")
    public ResponseEntity<Page<EnrollmentDTO>> getEnrollmentsByUserId(@PathVariable Long userId, Pageable pageable) {
        try {
            Page<EnrollmentDTO> enrollments = userService.getEnrollmentsByStudentId(userId, pageable);
            return ResponseEntity.ok(enrollments);
        } catch (ResourceNotFoundException e) {
            logger.warn("No enrollments found for user ID {}", userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            logger.error("Error retrieving enrollments for user ID {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{userId}/enrollments/{courseId}")
    @PreAuthorize("hasAuthority('USER_INFO')")
    public ResponseEntity<Void> enrollUserToCourse(@PathVariable Long userId,
                                                   @PathVariable Long courseId) {
        try {
            userService.enrollUserToCourse(userId, courseId);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (ResourceNotFoundException e) {
            logger.warn("Failed to enroll user ID {} to course ID {}", userId, courseId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (InvalidDataException e) {
            logger.error("Invalid data when enrolling user ID {} to course ID {}: {}", userId, courseId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @DeleteMapping("/{userId}/enrollments/{courseId}")
    @PreAuthorize("hasAuthority('MANAGER_CURSE')")
    public ResponseEntity<Void> unrollUserFromCourse(@PathVariable Long userId,
                                                     @PathVariable Long courseId) {
        try {
            userService.unrollUserFromCourse(userId, courseId);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            logger.warn("Failed to unroll user ID {} from course ID {}: {}", userId, courseId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (InvalidDataException e) {
            logger.error("Invalid data when attempting to unroll user ID {} from course ID {}: {}", userId, courseId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            logger.error("Error unrolling user ID {} from course ID {}: {}", userId, courseId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Método privado para manejar la respuesta de usuario
    private ResponseEntity<ApiResponse<UserDTO>> getApiResponseResponseEntity(Optional<UserDTO> userDTO) {
        return userDTO.map(user -> {
            ApiResponse<UserDTO> response = new ApiResponse<>(user);
            return ResponseEntity.ok(response);
        }).orElseGet(() -> {
            ApiResponse<UserDTO> response = new ApiResponse<>(HttpStatus.NOT_FOUND.value(), "User not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        });
    }
}
