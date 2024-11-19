package org.una.programmingIII.UTEMP_Project.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
import org.una.programmingIII.UTEMP_Project.exceptions.InvalidDataException;
import org.una.programmingIII.UTEMP_Project.exceptions.ResourceNotFoundException;
import org.una.programmingIII.UTEMP_Project.models.UserRole;
import org.una.programmingIII.UTEMP_Project.services.CustomUserDetails;
import org.una.programmingIII.UTEMP_Project.services.UserService;
import org.una.programmingIII.UTEMP_Project.utils.PageConverter;
import org.una.programmingIII.UTEMP_Project.utils.PageDTO;

import java.util.Optional;


@RestController
@RequestMapping("/utemp/users")
public class UserController {

    private final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(
            summary = "Get Current User",
            description = "Retrieves the details of the currently authenticated user."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User retrieved successfully.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Current user not found.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"Current user not found\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error retrieving current user.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"Error retrieving current user.\"}")
                    )
            )
    })
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDTO> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long authenticatedUserId = ((CustomUserDetails) authentication.getPrincipal()).getUser().getId();

        try {
            Optional<UserDTO> userDTO = userService.getUserById(authenticatedUserId);
            return userDTO.map(ResponseEntity::ok)
                    .orElseGet(() -> {
                        logger.warn("Current user not found");
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
                    });
        } catch (Exception e) {
            logger.error("Error retrieving current user: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation(
            summary = "Get All Users",
            description = "Retrieves all users with pagination."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "List of users retrieved successfully.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error retrieving the list of users.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"Error fetching all users.\"}")
                    )
            )
    })
    @GetMapping
    @PreAuthorize("hasAuthority('MANAGE_USERS')")
    public ResponseEntity<PageDTO<UserDTO>> getAllUsers(@PageableDefault Pageable pageable) {
        logger.info("Fetching all users with pagination");
        try {

            Page<UserDTO> usersDTO = userService.getAllUsers(pageable); // Cambiar a Page<User>
            PageDTO<UserDTO> userDTOPage = PageConverter.convertPageToDTO(usersDTO, userDTO -> userDTO);

            return ResponseEntity.ok(userDTOPage);
        } catch (Exception e) {
            logger.error("Error fetching all users: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    @Operation(
            summary = "Get All Users by role",
            description = "Retrieves all users by role with pagination."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "List of users by role retrieved successfully.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error retrieving the list of users by role.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"Error fetching all users.\"}")
                    )
            )
    })
    @GetMapping("/usersByRole/{role}")
    @PreAuthorize("hasAuthority('MANAGE_USERS')")
    public ResponseEntity<PageDTO<UserDTO>> getAllUsersByRole(@PathVariable UserRole role, @PageableDefault Pageable pageable) {
        logger.info("Fetching all users by role with pagination");
        try {

            Page<UserDTO> usersDTO = userService.getAllUsersByRole(role, pageable); // Cambiar a Page<User>
            PageDTO<UserDTO> userDTOPage = PageConverter.convertPageToDTO(usersDTO, userDTO -> userDTO);

            return ResponseEntity.ok(userDTOPage);
        } catch (Exception e) {
            logger.error("Error fetching all users by role: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    @Operation(
            summary = "Get User by ID",
            description = "Retrieves a specific user using their ID."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User found.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"User not found.\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid data error.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"Invalid data provided.\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error retrieving the user.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"Error retrieving user.\"}")
                    )
            )
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_USERS')")
    public ResponseEntity<UserDTO> getUserById(
            @Parameter(description = "ID of the user to retrieve") @PathVariable Long id) {
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

    @Operation(
            summary = "Get User by Identification Number",
            description = "Retrieves a specific user using their identification number."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User found.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"User not found.\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error retrieving the user.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"Error retrieving user.\"}")
                    )
            )
    })
    @GetMapping("/identification/{identificationNumber}")
    @PreAuthorize("hasAuthority('MANAGE_USERS')")
    public ResponseEntity<UserDTO> getUserByIdentificationNumber(
            @Parameter(description = "Identification number of the user") @PathVariable String identificationNumber) {
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

    @Operation(
            summary = "Get User by role",
            description = "Retrieves a specific user using their role."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User found.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"User not found.\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error retrieving the user.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"Error retrieving user.\"}")
                    )
            )
    })
    @GetMapping("/role/{role}")
    @PreAuthorize("hasAuthority('MANAGE_USERS')")
    public ResponseEntity<UserDTO> getUserByRole(
            @Parameter(description = "Identification number of the user") @PathVariable UserRole role) {
        try {
            Optional<UserDTO> userDTO = userService.getUserByRole(role);
            return userDTO.map(ResponseEntity::ok)
                    .orElseGet(() -> {
                        logger.warn("User with role {} not found", role);
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
                    });
        } catch (Exception e) {
            logger.error("Error retrieving user by role {}: {}", role, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation(
            summary = "Create User",
            description = "Creates a new user in the system."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "User created successfully.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid data provided.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"Invalid data provided.\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error creating the user.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"Error creating user.\"}")
                    )
            )
    })
    @PostMapping
    @PreAuthorize("hasAuthority('MANAGE_USERS')")
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody UserDTO userDTO) {
        try {
            UserDTO createdUser = userService.createUser(userDTO);
            return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
        } catch (InvalidDataException e) {
            logger.error("1Error creating user: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(null);
        } catch (Exception e) {
            logger.error("Error creating user: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    @Operation(
            summary = "Update User",
            description = "Updates the details of an existing user."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User updated successfully.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found for update.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"User not found for update.\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid data provided.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"Invalid data provided.\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error updating the user.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"Error updating user.\"}")
                    )
            )
    })
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
            logger.error("2Error updating user: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(null);
        } catch (Exception e) {
            logger.error("Error updating user: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    @Operation(
            summary = "Delete User",
            description = "Deletes a specific user from the system."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "User deleted successfully."
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found for deletion.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"User not found for deletion.\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error deleting the user.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"Error deleting user.\"}")
                    )
            )
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_USERS')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        try {
            boolean deleted = userService.deleteUser(id);
            return deleted ? ResponseEntity.noContent().build()
                    : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            logger.error("Error deleting user with ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(
            summary = "Get Courses Taught by User",
            description = "Retrieves all courses taught by a specific user."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Courses retrieved successfully."
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No courses found for the user.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"No courses found for the user.\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error retrieving courses.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"Error retrieving courses.\"}")
                    )
            )
    })
    @GetMapping("/{id}/courses")
    @PreAuthorize("hasAuthority('GET_TEACHER_COURSES')")
    public ResponseEntity<PageDTO<CourseDTO>> getCoursesTeachingByUserId(@PathVariable Long id,
                                                                         Pageable pageable) {
        try {
            Page<CourseDTO> coursesPage = userService.getCoursesTeachingByUserId(id, pageable);

            // Convertir la página a PageDTO
            PageDTO<CourseDTO> coursesDTOPage = PageConverter.convertPageToDTO(coursesPage, courseDTO -> courseDTO);

            // Retornar la respuesta con el PageDTO
            return ResponseEntity.ok(coursesDTOPage);
        } catch (ResourceNotFoundException e) {
            logger.warn("Courses for teacher ID {} not found", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            logger.error("Error retrieving courses for teacher ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(
            summary = "Assign Course to Teacher",
            description = "Assigns a specific course to a teacher."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Course successfully assigned to the teacher."
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Course or teacher not found.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"Course or teacher not found.\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid data provided.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"Invalid data provided.\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error assigning the course.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"Error assigning course.\"}")
                    )
            )
    })
    @PostMapping("/{userId}/courses/{courseId}")
    @PreAuthorize("hasAuthority('ADD_TEACHER_COURSES')")
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

    @Operation(
            summary = "Remove Course from Teacher",
            description = "Removes a course assigned to a specific teacher."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Course successfully removed from the teacher."
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Course or teacher not found.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"Course or teacher not found.\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error removing the course.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"Error removing course.\"}")
                    )
            )
    })
    @DeleteMapping("/{userId}/courses/{courseId}")
    @PreAuthorize("hasAuthority('REMOVE_TEACHER_COURSE')")
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

    @Operation(
            summary = "Get Enrollments by User ID",
            description = "Retrieves all enrollments for a specific user."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Enrollments retrieved successfully.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Page.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No enrollments found for the user.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"No enrollments found for user.\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error retrieving enrollments.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"Error retrieving enrollments.\"}")
                    )
            )
    })
    @GetMapping("/{userId}/enrollments")
    @PreAuthorize("hasAuthority('GET_STUDENT_ENROLLMENTS')")
    public ResponseEntity<PageDTO<EnrollmentDTO>> retrieveEnrollmentsForUser(
            @PathVariable Long userId,
            Pageable pageable) {
        try {
            Page<EnrollmentDTO> enrollments = userService.getEnrollmentsByStudentId(userId, pageable);
            PageDTO<EnrollmentDTO> enroll = PageConverter.convertPageToDTO(enrollments, EnrollmentDTO -> EnrollmentDTO);

            return ResponseEntity.ok(enroll);
        } catch (ResourceNotFoundException e) {
            logger.warn("No enrollments found for user ID {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            logger.error("Error retrieving enrollments for user ID {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(
            summary = "Enroll User in Course",
            description = "Enrolls a user in a specific course."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "206",
                    description = "User enrollment is being processed asynchronously."
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User or course not found.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"User or course not found.\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid data provided.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"Invalid data provided.\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error enrolling the user.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"Error enrolling user.\"}")
                    )
            )
    })
    @PostMapping("/{userId}/enrollments/{courseId}")
    @PreAuthorize("hasAuthority('ADD_STUDENT_COURSES')")
    public ResponseEntity<Void> registerUserForCourseEnrollment(
            @PathVariable Long userId,
            @PathVariable Long courseId) {
        try {
            userService.enrollUserToCourse(userId, courseId);
            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).build();
        } catch (ResourceNotFoundException e) {
            logger.warn("Failed to enroll user ID {} to course ID {}: {}", userId, courseId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (InvalidDataException e) {
            logger.error("Invalid data when enrolling user ID {} to course ID {}: {}", userId, courseId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            logger.error("Error enrolling user ID {} to course ID {}: {}", userId, courseId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(
            summary = "Unenroll User from Course",
            description = "Unenrolls a user from a specific course."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "User successfully unenrolled from the course."
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User or course not found.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"User or course not found.\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid data provided.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"Invalid data provided.\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error unenrolling the user.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"Error unenrolling user.\"}")
                    )
            )
    })
    @DeleteMapping("/{userId}/enrollments/{courseId}")
    @PreAuthorize("hasAuthority('REMOVE_STUDENT_COURSES')")
    public ResponseEntity<Void> removeUserFromCourseEnrollment(
            @PathVariable Long userId,
            @PathVariable Long courseId) {
        try {
            userService.unrollUserFromCourse(userId, courseId);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            logger.warn("Failed to remove user ID {} from course ID {}: {}", userId, courseId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (InvalidDataException e) {
            logger.error("Invalid data when attempting to remove user ID {} from course ID {}: {}", userId, courseId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            logger.error("Error removing user ID {} from course ID {}: {}", userId, courseId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}