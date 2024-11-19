package org.una.programmingIII.UTEMP_Project.dtos;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    @JsonBackReference("university-faculties")  // Unique name for university reference
    @NotNull(message = "University must not be null")
    @Builder.Default
    private UniversityDTO university = new UniversityDTO();

    @JsonManagedReference("faculty-departments")  // Unique name for departments reference
    @Builder.Default
    private List<DepartmentDTO> departments = new ArrayList<>();

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSSSSS")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSSSSS")
    private LocalDateTime lastUpdate;

    @Override
    public String toString() {
        return "FacultyDTO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", university=" + (university != null ? "UniversityDTO{id=" + university.getId() + ", name=" + university.getName() + "}" : "null") +
                ", departments=" + (departments != null ? departments.size() + " departments" : "null") +
                ", createdAt=" + createdAt +
                ", lastUpdate=" + lastUpdate +
                '}';
    }


}

