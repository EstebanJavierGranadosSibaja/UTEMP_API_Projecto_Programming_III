package org.una.programmingIII.UTEMP_Project.services.submission;

import jakarta.validation.Valid;
import org.una.programmingIII.UTEMP_Project.dtos.FileMetadatumDTO;
import org.una.programmingIII.UTEMP_Project.dtos.GradeDTO;
import org.una.programmingIII.UTEMP_Project.dtos.SubmissionDTO;

import java.util.List;
import java.util.Optional;

public interface SubmissionService {
    List<SubmissionDTO> getAllSubmissions();
    Optional<SubmissionDTO> getSubmissionById(Long id);
    SubmissionDTO createSubmission(@Valid SubmissionDTO submissionDTO);
    Optional<SubmissionDTO> updateSubmission(Long id, @Valid SubmissionDTO submissionDTO);
    void deleteSubmission(Long id);
    List<SubmissionDTO> getSubmissionsByAssignmentId(Long assignmentId);
    FileMetadatumDTO addFileMetadatumToSubmission(Long submissionId, @Valid FileMetadatumDTO fileMetadatumDTO);
    GradeDTO addGradeToSubmission(Long submissionId, @Valid GradeDTO gradeDTO);
    void removeFileMetadatumFromSubmission(Long submissionId, Long fileMetadatumId);
    void removeGradeFromSubmission(Long submissionId, Long gradeId);
}
