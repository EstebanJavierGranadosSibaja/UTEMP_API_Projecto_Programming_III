package org.una.programmingIII.UTEMP_Project.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
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
public class FacultyDTO {

    @Schema(description = "Unique identifier for the faculty", example = "1")
    private Long id;

    @NotNull(message = "Faculty name must not be null")
    @Size(max = 50, message = "Faculty name must be at most 50 characters long")
    @Schema(description = "Name of the faculty", example = "Faculty of Science")
    private String name;

    @NotNull(message = "University must not be null")
    @Builder.Default
    @Schema(description = "University associated with the faculty")
    private UniversityDTO university = new UniversityDTO();

    @Builder.Default
    @Schema(description = "List of departments under this faculty")
    private List<DepartmentDTO> departments = new ArrayList<>();

    @Schema(description = "Timestamp of when the faculty record was created")
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp of the last update to the faculty record")
    private LocalDateTime lastUpdate;
}
