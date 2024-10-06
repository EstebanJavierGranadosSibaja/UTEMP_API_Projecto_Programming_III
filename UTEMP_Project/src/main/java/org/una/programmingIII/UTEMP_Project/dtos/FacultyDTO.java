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
public class FacultyDTO {

    private Long id;

    @NotNull(message = "Faculty name must not be null")
    @Size(max = 50, message = "Faculty name must be at most 50 characters long")
    private String name;

    @NotNull(message = "University must not be null")
    @Builder.Default
    private UniversityDTO university = new UniversityDTO();

    @Builder.Default
    private List<DepartmentDTO> departments = new ArrayList<>();

    private LocalDateTime createdAt;

    private LocalDateTime lastUpdate;
}