package org.una.programmingIII.UTEMP_Project.services.assignment;

import jakarta.validation.Valid;
import org.una.programmingIII.UTEMP_Project.dtos.AssignmentDTO;
import org.una.programmingIII.UTEMP_Project.dtos.SubmissionDTO;

import java.util.List;
import java.util.Optional;

public interface AssignmentService {
    List<AssignmentDTO> getAllAssignments();
    Optional<AssignmentDTO> getAssignmentById(Long id);
    AssignmentDTO createAssignment(@Valid AssignmentDTO assignmentDTO);
    Optional<AssignmentDTO> updateAssignment(Long id, @Valid AssignmentDTO assignmentDTO);
    void deleteAssignment(Long id);
    List<AssignmentDTO> getAssignmentsByCourseId(Long courseId);
    List<SubmissionDTO> getSubmissionsByAssignmentId(Long assignmentId);
    SubmissionDTO addSubmissionToAssignment(Long assignmentId, @Valid SubmissionDTO submissionDTO);
    void deleteSubmissionFromAssignment(Long assignmentId, Long submissionId);
}
