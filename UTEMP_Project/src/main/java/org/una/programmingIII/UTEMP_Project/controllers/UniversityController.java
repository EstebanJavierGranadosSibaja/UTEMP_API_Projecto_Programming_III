package org.una.programmingIII.UTEMP_Project.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.una.programmingIII.UTEMP_Project.dtos.UniversityDTO;
import org.una.programmingIII.UTEMP_Project.dtos.FacultyDTO;
import org.una.programmingIII.UTEMP_Project.services.university.UniversityService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/universities")
@Validated
public class UniversityController {

    @Autowired
    private UniversityService universityService;

    @GetMapping
    public ResponseEntity<List<UniversityDTO>> getAllUniversities() {
        List<UniversityDTO> universities = universityService.getAllUniversities();
        return new ResponseEntity<>(universities, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UniversityDTO> getUniversityById(@PathVariable Long id) {
        Optional<UniversityDTO> university = universityService.getUniversityById(id);
        return university.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public ResponseEntity<UniversityDTO> createUniversity(@RequestBody UniversityDTO universityDTO) {
        UniversityDTO createdUniversity = universityService.createUniversity(universityDTO);
        return new ResponseEntity<>(createdUniversity, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UniversityDTO> updateUniversity(@PathVariable Long id, @RequestBody UniversityDTO universityDTO) {
        Optional<UniversityDTO> updatedUniversity = universityService.updateUniversity(id, universityDTO);
        return updatedUniversity.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUniversity(@PathVariable Long id) {
        try {
            universityService.deleteUniversity(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/{universityId}/faculties")
    public ResponseEntity<List<FacultyDTO>> getFacultiesByUniversityId(@PathVariable Long universityId) {
        List<FacultyDTO> faculties = universityService.getFacultiesByUniversityId(universityId);
        return new ResponseEntity<>(faculties, HttpStatus.OK);
    }

    @PostMapping("/{universityId}/faculties")
    public ResponseEntity<Void> addFacultyToUniversity(@PathVariable Long universityId, @RequestBody FacultyDTO facultyDTO) {
        try {
            universityService.addFacultyToUniversity(universityId, facultyDTO);
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{universityId}/faculties/{facultyId}")
    public ResponseEntity<Void> removeFacultyFromUniversity(@PathVariable Long universityId, @PathVariable Long facultyId) {
        try {
            universityService.removeFacultyFromUniversity(universityId, facultyId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}

