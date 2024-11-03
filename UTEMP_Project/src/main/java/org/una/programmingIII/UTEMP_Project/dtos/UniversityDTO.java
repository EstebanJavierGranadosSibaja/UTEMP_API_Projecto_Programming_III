package org.una.programmingIII.UTEMP_Project.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
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
public class UniversityDTO {

    @Schema(description = "Unique identifier for the university", example = "1")
    private Long id;

    @NotBlank(message = "University name must not be blank")
    @Size(max = 100, message = "University name must be at most 100 characters long")
    @Schema(description = "Name of the university", example = "National University")
    private String name;

    @Size(max = 200, message = "Location must be at most 200 characters long")
    @Schema(description = "Location of the university", example = "123 University Ave, City, Country")
    private String location;

    @Builder.Default
    @Schema(description = "List of faculties associated with the university")
    private List<FacultyDTO> faculties = new ArrayList<>();

    @Schema(description = "Timestamp of when the university was created")
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp of the last update to the university record")
    private LocalDateTime lastUpdate;
}
