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
import org.una.programmingIII.UTEMP_Project.dtos.FacultyDTO;
import org.una.programmingIII.UTEMP_Project.dtos.DepartmentDTO;
import org.una.programmingIII.UTEMP_Project.exceptions.InvalidDataException;
import org.una.programmingIII.UTEMP_Project.exceptions.ResourceNotFoundException;
import org.una.programmingIII.UTEMP_Project.services.faculty.FacultyService;

@RestController
@RequestMapping("/utemp/faculties")
public class FacultyController {

    private final FacultyService facultyService;
    private static final Logger logger = LoggerFactory.getLogger(FacultyController.class);

    @Autowired
    public FacultyController(FacultyService facultyService) {
        this.facultyService = facultyService;
    }

    @GetMapping
    public ResponseEntity<Page<FacultyDTO>> getAllFaculties(Pageable pageable) {
        try {
            Page<FacultyDTO> faculties = facultyService.getAllFaculties(pageable);
            return new ResponseEntity<>(faculties, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error retrieving faculties: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<FacultyDTO> getFacultyById(@PathVariable Long id) {
        try {
            return facultyService.getFacultyById(id)
                    .map(faculty -> new ResponseEntity<>(faculty, HttpStatus.OK))
                    .orElseGet(() -> {
                        logger.warn("Faculty not found with id: {}", id);
                        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
                    });
        } catch (Exception e) {
            logger.error("Error retrieving faculty with id {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    public ResponseEntity<FacultyDTO> createFaculty(@Valid @RequestBody FacultyDTO facultyDTO) {
        try {
            FacultyDTO createdFaculty = facultyService.createFaculty(facultyDTO);
            return new ResponseEntity<>(createdFaculty, HttpStatus.CREATED);
        } catch (InvalidDataException e) {
            logger.error("Invalid data while creating faculty: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            logger.error("Error creating faculty: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<FacultyDTO> updateFaculty(@PathVariable Long id,
                                                    @Valid @RequestBody FacultyDTO facultyDTO) {
        try {
            return facultyService.updateFaculty(id, facultyDTO)
                    .map(updatedFaculty -> new ResponseEntity<>(updatedFaculty, HttpStatus.OK))
                    .orElseGet(() -> {
                        logger.warn("Faculty not found for update with id: {}", id);
                        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
                    });
        } catch (InvalidDataException e) {
            logger.error("Invalid data while updating faculty: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            logger.error("Error updating faculty with id {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFaculty(@PathVariable Long id) {
        try {
            facultyService.deleteFaculty(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (ResourceNotFoundException e) {
            logger.warn("Faculty not found for deletion with id: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            logger.error("Error deleting faculty with id {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/university/{universityId}")
    public ResponseEntity<Page<FacultyDTO>> getFacultiesByUniversityId(@PathVariable Long universityId,
                                                                       Pageable pageable) {
        try {
            Page<FacultyDTO> faculties = facultyService.getFacultiesByUniversityId(universityId, pageable);
            return new ResponseEntity<>(faculties, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error retrieving faculties for university {}: {}", universityId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/{facultyId}/departments")
    public ResponseEntity<Void> addDepartmentToFaculty(@PathVariable Long facultyId,
                                                       @Valid @RequestBody DepartmentDTO departmentDTO) {
        try {
            facultyService.addDepartmentToFaculty(facultyId, departmentDTO);
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (InvalidDataException e) {
            logger.error("Invalid data while adding department to faculty: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            logger.error("Error adding department to faculty with id {}: {}", facultyId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{facultyId}/departments/{departmentId}")
    public ResponseEntity<Void> removeDepartmentFromFaculty(@PathVariable Long facultyId,
                                                            @PathVariable Long departmentId) {
        try {
            facultyService.removeDepartmentFromFaculty(facultyId, departmentId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (ResourceNotFoundException e) {
            logger.warn("Department not found for removal from faculty with id: {} and departmentId: {}", facultyId, departmentId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            logger.error("Error removing department from faculty with id {}: {}", facultyId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}