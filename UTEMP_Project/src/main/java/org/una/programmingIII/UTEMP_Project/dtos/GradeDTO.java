package org.una.programmingIII.UTEMP_Project.dtos;

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

    private Long id;

    @NotNull(message = "Submission must not be null")
    @Builder.Default
    private SubmissionDTO submission = new SubmissionDTO();

    private Double grade;

    @Size(max = 1000, message = "Comments must be at most 1000 characters long")
    private String comments;

    private Boolean reviewedByAi;

    @NotNull(message = "State must not be null")
    private GradeState state;

    private LocalDateTime createdAt;

    private LocalDateTime lastUpdate;
}
