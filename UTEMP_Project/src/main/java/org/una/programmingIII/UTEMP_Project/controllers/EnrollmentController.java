package org.una.programmingIII.UTEMP_Project.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.una.programmingIII.UTEMP_Project.dtos.EnrollmentDTO;
import org.una.programmingIII.UTEMP_Project.services.EnrollmentServices.EnrollmentService;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/enrollments")
@Validated
public class EnrollmentController {

    @Autowired
    private EnrollmentService enrollmentService;

    @GetMapping
    public ResponseEntity<List<EnrollmentDTO>> getAllEnrollments() {
        List<EnrollmentDTO> enrollments = enrollmentService.getAllEnrollments();
        return ResponseEntity.ok(enrollments);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EnrollmentDTO> getEnrollmentById(@PathVariable Long id) {
        Optional<EnrollmentDTO> enrollment = enrollmentService.getEnrollmentById(id);
        return enrollment.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<EnrollmentDTO> createEnrollment(@RequestBody EnrollmentDTO enrollmentDTO) {
        EnrollmentDTO newEnrollment = enrollmentService.createEnrollment(enrollmentDTO);
        return ResponseEntity.ok(newEnrollment);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EnrollmentDTO> updateEnrollment(@PathVariable Long id, @RequestBody EnrollmentDTO enrollmentDTO) {
        Optional<EnrollmentDTO> updatedEnrollment = enrollmentService.updateEnrollment(id, enrollmentDTO);
        return updatedEnrollment.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEnrollment(@PathVariable Long id) {
        enrollmentService.deleteEnrollment(id);
        return ResponseEntity.noContent().build();
    }
}
