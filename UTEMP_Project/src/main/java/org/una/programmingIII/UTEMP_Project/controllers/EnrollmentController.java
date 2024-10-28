package org.una.programmingIII.UTEMP_Project.controllers;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.una.programmingIII.UTEMP_Project.dtos.EnrollmentDTO;
import org.una.programmingIII.UTEMP_Project.services.enrollment.EnrollmentService;
import org.una.programmingIII.UTEMP_Project.exceptions.ResourceNotFoundException;
import org.una.programmingIII.UTEMP_Project.exceptions.InvalidDataException;
import org.una.programmingIII.UTEMP_Project.exceptions.ResourceAlreadyExistsException;

import java.util.Optional;

@RestController
@RequestMapping("/utemp/enrollments")
public class EnrollmentController {

    @Autowired
    private EnrollmentService enrollmentService;

    @GetMapping
    public ResponseEntity<Page<EnrollmentDTO>> getAllEnrollments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<EnrollmentDTO> enrollments = enrollmentService.getAllEnrollments(page, size);
            return new ResponseEntity<>(enrollments, HttpStatus.OK);
        } catch (Exception e) {
            throw new InvalidDataException("Error retrieving enrollments: " + e.getMessage());
        }
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<Page<EnrollmentDTO>> getEnrollmentsByCourseId(@PathVariable Long courseId,
                                                                        @RequestParam(defaultValue = "0") int page,
                                                                        @RequestParam(defaultValue = "10") int size) {
        try {
            Page<EnrollmentDTO> enrollments = enrollmentService.getEnrollmentsByCourseId(courseId, page, size);
            return new ResponseEntity<>(enrollments, HttpStatus.OK);
        } catch (Exception e) {
            throw new InvalidDataException("Error retrieving enrollments for course: " + e.getMessage());
        }
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<Page<EnrollmentDTO>> getEnrollmentsByStudentId(@PathVariable Long studentId,
                                                                         @RequestParam(defaultValue = "0") int page,
                                                                         @RequestParam(defaultValue = "10") int size) {
        try {
            Page<EnrollmentDTO> enrollments = enrollmentService.getEnrollmentsByStudentId(studentId, page, size);
            return new ResponseEntity<>(enrollments, HttpStatus.OK);
        } catch (Exception e) {
            throw new InvalidDataException("Error retrieving enrollments for student: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<EnrollmentDTO> getEnrollmentById(@PathVariable Long id) {
        try {
            Optional<EnrollmentDTO> enrollment = enrollmentService.getEnrollmentById(id);
            return enrollment.map(ResponseEntity::ok)
                    .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found with id: " , id));
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidDataException("Error retrieving enrollment: " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<EnrollmentDTO> createEnrollment(@Valid @RequestBody EnrollmentDTO enrollmentDTO) {
        try {
            EnrollmentDTO createdEnrollment = enrollmentService.createEnrollment(enrollmentDTO);
            return new ResponseEntity<>(createdEnrollment, HttpStatus.CREATED);
        } catch (ResourceAlreadyExistsException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidDataException("Error creating enrollment: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<EnrollmentDTO> updateEnrollment(
            @PathVariable Long id,
            @Valid @RequestBody EnrollmentDTO enrollmentDTO) {
        try {
            Optional<EnrollmentDTO> updatedEnrollment = enrollmentService.updateEnrollment(id, enrollmentDTO);
            return updatedEnrollment.map(ResponseEntity::ok)
                    .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found with id: " , id));
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidDataException("Error updating enrollment: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEnrollment(@PathVariable Long id) {
        try {
            enrollmentService.deleteEnrollment(id);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidDataException("Error deleting enrollment: " + e.getMessage());
        }
    }
}