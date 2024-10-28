package org.una.programmingIII.UTEMP_Project.services.grade;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.una.programmingIII.UTEMP_Project.dtos.GradeDTO;

import java.util.List;
import java.util.Optional;

public interface GradeService {
    Page<GradeDTO> getAllGrades(Pageable pageable);
    Optional<GradeDTO> getGradeById(Long id);
    GradeDTO createGrade(@Valid GradeDTO gradeDTO);
    Optional<GradeDTO> updateGrade(Long id, @Valid GradeDTO gradeDTO);
    void deleteGrade(Long id);
    Page<GradeDTO> getGradesBySubmissionId(Long submissionId, Pageable pageable);
}
