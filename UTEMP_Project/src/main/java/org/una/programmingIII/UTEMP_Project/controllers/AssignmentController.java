package org.una.programmingIII.UTEMP_Project.controllers;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.una.programmingIII.UTEMP_Project.dtos.AssignmentDTO;
import org.una.programmingIII.UTEMP_Project.dtos.SubmissionDTO;
import org.una.programmingIII.UTEMP_Project.services.assignment.AssignmentService;
import org.una.programmingIII.UTEMP_Project.exceptions.ResourceNotFoundException;
import org.una.programmingIII.UTEMP_Project.exceptions.InvalidDataException;
import org.una.programmingIII.UTEMP_Project.exceptions.ResourceAlreadyExistsException;

@RestController
@RequestMapping("/utemp/assignments")
public class AssignmentController {

    @Autowired
    private AssignmentService assignmentService;

    @GetMapping
    public ResponseEntity<Page<AssignmentDTO>> getAllAssignments(Pageable pageable) {
        Page<AssignmentDTO> assignments = assignmentService.getAllAssignments(pageable);
        return ResponseEntity.ok(assignments);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AssignmentDTO> getAssignmentById(@PathVariable Long id) {
        try {
            return assignmentService.getAssignmentById(id)
                    .map(ResponseEntity::ok)
                    .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with id: " , id));
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidDataException("Error retrieving assignment: " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<AssignmentDTO> createAssignment(@Valid @RequestBody AssignmentDTO assignmentDTO) {
        try {
            AssignmentDTO createdAssignment = assignmentService.createAssignment(assignmentDTO);
            return ResponseEntity.status(201).body(createdAssignment);
        } catch (ResourceAlreadyExistsException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidDataException("Error creating assignment: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<AssignmentDTO> updateAssignment(@PathVariable Long id, @Valid @RequestBody AssignmentDTO assignmentDTO) {
        try {
            return assignmentService.updateAssignment(id, assignmentDTO)
                    .map(ResponseEntity::ok)
                    .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with id: ", id));
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidDataException("Error updating assignment: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAssignment(@PathVariable Long id) {
        try {
            assignmentService.deleteAssignment(id);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidDataException("Error deleting assignment: " + e.getMessage());
        }
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<Page<AssignmentDTO>> getAssignmentsByCourseId(@PathVariable Long courseId, Pageable pageable) {
        Page<AssignmentDTO> assignments = assignmentService.getAssignmentsByCourseId(courseId, pageable);
        return ResponseEntity.ok(assignments);
    }

    @GetMapping("/{assignmentId}/submissions")
    public ResponseEntity<Page<SubmissionDTO>> getSubmissionsByAssignmentId(@PathVariable Long assignmentId, Pageable pageable) {
        Page<SubmissionDTO> submissions = assignmentService.getSubmissionsByAssignmentId(assignmentId, pageable);
        return ResponseEntity.ok(submissions);
    }

    @PostMapping("/{assignmentId}/submissions")
    public ResponseEntity<SubmissionDTO> addSubmissionToAssignment(@PathVariable Long assignmentId, @Valid @RequestBody SubmissionDTO submissionDTO) {
        try {
            SubmissionDTO createdSubmission = assignmentService.addSubmissionToAssignment(assignmentId, submissionDTO);
            return ResponseEntity.status(201).body(createdSubmission);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidDataException("Error adding submission to assignment: " + e.getMessage());
        }
    }

    @DeleteMapping("/{assignmentId}/submissions/{submissionId}")
    public ResponseEntity<Void> deleteSubmissionFromAssignment(@PathVariable Long assignmentId, @PathVariable Long submissionId) {
        try {
            assignmentService.deleteSubmissionFromAssignment(assignmentId, submissionId);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidDataException("Error deleting submission from assignment: " + e.getMessage());
        }
    }
}