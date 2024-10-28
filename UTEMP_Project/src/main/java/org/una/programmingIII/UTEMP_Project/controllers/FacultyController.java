package org.una.programmingIII.UTEMP_Project.controllers;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.una.programmingIII.UTEMP_Project.dtos.FacultyDTO;
import org.una.programmingIII.UTEMP_Project.dtos.DepartmentDTO;
import org.una.programmingIII.UTEMP_Project.services.faculty.FacultyService;
import org.una.programmingIII.UTEMP_Project.exceptions.ResourceNotFoundException;
import org.una.programmingIII.UTEMP_Project.exceptions.InvalidDataException;

import java.util.Optional;

@RestController
@RequestMapping("/utemp/faculties")
public class FacultyController {

    @Autowired
    private FacultyService facultyService;

    @GetMapping
    public ResponseEntity<Page<FacultyDTO>> getAllFaculties(
            @PageableDefault(size = 10, page = 0) Pageable pageable) {
        try {
            return ResponseEntity.ok(facultyService.getAllFaculties(pageable));
        } catch (Exception e) {
            throw new InvalidDataException("Error retrieving faculties: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<FacultyDTO> getFacultyById(@PathVariable Long id) {
        try {
            Optional<FacultyDTO> faculty = facultyService.getFacultyById(id);
            return faculty.map(ResponseEntity::ok)
                    .orElseThrow(() -> new ResourceNotFoundException("Faculty not found with id: " , id));
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidDataException("Error retrieving faculty: " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<FacultyDTO> createFaculty(@Valid @RequestBody FacultyDTO facultyDTO) {
        try {
            FacultyDTO createdFaculty = facultyService.createFaculty(facultyDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdFaculty);
        } catch (Exception e) {
            throw new InvalidDataException("Error creating faculty: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<FacultyDTO> updateFaculty(@PathVariable Long id, @Valid @RequestBody FacultyDTO facultyDTO) {
        try {
            Optional<FacultyDTO> updatedFaculty = facultyService.updateFaculty(id, facultyDTO);
            return updatedFaculty.map(ResponseEntity::ok)
                    .orElseThrow(() -> new ResourceNotFoundException("Faculty not found with id: " , id));
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidDataException("Error updating faculty: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFaculty(@PathVariable Long id) {
        try {
            facultyService.deleteFaculty(id);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidDataException("Error deleting faculty: " + e.getMessage());
        }
    }

    @GetMapping("/{facultyId}/departments")
    public ResponseEntity<Page<DepartmentDTO>> getDepartmentsByFacultyId(
            @PathVariable Long facultyId,
            @PageableDefault(size = 10, page = 0) Pageable pageable) {
        try {
            return ResponseEntity.ok(facultyService.getDepartmentsByFacultyId(facultyId, pageable));
        } catch (Exception e) {
            throw new InvalidDataException("Error retrieving departments for faculty: " + e.getMessage());
        }
    }

    @PostMapping("/{facultyId}/departments")
    public ResponseEntity<Void> addDepartmentToFaculty(
            @PathVariable Long facultyId, @Valid @RequestBody DepartmentDTO departmentDTO) {
        try {
            facultyService.addDepartmentToFaculty(facultyId, departmentDTO);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (Exception e) {
            throw new InvalidDataException("Error adding department to faculty: " + e.getMessage());
        }
    }

    @DeleteMapping("/{facultyId}/departments/{departmentId}")
    public ResponseEntity<Void> removeDepartmentFromFaculty(
            @PathVariable Long facultyId, @PathVariable Long departmentId) {
        try {
            facultyService.removeDepartmentFromFaculty(facultyId, departmentId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            throw new InvalidDataException("Error removing department from faculty: " + e.getMessage());
        }
    }
}