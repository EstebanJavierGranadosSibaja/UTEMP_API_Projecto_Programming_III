package org.una.programmingIII.UTEMP_Project.services.enrollment;

import jakarta.validation.Valid;
import org.una.programmingIII.UTEMP_Project.dtos.EnrollmentDTO;

import java.util.List;
import java.util.Optional;

public interface EnrollmentService {
    List<EnrollmentDTO> getAllEnrollments();
    Optional<EnrollmentDTO> getEnrollmentById(Long id);
    EnrollmentDTO createEnrollment(@Valid EnrollmentDTO enrollmentDTO);
    Optional<EnrollmentDTO> updateEnrollment(Long id, @Valid EnrollmentDTO enrollmentDTO);
    void deleteEnrollment(Long id);
}
