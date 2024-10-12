package org.una.programmingIII.UTEMP_Project.dtos;

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
    @Builder.Default
    private List<CourseDTO> coursesTeaching = new ArrayList<>();
    @Builder.Default
    private List<NotificationDTO> notifications = new ArrayList<>();
    @Builder.Default
    private List<EnrollmentDTO> userEnrollments = new ArrayList<>();
    @Builder.Default
    private List<SubmissionDTO> submissions = new ArrayList<>();

    @NotNull(message = "State must not be null")
    private UserState state;

    @NotNull(message = "Role must not be null")
    private UserRole role;

    @Builder.Default
    private List<UserPermission> permissions = new ArrayList<>();

    private LocalDateTime createdAt;

    private LocalDateTime lastUpdate;
}
