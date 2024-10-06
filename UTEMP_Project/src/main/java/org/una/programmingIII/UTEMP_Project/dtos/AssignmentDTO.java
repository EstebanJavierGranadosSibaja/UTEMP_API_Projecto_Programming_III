package org.una.programmingIII.UTEMP_Project.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
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

    private Long id;

    @NotNull(message = "Title must not be null")
    @Size(max = 100, message = "Title must not exceed 100 characters")
    private String title;

    @NotNull(message = "Description must not be null")
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    private Instant deadline;

    @NotNull(message = "Course must not be null")
    private CourseDTO course;

    private List<FileMetadatumDTO> fileMetadata = new ArrayList<>();

    private List<SubmissionDTO> submissions = new ArrayList<>();

    @NotNull(message = "State must not be null")
    private AssignmentState state;

    private LocalDateTime createdAt;

    private LocalDateTime lastUpdate;
}
