package org.una.programmingIII.UTEMP_Project.controllers;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.una.programmingIII.UTEMP_Project.dtos.AssignmentDTO;
import org.una.programmingIII.UTEMP_Project.dtos.SubmissionDTO;
import org.una.programmingIII.UTEMP_Project.responses.ApiResponse;
import org.una.programmingIII.UTEMP_Project.services.AssignmentServices.AssignmentService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/assignments")
public class AssignmentController {

    private final AssignmentService assignmentService;

    @Autowired
    public AssignmentController(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    // CRUD básico
    @GetMapping
    @PreAuthorize("hasAuthority('MANAGE_ASSIGNMENTS')")
    public ResponseEntity<ApiResponse<List<AssignmentDTO>>> getAllAssignments() {
        List<AssignmentDTO> assignments = assignmentService.getAllAssignments();
        ApiResponse<List<AssignmentDTO>> response = new ApiResponse<>(assignments);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_ASSIGNMENTS') or hasAuthority('VIEW_ASSIGNMENTS')")
    public ResponseEntity<ApiResponse<AssignmentDTO>> getAssignmentById(@PathVariable Long id) {
        Optional<AssignmentDTO> assignmentDTO = assignmentService.getAssignmentById(id);
        return getApiResponseResponseEntity(assignmentDTO);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('MANAGE_ASSIGNMENTS')")
    public ResponseEntity<ApiResponse<AssignmentDTO>> createAssignment(@Valid @RequestBody AssignmentDTO assignmentDTO) {
        AssignmentDTO createdAssignment = assignmentService.createAssignment(assignmentDTO);
        ApiResponse<AssignmentDTO> response = new ApiResponse<>(createdAssignment);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_ASSIGNMENTS')")
    public ResponseEntity<ApiResponse<AssignmentDTO>> updateAssignment(@PathVariable Long id, @Valid @RequestBody AssignmentDTO assignmentDTO) {
        Optional<AssignmentDTO> updatedAssignment = assignmentService.updateAssignment(id, assignmentDTO);
        return getApiResponseResponseEntity(updatedAssignment);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_ASSIGNMENTS')")
    public ResponseEntity<ApiResponse<Boolean>> deleteAssignment(@PathVariable Long id) {
        assignmentService.deleteAssignment(id);
        ApiResponse<Boolean> response = new ApiResponse<>(true);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
    }

    // Métodos adicionales relacionados con asignaciones
    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasAuthority('VIEW_ASSIGNMENTS')")
    public ResponseEntity<ApiResponse<List<AssignmentDTO>>> getAssignmentsByCourseId(@PathVariable Long courseId) {
        List<AssignmentDTO> assignments = assignmentService.getAssignmentsByCourseId(courseId);
        ApiResponse<List<AssignmentDTO>> response = new ApiResponse<>(assignments);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{assignmentId}/submissions")
    @PreAuthorize("hasAuthority('VIEW_SUBMISSIONS')")
    public ResponseEntity<ApiResponse<List<SubmissionDTO>>> getSubmissionsByAssignmentId(@PathVariable Long assignmentId) {
        List<SubmissionDTO> submissions = assignmentService.getSubmissionsByAssignmentId(assignmentId);
        ApiResponse<List<SubmissionDTO>> response = new ApiResponse<>(submissions);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{assignmentId}/submissions")
    @PreAuthorize("hasAuthority('MANAGE_SUBMISSIONS')")
    public ResponseEntity<ApiResponse<SubmissionDTO>> addSubmissionToAssignment(@PathVariable Long assignmentId, @Valid @RequestBody SubmissionDTO submissionDTO) {
        SubmissionDTO createdSubmission = assignmentService.addSubmissionToAssignment(assignmentId, submissionDTO);
        ApiResponse<SubmissionDTO> response = new ApiResponse<>(createdSubmission);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{assignmentId}/submissions/{submissionId}")
    @PreAuthorize("hasAuthority('MANAGE_SUBMISSIONS')")
    public ResponseEntity<ApiResponse<String>> deleteSubmissionFromAssignment(@PathVariable Long assignmentId, @PathVariable Long submissionId) {
        assignmentService.deleteSubmissionFromAssignment(assignmentId, submissionId);
        ApiResponse<String> response = new ApiResponse<>("Submission deleted successfully");
        return ResponseEntity.ok(response);
    }

    private ResponseEntity<ApiResponse<AssignmentDTO>> getApiResponseResponseEntity(Optional<AssignmentDTO> assignmentDTO) {
        return assignmentDTO.map(assignment -> {
            ApiResponse<AssignmentDTO> response = new ApiResponse<>(assignment);
            return ResponseEntity.ok(response);
        }).orElseGet(() -> {
            ApiResponse<AssignmentDTO> response = new ApiResponse<>(HttpStatus.NOT_FOUND.value(), "Assignment not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        });
    }
}
