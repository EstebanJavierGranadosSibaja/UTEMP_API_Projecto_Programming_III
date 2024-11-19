package org.una.programmingIII.UTEMP_Project.dtos;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
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
    @JsonBackReference("submission-grades")
    private SubmissionDTO submission = new SubmissionDTO();

    private Double grade;

    @Size(max = 1000, message = "Comments must be at most 1000 characters long")
    private String comments;

    private Boolean reviewedByAi;

    @NotNull(message = "State must not be null")
    private GradeState state;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSSSSS")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSSSSS")
    private LocalDateTime lastUpdate;
}
