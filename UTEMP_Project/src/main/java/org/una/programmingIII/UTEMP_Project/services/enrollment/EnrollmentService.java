package org.una.programmingIII.UTEMP_Project.services.enrollment;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.una.programmingIII.UTEMP_Project.dtos.EnrollmentDTO;

import java.util.List;
import java.util.Optional;

public interface EnrollmentService {
    Page<EnrollmentDTO> getAllEnrollments(Pageable pageable);
    Page<EnrollmentDTO> getEnrollmentsByCourseId(Long courseId, Pageable pageable);
    Page<EnrollmentDTO> getEnrollmentsByStudentId(Long studentId, Pageable pageable);
    Optional<EnrollmentDTO> getEnrollmentById(Long id);
    EnrollmentDTO createEnrollment(@Valid EnrollmentDTO enrollmentDTO);
    Optional<EnrollmentDTO> updateEnrollment(Long id, @Valid EnrollmentDTO enrollmentDTO);
    void deleteEnrollment(Long id);
}
