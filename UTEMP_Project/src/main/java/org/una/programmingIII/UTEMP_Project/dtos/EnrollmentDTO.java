package org.una.programmingIII.UTEMP_Project.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.una.programmingIII.UTEMP_Project.models.EnrollmentState;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrollmentDTO {

    @Schema(description = "Unique identifier for the enrollment", example = "1")
    private Long id;

    @NotNull(message = "Course must not be null")
    @Builder.Default
    @Schema(description = "Course associated with the enrollment")
    private CourseDTO course = new CourseDTO();

    @NotNull(message = "Student must not be null")
    @Builder.Default
    @Schema(description = "Student enrolled in the course")
    private UserDTO student = new UserDTO();

    @NotNull(message = "State must not be null")
    @Schema(description = "Current state of the enrollment", example = "ACTIVE")
    private EnrollmentState state;

    @Schema(description = "Timestamp of when the enrollment record was created")
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp of the last update to the enrollment record")
    private LocalDateTime lastUpdate;
}
