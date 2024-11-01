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
import org.una.programmingIII.UTEMP_Project.dtos.UniversityDTO;
import org.una.programmingIII.UTEMP_Project.dtos.FacultyDTO;
import org.una.programmingIII.UTEMP_Project.services.university.UniversityService;
import org.una.programmingIII.UTEMP_Project.exceptions.ResourceNotFoundException;
import org.una.programmingIII.UTEMP_Project.exceptions.InvalidDataException;

import java.util.Optional;

@RestController
@RequestMapping("/utemp/universities")
public class UniversityController {

    private final UniversityService universityService;
    private static final Logger logger = LoggerFactory.getLogger(UniversityController.class);

    @Autowired
    public UniversityController(UniversityService universityService) {
        this.universityService = universityService;
    }

    @GetMapping
    public ResponseEntity<Page<UniversityDTO>> getAllUniversities(Pageable pageable) {
        logger.info("Fetching all universities with pagination");
        try {
            Page<UniversityDTO> universities = universityService.getAllUniversities(pageable);
            return ResponseEntity.ok(universities);
        } catch (Exception e) {
            logger.error("Error fetching universities: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<UniversityDTO> getUniversityById(@PathVariable Long id) {
        logger.info("Fetching university with ID: {}", id);
        try {
            Optional<UniversityDTO> university = universityService.getUniversityById(id);
            return university.map(ResponseEntity::ok)
                    .orElseGet(() -> {
                        logger.warn("University with ID: {} not found", id);
                        return ResponseEntity.notFound().build();
                    });
        } catch (ResourceNotFoundException e) {
            logger.error("Resource not found for ID: {} - {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            logger.error("Error fetching university with ID: {} - {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    public ResponseEntity<UniversityDTO> createUniversity(@Valid @RequestBody UniversityDTO universityDTO) {
        logger.info("Creating new university: {}", universityDTO);
        try {
            UniversityDTO createdUniversity = universityService.createUniversity(universityDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUniversity);
        } catch (InvalidDataException e) {
            logger.error("Invalid data for university creation: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            logger.error("Error creating university: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<UniversityDTO> updateUniversity(@PathVariable Long id,
                                                          @Valid @RequestBody UniversityDTO universityDTO) {
        logger.info("Updating university with ID: {}", id);
        try {
            Optional<UniversityDTO> updatedUniversity = universityService.updateUniversity(id, universityDTO);
            return updatedUniversity.map(ResponseEntity::ok)
                    .orElseGet(() -> {
                        logger.warn("University with ID: {} not found for update", id);
                        return ResponseEntity.notFound().build();
                    });
        } catch (InvalidDataException e) {
            logger.error("Invalid data provided for university update: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (ResourceNotFoundException e) {
            logger.error("University with ID: {} not found: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            logger.error("Error updating university with ID: {} - {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUniversity(@PathVariable Long id) {
        logger.info("Deleting university with ID: {}", id);
        try {
            universityService.deleteUniversity(id);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            logger.warn("University with ID: {} not found for deletion", id);
            return ResponseEntity.notFound().build();
        } catch (InvalidDataException e) {
            logger.error("Invalid data provided for university deletion: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            logger.error("Error deleting university with ID: {} - {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{universityId}/faculties")
    public ResponseEntity<Void> addFacultyToUniversity(@PathVariable Long universityId,
                                                       @Valid @RequestBody FacultyDTO facultyDTO) {
        logger.info("Adding faculty to university with ID: {}", universityId);
        try {
            universityService.addFacultyToUniversity(universityId, facultyDTO);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (ResourceNotFoundException e) {
            logger.warn("University with ID: {} not found when adding faculty", universityId);
            return ResponseEntity.notFound().build();
        } catch (InvalidDataException e) {
            logger.error("Invalid data provided for adding faculty: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            logger.error("Error adding faculty to university with ID: {} - {}", universityId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{universityId}/faculties/{facultyId}")
    public ResponseEntity<Void> removeFacultyFromUniversity(@PathVariable Long universityId,
                                                            @PathVariable Long facultyId) {
        logger.info("Removing faculty with ID: {} from university with ID: {}", facultyId, universityId);
        try {
            universityService.removeFacultyFromUniversity(universityId, facultyId);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            logger.warn("Faculty with ID: {} not found in university with ID: {}", facultyId, universityId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error removing faculty from university with ID: {} - {}", universityId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}