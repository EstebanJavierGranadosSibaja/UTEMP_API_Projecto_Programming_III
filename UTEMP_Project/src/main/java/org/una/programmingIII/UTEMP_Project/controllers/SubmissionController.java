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
import org.una.programmingIII.UTEMP_Project.dtos.FileMetadatumDTO;
import org.una.programmingIII.UTEMP_Project.dtos.GradeDTO;
import org.una.programmingIII.UTEMP_Project.dtos.SubmissionDTO;
import org.una.programmingIII.UTEMP_Project.exceptions.InvalidDataException;
import org.una.programmingIII.UTEMP_Project.exceptions.ResourceNotFoundException;
import org.una.programmingIII.UTEMP_Project.services.submission.SubmissionService;

import java.util.Optional;

@RestController
@RequestMapping("/utemp/submissions")
public class SubmissionController {

    private final SubmissionService submissionService;
    private final Logger logger = LoggerFactory.getLogger(SubmissionController.class);

    @Autowired
    public SubmissionController(SubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    @GetMapping
    public ResponseEntity<Page<SubmissionDTO>> getAllSubmissions(Pageable pageable) {
        try {
            Page<SubmissionDTO> submissions = submissionService.getAllSubmissions(pageable);
            return ResponseEntity.ok(submissions);
        } catch (Exception e) {
            logger.error("Error retrieving submissions", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubmissionDTO> getSubmissionById(@PathVariable Long id) {
        try {
            Optional<SubmissionDTO> submissionDTO = submissionService.getSubmissionById(id);
            return submissionDTO.map(ResponseEntity::ok)
                    .orElseGet(() -> {
                        logger.warn("Submission not found with id: {}", id);
                        return ResponseEntity.notFound().build();
                    });
        } catch (Exception e) {
            logger.error("Error retrieving submission with id: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    public ResponseEntity<SubmissionDTO> createSubmission(@Valid @RequestBody SubmissionDTO submissionDTO) {
        try {
            SubmissionDTO createdSubmission = submissionService.createSubmission(submissionDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdSubmission);
        } catch (InvalidDataException e) {
            logger.warn("Invalid data for submission creation: {}", submissionDTO, e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error creating submission", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<SubmissionDTO> updateSubmission(@PathVariable Long id,
                                                          @Valid @RequestBody SubmissionDTO submissionDTO) {
        try {
            Optional<SubmissionDTO> updatedSubmission = submissionService.updateSubmission(id, submissionDTO);
            return updatedSubmission.map(ResponseEntity::ok)
                    .orElseGet(() -> {
                        logger.warn("Submission not found with id: {}", id);
                        return ResponseEntity.notFound().build();
                    });
        } catch (InvalidDataException e) {
            logger.warn("Invalid data for submission update: {}", submissionDTO, e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error updating submission with id: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSubmission(@PathVariable Long id) {
        try {
            submissionService.deleteSubmission(id);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            logger.warn("Submission not found with id: {}", id, e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error deleting submission with id: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/assignment/{assignmentId}")
    public ResponseEntity<Page<SubmissionDTO>> getSubmissionsByAssignmentId(@PathVariable Long assignmentId,
                                                                            Pageable pageable) {
        try {
            Page<SubmissionDTO> submissions = submissionService.getSubmissionsByAssignmentId(assignmentId, pageable);
            return ResponseEntity.ok(submissions);
        } catch (Exception e) {
            logger.error("Error retrieving submissions for assignment id: {}", assignmentId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{submissionId}/file-metadata")
    public ResponseEntity<FileMetadatumDTO> addFileMetadatumToSubmission(@PathVariable Long submissionId,
                                                                         @Valid @RequestBody FileMetadatumDTO fileMetadatumDTO) {
        try {
            FileMetadatumDTO createdFileMetadatum = submissionService.addFileMetadatumToSubmission(submissionId, fileMetadatumDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdFileMetadatum);
        } catch (InvalidDataException e) {
            logger.warn("Invalid data for adding file metadata to submission id: {}", submissionId, e);
            return ResponseEntity.badRequest().build();
        } catch (ResourceNotFoundException e) {
            logger.warn("Submission not found with id: {}", submissionId, e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error adding file metadata to submission id: {}", submissionId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{submissionId}/grades")
    public ResponseEntity<GradeDTO> addGradeToSubmission(@PathVariable Long submissionId,
                                                         @Valid @RequestBody GradeDTO gradeDTO) {
        try {
            GradeDTO createdGrade = submissionService.addGradeToSubmission(submissionId, gradeDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdGrade);
        } catch (InvalidDataException e) {
            logger.warn("Invalid data for adding grade to submission id: {}", submissionId, e);
            return ResponseEntity.badRequest().build();
        } catch (ResourceNotFoundException e) {
            logger.warn("Submission not found with id: {}", submissionId, e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error adding grade to submission id: {}", submissionId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{submissionId}/file-metadata/{fileMetadatumId}")
    public ResponseEntity<Void> removeFileMetadatumFromSubmission(@PathVariable Long submissionId,
                                                                  @PathVariable Long fileMetadatumId) {
        try {
            submissionService.removeFileMetadatumFromSubmission(submissionId, fileMetadatumId);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            logger.warn("File metadata not found for submission id: {}, fileMetadatumId: {}", submissionId, fileMetadatumId, e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error removing file metadata from submission id: {}", submissionId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{submissionId}/grades/{gradeId}")
    public ResponseEntity<Void> removeGradeFromSubmission(@PathVariable Long submissionId,
                                                          @PathVariable Long gradeId) {
        try {
            submissionService.removeGradeFromSubmission(submissionId, gradeId);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            logger.warn("Grade not found for submission id: {}, gradeId: {}", submissionId, gradeId, e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error removing grade from submission id: {}", submissionId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}