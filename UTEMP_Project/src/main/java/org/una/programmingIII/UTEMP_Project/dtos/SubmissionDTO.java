package org.una.programmingIII.UTEMP_Project.dtos;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.una.programmingIII.UTEMP_Project.models.SubmissionState;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmissionDTO {

    private Long id;

    @NotNull(message = "Assignment must not be null")
    @Builder.Default
    @JsonBackReference("assignment-submissions")  // Unique name for assignment reference
    private AssignmentDTO assignment = new AssignmentDTO();
    @NotNull(message = "Student must not be null")
    @Builder.Default
    @JsonBackReference("user-submissions")
    private UserDTO student = new UserDTO();

    private String asignaciontitle;
    private long asignaciontId;
    private String studentUniqueName;
    private Long studeId;
    private Long metadataID;

    @NotBlank(message = "File name must not be blank")
    @Size(max = 255, message = "File name must be at most 255 characters long")
    private String fileName;

    private Double grade;

    @Size(max = 500, message = "Comments must be at most 500 characters long")
    private String comments;

    @NotNull(message = "State must not be null")
    private SubmissionState state;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSSSSS")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSSSSS")
    private LocalDateTime lastUpdate;

    @Builder.Default
    @JsonIgnore
    @JsonBackReference("submission-grades")  // Unique name for grades reference
    private List<GradeDTO> grades = new ArrayList<>();

    @Builder.Default
    @JsonIgnore
    @JsonBackReference("submission-fileMetadata")  // Unique name for fileMetadata reference
    private List<FileMetadatumDTO> fileMetadata = new ArrayList<>();

    @Override
    public String toString() {
        return "SubmissionDTO{" +
                "id=" + id +
                ", fileName='" + fileName + '\'' +
                ", grade=" + grade +
                ", comments='" + comments + '\'' +
                ", state=" + state +
                ", createdAt=" + createdAt +
                ", lastUpdate=" + lastUpdate +
                ", studentId=" + (student != null ? student.getId() : "null") +
                ", assignmentId=" + (assignment != null ? assignment.getId() : "null") +
                '}';
    }
}
