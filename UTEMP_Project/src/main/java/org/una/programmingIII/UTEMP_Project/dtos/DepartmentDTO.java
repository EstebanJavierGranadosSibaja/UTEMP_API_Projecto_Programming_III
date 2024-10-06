package org.una.programmingIII.UTEMP_Project.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepartmentDTO {

    private Long id;

    @NotNull(message = "Department name must not be null")
    @Size(max = 50, message = "Department name must not exceed 50 characters")
    private String name;

    @NotNull(message = "Faculty must not be null")
    private FacultyDTO faculty = new FacultyDTO();

    private List<CourseDTO> courses = new ArrayList<>();

    private LocalDateTime createdAt;

    private LocalDateTime lastUpdate;
}