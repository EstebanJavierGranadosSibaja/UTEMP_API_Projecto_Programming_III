package org.una.programmingIII.UTEMP_Project.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.una.programmingIII.UTEMP_Project.models.AssignmentState;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignmentDTO {

    @Schema(description = "Unique identifier for the assignment", example = "1")
    private Long id;

    @NotNull(message = "Title must not be null")
    @Size(max = 100, message = "Title must not exceed 100 characters")
    @Schema(description = "Title of the assignment", example = "Midterm Project")
    private String title;

    @NotNull(message = "Description must not be null")
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    @Schema(description = "Detailed description of the assignment", example = "This project includes various topics covered in the first half of the semester.")
    private String description;

    @Schema(description = "Deadline for the assignment submission", example = "2024-12-01T23:59:59Z")
    private Instant deadline;

    @NotNull(message = "Course must not be null")
    @Builder.Default
    @Schema(description = "Course associated with the assignment")
    private CourseDTO course = new CourseDTO();

    @Builder.Default
    @Schema(description = "List of submissions for the assignment")
    private List<SubmissionDTO> submissions = new ArrayList<>();

    @NotNull(message = "State must not be null")
    @Schema(description = "Current state of the assignment", example = "ACTIVE")
    private AssignmentState state;

    @Schema(description = "Timestamp of when the assignment record was created")
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp of the last update to the assignment record")
    private LocalDateTime lastUpdate;
}