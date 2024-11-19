package org.una.programmingIII.UTEMP_Project.dtos;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
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

    private Long id;

    @NotNull(message = "Course must not be null")
    @Builder.Default
    @JsonBackReference("course-enrollments")  // Unique name for course reference
    private CourseDTO course = new CourseDTO();

    private Long courseId;

    @NotNull(message = "Student must not be null")
    @Builder.Default
    @JsonBackReference("user-enrollments")  // Unique name for student reference
    private UserDTO student = new UserDTO();

    private Long studentId;

    @NotNull(message = "State must not be null")
    private EnrollmentState state;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSSSSS")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSSSSS")
    private LocalDateTime lastUpdate;
}

