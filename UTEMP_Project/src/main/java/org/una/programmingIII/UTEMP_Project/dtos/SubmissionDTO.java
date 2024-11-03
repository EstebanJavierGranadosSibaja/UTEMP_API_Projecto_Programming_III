package org.una.programmingIII.UTEMP_Project.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.una.programmingIII.UTEMP_Project.models.SubmissionState;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmissionDTO {

    @Schema(description = "Unique identifier for the submission", example = "1")
    private Long id;

    @NotNull(message = "Assignment must not be null")
    @Schema(description = "The assignment related to this submission")
    @Builder.Default
    private AssignmentDTO assignment = new AssignmentDTO();

    @NotNull(message = "Student must not be null")
    @Schema(description = "The student who made the submission")
    @Builder.Default
    private UserDTO student = new UserDTO();

    @NotBlank(message = "File name must not be blank")
    @Size(max = 255, message = "File name must be at most 255 characters long")
    @Schema(description = "Name of the submitted file", example = "assignment1.pdf")
    private String fileName;

    @Schema(description = "Grade awarded for the submission", example = "85.5")
    private Double grade;

    @Size(max = 500, message = "Comments must be at most 500 characters long")
    @Schema(description = "Comments regarding the submission", example = "Good work, but improve the formatting.")
    private String comments;

    @Builder.Default
    @Schema(description = "List of grades associated with this submission")
    private List<GradeDTO> grades = new ArrayList<>();

    @Builder.Default
    @Schema(description = "List of file metadata associated with this submission")
    private List<FileMetadatumDTO> fileMetadata = new ArrayList<>();

    @NotNull(message = "State must not be null")
    @Schema(description = "Current state of the submission", example = "SUBMITTED")
    private SubmissionState state;

    @Schema(description = "Timestamp of when the submission was created")
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp of the last update to the submission")
    private LocalDateTime lastUpdate;
}