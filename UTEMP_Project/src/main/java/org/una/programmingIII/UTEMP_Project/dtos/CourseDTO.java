package org.una.programmingIII.UTEMP_Project.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.una.programmingIII.UTEMP_Project.models.CourseState;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseDTO {

    private Long id;

    @NotNull(message = "Course name must not be null")
    @Size(max = 50, message = "Course name must not exceed 50 characters")
    private String name;

    @NotNull(message = "Description must not be null")
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @NotNull(message = "Teacher must not be null")
    private UserDTO teacher;

    @NotNull(message = "Department must not be null")
    private DepartmentDTO department = new DepartmentDTO();

    private List<AssignmentDTO> assignment = new ArrayList<>();

    private List<EnrollmentDTO> enrollments = new ArrayList<>();

    @NotNull(message = "State must not be null")
    private CourseState state;

    private LocalDateTime createdAt;

    private LocalDateTime lastUpdate;
}