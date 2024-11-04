package org.una.programmingIII.UTEMP_Project.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.una.programmingIII.UTEMP_Project.models.UserPermission;
import org.una.programmingIII.UTEMP_Project.models.UserRole;
import org.una.programmingIII.UTEMP_Project.models.UserState;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {

    @Schema(description = "Unique identifier for the user", example = "1")
    private Long id;

    @NotBlank(message = "Name must not be blank")
    @Size(max = 100, message = "Name must be at most 100 characters long")
    @Schema(description = "Full name of the user", example = "John Doe")
    private String name;

    @NotNull(message = "Email must not be null")
    @Email(message = "Email should be valid")
    @Size(max = 150, message = "Email must be at most 150 characters long")
    @Schema(description = "Email address of the user", example = "john.doe@example.com")
    private String email;

    @NotNull(message = "Password must not be null")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    @Size(max = 128, message = "Password must be at most 128 characters long")
    @Schema(description = "Password for user authentication", example = "SecurePassword123")
    private String password;

    @Size(max = 50, message = "Identification number must be at most 50 characters long")
    @Schema(description = "Identification number of the user", example = "ID12345678")
    private String identificationNumber;

    @Builder.Default
    @Schema(description = "List of courses that the user is teaching")
    private List<CourseDTO> coursesTeaching = new ArrayList<>();

    @Builder.Default
    @Schema(description = "List of notifications associated with the user")
    private List<NotificationDTO> notifications = new ArrayList<>();

    @Builder.Default
    @Schema(description = "List of enrollments for the user")
    private List<EnrollmentDTO> userEnrollments = new ArrayList<>();

    @Builder.Default
    @Schema(description = "List of submissions made by the user")
    private List<SubmissionDTO> submissions = new ArrayList<>();

    @NotNull(message = "State must not be null")
    @Schema(description = "Current state of the user account")
    private UserState state;

    @NotNull(message = "Role must not be null")
    @Schema(description = "Role assigned to the user", example = "ADMIN")
    private UserRole role;

    @Builder.Default
    @Schema(description = "List of permissions assigned to the user")
    private List<UserPermission> permissions = new ArrayList<>();

    @Schema(description = "Timestamp of when the user was created")
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp of the last update to the user record")
    private LocalDateTime lastUpdate;
}