package org.una.programmingIII.UTEMP_Project.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UniversityDTO {

    private Long id;

    @NotBlank(message = "University name must not be blank")
    @Size(max = 100, message = "University name must be at most 100 characters long")
    private String name;

    @Size(max = 200, message = "Location must be at most 200 characters long")
    private String location;

    @Builder.Default
    private List<FacultyDTO> faculties = new ArrayList<>();

    private LocalDateTime createdAt;

    private LocalDateTime lastUpdate;
}
