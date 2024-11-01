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
import org.una.programmingIII.UTEMP_Project.dtos.EnrollmentDTO;
import org.una.programmingIII.UTEMP_Project.exceptions.InvalidDataException;
import org.una.programmingIII.UTEMP_Project.exceptions.ResourceNotFoundException;
import org.una.programmingIII.UTEMP_Project.services.enrollment.EnrollmentService;

import java.util.Optional;

@RestController
@RequestMapping("/utemp/enrollments")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;
    private static final Logger logger = LoggerFactory.getLogger(EnrollmentController.class);

    @Autowired
    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @GetMapping
    public ResponseEntity<Page<EnrollmentDTO>> getAllEnrollments(Pageable pageable) {
        try {
            Page<EnrollmentDTO> enrollments = enrollmentService.getAllEnrollments(pageable);
            return ResponseEntity.ok(enrollments);
        } catch (Exception e) {
            logger.error("Error retrieving enrollments: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<Page<EnrollmentDTO>> getEnrollmentsByCourseId(@PathVariable Long courseId,
                                                                        Pageable pageable) {
        try {
            Page<EnrollmentDTO> enrollments = enrollmentService.getEnrollmentsByCourseId(courseId, pageable);
            return ResponseEntity.ok(enrollments);
        } catch (Exception e) {
            logger.error("Error retrieving enrollments for course {}: {}", courseId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<Page<EnrollmentDTO>> getEnrollmentsByStudentId(@PathVariable Long studentId,
                                                                         Pageable pageable) {
        try {
            Page<EnrollmentDTO> enrollments = enrollmentService.getEnrollmentsByStudentId(studentId, pageable);
            return ResponseEntity.ok(enrollments);
        } catch (Exception e) {
            logger.error("Error retrieving enrollments for student {}: {}", studentId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<EnrollmentDTO> getEnrollmentById(@PathVariable Long id) {
        try {
            Optional<EnrollmentDTO> enrollmentDTO = enrollmentService.getEnrollmentById(id);
            return enrollmentDTO.map(ResponseEntity::ok)
                    .orElseGet(() -> {
                        logger.warn("Enrollment not found with id: {}", id);
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
                    });
        } catch (Exception e) {
            logger.error("Error retrieving enrollment with id {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    public ResponseEntity<EnrollmentDTO> createEnrollment(@Valid @RequestBody EnrollmentDTO enrollmentDTO) {
        try {
            EnrollmentDTO createdEnrollment = enrollmentService.createEnrollment(enrollmentDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdEnrollment);
        } catch (InvalidDataException e) {
            logger.error("Invalid data while creating enrollment: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            logger.error("Error creating enrollment: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<EnrollmentDTO> updateEnrollment(@PathVariable Long id,
                                                          @Valid @RequestBody EnrollmentDTO enrollmentDTO) {
        try {
            Optional<EnrollmentDTO> updatedEnrollment = enrollmentService.updateEnrollment(id, enrollmentDTO);
            return updatedEnrollment.map(ResponseEntity::ok)
                    .orElseGet(() -> {
                        logger.warn("Enrollment not found for update with id: {}", id);
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
                    });
        } catch (InvalidDataException e) {
            logger.error("Invalid data while updating enrollment: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            logger.error("Error updating enrollment with id {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEnrollment(@PathVariable Long id) {
        try {
            enrollmentService.deleteEnrollment(id);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            logger.warn("Enrollment not found for deletion with id: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            logger.error("Error deleting enrollment with id {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}