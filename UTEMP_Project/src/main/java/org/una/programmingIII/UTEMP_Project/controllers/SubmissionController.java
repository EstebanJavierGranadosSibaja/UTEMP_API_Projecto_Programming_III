package org.una.programmingIII.UTEMP_Project.controllers;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.una.programmingIII.UTEMP_Project.dtos.SubmissionDTO;
import org.una.programmingIII.UTEMP_Project.dtos.FileMetadatumDTO;
import org.una.programmingIII.UTEMP_Project.dtos.GradeDTO;
import org.una.programmingIII.UTEMP_Project.services.submission.SubmissionService;
import org.una.programmingIII.UTEMP_Project.exceptions.ResourceNotFoundException;
import org.una.programmingIII.UTEMP_Project.exceptions.InvalidDataException;

import java.util.Optional;

@RestController
@RequestMapping("/utemp/submissions")
public class SubmissionController {

    @Autowired
    private SubmissionService submissionService;

    @GetMapping
    public ResponseEntity<Page<SubmissionDTO>> getAllSubmissions(Pageable pageable) {
        try {
            Page<SubmissionDTO> submissions = submissionService.getAllSubmissions(pageable);
            return new ResponseEntity<>(submissions, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubmissionDTO> getSubmissionById(@PathVariable Long id) {
        try {
            Optional<SubmissionDTO> submission = submissionService.getSubmissionById(id);
            return submission.map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping
    public ResponseEntity<SubmissionDTO> createSubmission(@Valid @RequestBody SubmissionDTO submissionDTO) {
        try {
            SubmissionDTO createdSubmission = submissionService.createSubmission(submissionDTO);
            return new ResponseEntity<>(createdSubmission, HttpStatus.CREATED);
        } catch (InvalidDataException e) {
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<SubmissionDTO> updateSubmission(@PathVariable Long id,
                                                          @Valid @RequestBody SubmissionDTO submissionDTO) {
        try {
            Optional<SubmissionDTO> updatedSubmission = submissionService.updateSubmission(id, submissionDTO);
            return updatedSubmission.map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (InvalidDataException e) {
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSubmission(@PathVariable Long id) {
        try {
            submissionService.deleteSubmission(id);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/{submissionId}/file-metadata")
    public ResponseEntity<FileMetadatumDTO> addFileMetadatumToSubmission(@PathVariable Long submissionId,
                                                                         @Valid @RequestBody FileMetadatumDTO fileMetadatumDTO) {
        try {
            FileMetadatumDTO addedFileMetadatum = submissionService.addFileMetadatumToSubmission(submissionId, fileMetadatumDTO);
            return new ResponseEntity<>(addedFileMetadatum, HttpStatus.CREATED);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (InvalidDataException e) {
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/{submissionId}/grades")
    public ResponseEntity<GradeDTO> addGradeToSubmission(@PathVariable Long submissionId,
                                                         @Valid @RequestBody GradeDTO gradeDTO) {
        try {
            GradeDTO addedGrade = submissionService.addGradeToSubmission(submissionId, gradeDTO);
            return new ResponseEntity<>(addedGrade, HttpStatus.CREATED);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (InvalidDataException e) {
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{submissionId}/file-metadata/{fileMetadatumId}")
    public ResponseEntity<Void> removeFileMetadatumFromSubmission(@PathVariable Long submissionId,
                                                                  @PathVariable Long fileMetadatumId) {
        try {
            submissionService.removeFileMetadatumFromSubmission(submissionId, fileMetadatumId);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{submissionId}/grades/{gradeId}")
    public ResponseEntity<Void> removeGradeFromSubmission(@PathVariable Long submissionId,
                                                          @PathVariable Long gradeId) {
        try {
            submissionService.removeGradeFromSubmission(submissionId, gradeId);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}