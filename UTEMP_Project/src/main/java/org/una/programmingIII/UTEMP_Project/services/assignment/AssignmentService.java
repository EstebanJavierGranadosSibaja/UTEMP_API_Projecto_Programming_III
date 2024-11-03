package org.una.programmingIII.UTEMP_Project.services.assignment;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.una.programmingIII.UTEMP_Project.dtos.AssignmentDTO;
import org.una.programmingIII.UTEMP_Project.dtos.SubmissionDTO;

import java.util.Optional;

public interface AssignmentService {
    Page<AssignmentDTO> getAllAssignments(Pageable pageable);

    Optional<AssignmentDTO> getAssignmentById(Long id);

    AssignmentDTO createAssignment(@Valid AssignmentDTO assignmentDTO);

    Optional<AssignmentDTO> updateAssignment(Long id, @Valid AssignmentDTO assignmentDTO);

    void deleteAssignment(Long id);

    Page<AssignmentDTO> getAssignmentsByCourseId(Long courseId, Pageable pageable);

    SubmissionDTO addSubmissionToAssignment(Long assignmentId, @Valid SubmissionDTO submissionDTO);

    void removeSubmissionFromAssignment(Long assignmentId, Long submissionId);
}