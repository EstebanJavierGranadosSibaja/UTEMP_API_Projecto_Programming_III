package org.una.programmingIII.UTEMP_Project.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.una.programmingIII.UTEMP_Project.models.GradeState;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GradeDTO {

    @Schema(description = "Unique identifier for the grade", example = "1")
    private Long id;

    @NotNull(message = "Submission must not be null")
    @Builder.Default
    @Schema(description = "Submission associated with this grade")
    private SubmissionDTO submission = new SubmissionDTO();

    @Schema(description = "The grade awarded", example = "95.0")
    private Double grade;

    @Size(max = 1000, message = "Comments must be at most 1000 characters long")
    @Schema(description = "Comments related to the grade", example = "Great work on the assignment!")
    private String comments;

    @Schema(description = "Indicates if the grade was reviewed by AI", example = "true")
    private Boolean reviewedByAi;

    @NotNull(message = "State must not be null")
    @Schema(description = "Current state of the grade")
    private GradeState state;

    @Schema(description = "Timestamp of when the grade was created")
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp of the last update to the grade")
    private LocalDateTime lastUpdate;

}
