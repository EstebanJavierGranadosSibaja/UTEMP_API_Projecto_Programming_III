package org.una.programmingIII.UTEMP_Project.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.una.programmingIII.UTEMP_Project.dtos.GradeDTO;
import org.una.programmingIII.UTEMP_Project.services.grade.GradeService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/grades")
@Validated
public class GradeController {

    @Autowired
    private GradeService gradeService;

    @GetMapping
    public ResponseEntity<List<GradeDTO>> getAllGrades() {
        List<GradeDTO> grades = gradeService.getAllGrades();
        return new ResponseEntity<>(grades, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GradeDTO> getGradeById(@PathVariable Long id) {
        Optional<GradeDTO> grade = gradeService.getGradeById(id);
        return grade.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public ResponseEntity<GradeDTO> createGrade(@RequestBody GradeDTO gradeDTO) {
        GradeDTO createdGrade = gradeService.createGrade(gradeDTO);
        return new ResponseEntity<>(createdGrade, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<GradeDTO> updateGrade(@PathVariable Long id, @RequestBody GradeDTO gradeDTO) {
        Optional<GradeDTO> updatedGrade = gradeService.updateGrade(id, gradeDTO);
        return updatedGrade.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGrade(@PathVariable Long id) {
        gradeService.deleteGrade(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/submission/{submissionId}")
    public ResponseEntity<List<GradeDTO>> getGradesBySubmissionId(@PathVariable Long submissionId) {
        List<GradeDTO> grades = gradeService.getGradesBySubmissionId(submissionId);
        return new ResponseEntity<>(grades, HttpStatus.OK);
    }
}
