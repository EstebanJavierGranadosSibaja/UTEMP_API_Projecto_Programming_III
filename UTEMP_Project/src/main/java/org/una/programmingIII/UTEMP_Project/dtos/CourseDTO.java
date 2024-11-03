package org.una.programmingIII.UTEMP_Project.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.una.programmingIII.UTEMP_Project.models.CourseState;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseDTO {

    @Schema(description = "Unique identifier for the course", example = "1")
    private Long id;

    @NotNull(message = "Course name must not be null")
    @Size(max = 50, message = "Course name must not exceed 50 characters")
    @Schema(description = "Name of the course", example = "Computer Science")
    private String name;

    @NotNull(message = "Description must not be null")
    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Schema(description = "Detailed description of the course", example = "An introduction to computer science principles and programming.")
    private String description;

    @NotNull(message = "Teacher must not be null")
    @Builder.Default
    @Schema(description = "Teacher assigned to the course")
    private UserDTO teacher = new UserDTO();

    @NotNull(message = "Department must not be null")
    @Builder.Default
    @Schema(description = "Department to which the course belongs")
    private DepartmentDTO department = new DepartmentDTO();

    @Builder.Default
    @Schema(description = "List of assignments associated with the course")
    private List<AssignmentDTO> assignment = new ArrayList<>();

    @Builder.Default
    @Schema(description = "List of enrollments for the course")
    private List<EnrollmentDTO> enrollments = new ArrayList<>();

    @NotNull(message = "State must not be null")
    @Schema(description = "Current state of the course", example = "ACTIVE")
    private CourseState state;

    @Schema(description = "Timestamp of when the course record was created")
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp of the last update to the course record")
    private LocalDateTime lastUpdate;
}
