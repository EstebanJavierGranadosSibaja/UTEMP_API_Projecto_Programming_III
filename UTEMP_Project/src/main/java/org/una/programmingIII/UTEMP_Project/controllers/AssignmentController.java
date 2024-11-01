package org.una.programmingIII.UTEMP_Project.controllers;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.una.programmingIII.UTEMP_Project.dtos.AssignmentDTO;
import org.una.programmingIII.UTEMP_Project.dtos.SubmissionDTO;
import org.una.programmingIII.UTEMP_Project.exceptions.InvalidDataException;
import org.una.programmingIII.UTEMP_Project.exceptions.ResourceNotFoundException;
import org.una.programmingIII.UTEMP_Project.services.assignment.AssignmentService;

import java.util.Optional;

@RestController
@RequestMapping("/utemp/assignments")
public class AssignmentController {

    private static final Logger logger = LoggerFactory.getLogger(AssignmentController.class);

    private final AssignmentService assignmentService;

    @Autowired
    public AssignmentController(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    @GetMapping
    public ResponseEntity<Page<AssignmentDTO>> getAllAssignments(Pageable pageable) {
        try {
            Page<AssignmentDTO> assignments = assignmentService.getAllAssignments(pageable);
            return ResponseEntity.ok(assignments);
        } catch (InvalidDataException e) {
            logger.error("Invalid data fetching assignments: {}", e.getMessage());
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            logger.error("Error fetching all assignments: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<AssignmentDTO> getAssignmentById(@PathVariable Long id) {
        try {
            Optional<AssignmentDTO> assignmentDTO = assignmentService.getAssignmentById(id);
            return assignmentDTO
                    .map(ResponseEntity::ok)
                    .orElseThrow(() -> new ResourceNotFoundException("Assignment", id));
        } catch (ResourceNotFoundException e) {
            logger.error("Assignment not found with ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            logger.error("Unexpected error retrieving assignment with ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping
    public ResponseEntity<AssignmentDTO> createAssignment(@Valid @RequestBody AssignmentDTO assignmentDTO) {
        try {
            AssignmentDTO createdAssignment = assignmentService.createAssignment(assignmentDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdAssignment);
        } catch (InvalidDataException e) {
            logger.error("Invalid data for creating assignment: {}", e.getMessage());
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            logger.error("Unexpected error creating assignment: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<AssignmentDTO> updateAssignment(@PathVariable Long id,
                                                          @Valid @RequestBody AssignmentDTO assignmentDTO) {
        try {
            Optional<AssignmentDTO> updatedAssignment = assignmentService.updateAssignment(id, assignmentDTO);
            return updatedAssignment
                    .map(ResponseEntity::ok)
                    .orElseThrow(() -> new ResourceNotFoundException("Assignment", id));
        } catch (ResourceNotFoundException e) {
            logger.error("Assignment not found with ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (InvalidDataException e) {
            logger.error("Invalid data for updating assignment ID {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            logger.error("Unexpected error updating assignment ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAssignment(@PathVariable Long id) {
        try {
            assignmentService.deleteAssignment(id);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            logger.error("Error deleting assignment with ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            logger.error("Unexpected error deleting assignment with ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<Page<AssignmentDTO>> getAssignmentsByCourseId(@PathVariable Long courseId,
                                                                        Pageable pageable) {
        try {
            Page<AssignmentDTO> assignments = assignmentService.getAssignmentsByCourseId(courseId, pageable);
            return ResponseEntity.ok(assignments);
        } catch (InvalidDataException e) {
            logger.error("Invalid data fetching assignments for course ID {}: {}", courseId, e.getMessage());
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            logger.error("Error fetching assignments for course ID {}: {}", courseId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/{assignmentId}/submissions")
    public ResponseEntity<SubmissionDTO> addSubmissionToAssignment(@PathVariable Long assignmentId,
                                                                   @Valid @RequestBody SubmissionDTO submissionDTO) {
        try {
            SubmissionDTO createdSubmission = assignmentService.addSubmissionToAssignment(assignmentId, submissionDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdSubmission);
        } catch (ResourceNotFoundException e) {
            logger.error("Assignment not found for submission addition: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (InvalidDataException e) {
            logger.error("Invalid submission data: {}", e.getMessage());
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            logger.error("Unexpected error adding submission: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @DeleteMapping("/{assignmentId}/submissions/{submissionId}")
    public ResponseEntity<Void> deleteSubmissionFromAssignment(@PathVariable Long assignmentId,
                                                               @PathVariable Long submissionId) {
        try {
            assignmentService.deleteSubmissionFromAssignment(assignmentId, submissionId);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            logger.error("Submission not found for deletion: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            logger.error("Unexpected error deleting submission: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}