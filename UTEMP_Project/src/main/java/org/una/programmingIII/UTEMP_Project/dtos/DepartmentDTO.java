package org.una.programmingIII.UTEMP_Project.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepartmentDTO {

    @Schema(description = "Unique identifier for the department", example = "1")
    private Long id;

    @NotNull(message = "Department name must not be null")
    @Size(max = 50, message = "Department name must not exceed 50 characters")
    @Schema(description = "Name of the department", example = "Computer Science")
    private String name;

    @NotNull(message = "Faculty must not be null")
    @Builder.Default
    @Schema(description = "Faculty to which the department belongs")
    private FacultyDTO faculty = new FacultyDTO();

    @Builder.Default
    @Schema(description = "List of courses offered by the department")
    private List<CourseDTO> courses = new ArrayList<>();

    @Schema(description = "Timestamp of when the department record was created")
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp of the last update to the department record")
    private LocalDateTime lastUpdate;
}
