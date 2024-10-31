package org.una.programmingIII.UTEMP_Project.controllers;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.una.programmingIII.UTEMP_Project.dtos.GradeDTO;
import org.una.programmingIII.UTEMP_Project.services.grade.GradeService;
import org.una.programmingIII.UTEMP_Project.exceptions.ResourceNotFoundException;
import org.una.programmingIII.UTEMP_Project.exceptions.InvalidDataException;

import java.util.Optional;

@RestController
@RequestMapping("/utemp/grades")
public class GradeController {

    @Autowired
    private GradeService gradeService;

    @GetMapping
    public ResponseEntity<Page<GradeDTO>> getAllGrades(Pageable pageable) {
        try {
            Page<GradeDTO> grades = gradeService.getAllGrades(pageable);
            return new ResponseEntity<>(grades, HttpStatus.OK);
        } catch (Exception e) {
            throw new InvalidDataException("Error retrieving grades: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<GradeDTO> getGradeById(@PathVariable Long id) {
        try {
            Optional<GradeDTO> gradeDTO = gradeService.getGradeById(id);
            return gradeDTO.map(grade -> new ResponseEntity<>(grade, HttpStatus.OK))
                    .orElseThrow(() -> new ResourceNotFoundException("Grade not found with id: " , id));
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidDataException("Error retrieving grade: " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<GradeDTO> createGrade(@Valid @RequestBody GradeDTO gradeDTO) {
        try {
            GradeDTO createdGrade = gradeService.createGrade(gradeDTO);
            return new ResponseEntity<>(createdGrade, HttpStatus.CREATED);
        } catch (Exception e) {
            throw new InvalidDataException("Error creating grade: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<GradeDTO> updateGrade(@PathVariable Long id, @Valid @RequestBody GradeDTO gradeDTO) {
        try {
            Optional<GradeDTO> updatedGrade = gradeService.updateGrade(id, gradeDTO);
            return updatedGrade.map(grade -> new ResponseEntity<>(grade, HttpStatus.OK))
                    .orElseThrow(() -> new ResourceNotFoundException("Grade not found with id: " , id));
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidDataException("Error updating grade: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGrade(@PathVariable Long id) {
        try {
            gradeService.deleteGrade(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidDataException("Error deleting grade: " + e.getMessage());
        }
    }

    @GetMapping("/submission/{submissionId}")
    public ResponseEntity<Page<GradeDTO>> getGradesBySubmissionId(@PathVariable Long submissionId, Pageable pageable) {
        try {
            Page<GradeDTO> grades = gradeService.getGradesBySubmissionId(submissionId, pageable);
            return new ResponseEntity<>(grades, HttpStatus.OK);
        } catch (Exception e) {
            throw new InvalidDataException("Error retrieving grades for submission: " + e.getMessage());
        }
    }
}
