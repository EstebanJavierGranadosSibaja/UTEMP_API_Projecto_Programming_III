package org.una.programmingIII.UTEMP_Project.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.una.programmingIII.UTEMP_Project.dtos.GradeDTO;
import org.una.programmingIII.UTEMP_Project.services.grade.GradeService;
import org.una.programmingIII.UTEMP_Project.exceptions.InvalidDataException;
import org.una.programmingIII.UTEMP_Project.exceptions.ResourceNotFoundException;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/utemp/grades")
public class GradeController {

    private final GradeService gradeService;
    private static final Logger logger = LoggerFactory.getLogger(GradeController.class);

    @Autowired
    public GradeController(GradeService gradeService) {
        this.gradeService = gradeService;
    }

    @GetMapping
    public ResponseEntity<Page<GradeDTO>> getAllGrades(Pageable pageable) {
        try {
            Page<GradeDTO> grades = gradeService.getAllGrades(pageable);
            logger.info("Fetched all grades successfully.");
            return ResponseEntity.ok(grades);
        } catch (InvalidDataException e) {
            logger.error("Invalid data while fetching grades: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error while fetching grades: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<GradeDTO> getGradeById(@PathVariable Long id) {
        try {
            return gradeService.getGradeById(id)
                    .map(gradeDTO -> {
                        logger.info("Fetched grade with ID: {}", id);
                        return ResponseEntity.ok(gradeDTO);
                    })
                    .orElseGet(() -> {
                        logger.warn("Grade with ID: {} not found.", id);
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
                    });
        } catch (Exception e) {
            logger.error("Error while fetching grade with ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    public ResponseEntity<GradeDTO> createGrade(@Valid @RequestBody GradeDTO gradeDTO) {
        try {
            GradeDTO createdGrade = gradeService.createGrade(gradeDTO);
            logger.info("Created new grade: {}", createdGrade);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdGrade);
        } catch (InvalidDataException e) {
            logger.error("Invalid data while creating grade: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error while creating grade: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<GradeDTO> updateGrade(@PathVariable Long id,
                                                @Valid @RequestBody GradeDTO gradeDTO) {
        try {
            return gradeService.updateGrade(id, gradeDTO)
                    .map(updatedGrade -> {
                        logger.info("Updated grade with ID: {}", id);
                        return ResponseEntity.ok(updatedGrade);
                    })
                    .orElseGet(() -> {
                        logger.warn("Grade with ID: {} not found for update.", id);
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
                    });
        } catch (InvalidDataException e) {
            logger.error("Invalid data while updating grade with ID {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (ResourceNotFoundException e) {
            logger.warn("Grade with ID: {} not found for update.", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            logger.error("Error while updating grade with ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGrade(@PathVariable Long id) {
        try {
            gradeService.deleteGrade(id);
            logger.info("Deleted grade with ID: {}", id);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            logger.warn("Grade with ID: {} not found for deletion.", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            logger.error("Error while deleting grade with ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/submissions/{submissionId}")
    public ResponseEntity<Page<GradeDTO>> getGradesBySubmissionId(@PathVariable Long submissionId,
                                                                  Pageable pageable) {
        try {
            Page<GradeDTO> grades = gradeService.getGradesBySubmissionId(submissionId, pageable);
            logger.info("Fetched grades for submission ID: {}", submissionId);
            return ResponseEntity.ok(grades);
        } catch (InvalidDataException e) {
            logger.error("Invalid data while fetching grades for submission ID {}: {}", submissionId, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error while fetching grades for submission ID {}: {}", submissionId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}