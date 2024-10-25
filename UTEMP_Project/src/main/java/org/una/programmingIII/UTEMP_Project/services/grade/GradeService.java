package org.una.programmingIII.UTEMP_Project.services.grade;

import jakarta.validation.Valid;
import org.una.programmingIII.UTEMP_Project.dtos.GradeDTO;

import java.util.List;
import java.util.Optional;

public interface GradeService {
    List<GradeDTO> getAllGrades();
    Optional<GradeDTO> getGradeById(Long id);
    GradeDTO createGrade(@Valid GradeDTO gradeDTO);
    Optional<GradeDTO> updateGrade(Long id, @Valid GradeDTO gradeDTO);
    void deleteGrade(Long id);
    List<GradeDTO> getGradesBySubmissionId(Long submissionId);
}
