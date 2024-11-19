package org.una.programmingIII.UTEMP_Project.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
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

    // Basic user information
    private Long id;

    @NotBlank(message = "Name must not be blank")
    @Size(max = 100, message = "Name must be at most 100 characters long")
    private String name;

    @NotNull(message = "Email must not be null")
    @Email(message = "Email should be valid")
    @Size(max = 150, message = "Email must be at most 150 characters long")
    private String email;

    @NotNull(message = "Password must not be null")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    @Size(max = 128, message = "Password must be at most 128 characters long")
    private String password;

    @Size(max = 50, message = "Identification number must be at most 50 characters long")
    private String identificationNumber;

    // User status and role
    @NotNull(message = "State must not be null")
    private UserState state;

    @NotNull(message = "Role must not be null")
    private UserRole role;

    // Timestamps
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSSSSS")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSSSSS")
    private LocalDateTime lastUpdate;

    // Permissions
    @Builder.Default
    private List<UserPermission> permissions = new ArrayList<>();

    // Relationships and Collections
    @Builder.Default
    @JsonManagedReference("user-courses")  // Unique name for coursesTeaching
    private List<CourseDTO> coursesTeaching = new ArrayList<>();

    @Builder.Default
    @JsonManagedReference("user-notifications")  // Unique name for notifications
    private List<NotificationDTO> notifications = new ArrayList<>();

    @Builder.Default
    @JsonManagedReference("user-enrollments")  // Unique name for userEnrollments
    private List<EnrollmentDTO> userEnrollments = new ArrayList<>();

    @Builder.Default
    @JsonManagedReference("user-submissions")  // Unique name for submissions
    private List<SubmissionDTO> submissions = new ArrayList<>();

    @Override
    public String toString() {
        return "UserDTO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", password='" + (password != null ? "*****" : "null") + '\'' +  // Ocultamos la contraseña
                ", identificationNumber='" + identificationNumber + '\'' +
                ", state=" + state +
                ", role=" + role +
                ", createdAt=" + createdAt +
                ", lastUpdate=" + lastUpdate +
                '}';
    }
}