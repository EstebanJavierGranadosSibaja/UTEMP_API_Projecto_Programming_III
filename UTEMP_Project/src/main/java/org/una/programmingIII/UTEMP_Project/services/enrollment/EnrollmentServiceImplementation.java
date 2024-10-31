package org.una.programmingIII.UTEMP_Project.services.enrollment;

import jakarta.validation.Valid;
import org.hibernate.service.spi.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.una.programmingIII.UTEMP_Project.dtos.EnrollmentDTO;
import org.una.programmingIII.UTEMP_Project.exceptions.ResourceNotFoundException;
import org.una.programmingIII.UTEMP_Project.models.Enrollment;
import org.una.programmingIII.UTEMP_Project.repositories.EnrollmentRepository;
import org.una.programmingIII.UTEMP_Project.repositories.CourseRepository;
import org.una.programmingIII.UTEMP_Project.repositories.UserRepository;
import org.una.programmingIII.UTEMP_Project.transformers.mappers.GenericMapper;
import org.una.programmingIII.UTEMP_Project.transformers.mappers.GenericMapperFactory;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.function.Supplier;

@Service
@Transactional
public class EnrollmentServiceImplementation implements EnrollmentService {

    private static final Logger logger = LoggerFactory.getLogger(EnrollmentServiceImplementation.class);

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    private final GenericMapper<Enrollment, EnrollmentDTO> enrollmentMapper;

    @Autowired
    public EnrollmentServiceImplementation(GenericMapperFactory mapperFactory) {
        this.enrollmentMapper = mapperFactory.createMapper(Enrollment.class, EnrollmentDTO.class);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EnrollmentDTO> getAllEnrollments(int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            return enrollmentRepository.findAll(pageable).map(enrollmentMapper::convertToDTO);
        } catch (Exception e) {
            logger.error("Error fetching all enrollments");
            throw new ServiceException("Error fetching all enrollments", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EnrollmentDTO> getEnrollmentsByCourseId(Long courseId, int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            return enrollmentRepository.findByCourseId(courseId, pageable).map(enrollmentMapper::convertToDTO);
        } catch (Exception e) {
            logger.error("Error fetching enrollments by course ID {}: {}", courseId, e.getMessage());
            throw new ServiceException("Error fetching enrollments by course ID", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EnrollmentDTO> getEnrollmentsByStudentId(Long studentId, int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            return enrollmentRepository.findByStudentId(studentId, pageable).map(enrollmentMapper::convertToDTO);
        } catch (Exception e) {
            logger.error("Error fetching enrollments by student ID {}: {}", studentId, e.getMessage());
            throw new ServiceException("Error fetching enrollments by student ID", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EnrollmentDTO> getEnrollmentById(Long id) {
        try {
            Enrollment enrollment = getEntityById(id, enrollmentRepository, "Enrollment");
            return Optional.of(enrollmentMapper.convertToDTO(enrollment));
        } catch (ResourceNotFoundException e) {
            logger.warn("Enrollment not found: {}", e.getMessage());
            throw e; // Re-throwing to let the global exception handler handle this
        } catch (Exception e) {
            logger.error("Error fetching enrollment by ID {}: {}", id, e.getMessage());
            throw new ServiceException("Error fetching enrollment by ID", e);
        }
    }

    @Override
    @Transactional
    public EnrollmentDTO createEnrollment(@Valid EnrollmentDTO enrollmentDTO) {
        try {
            Enrollment enrollment = enrollmentMapper.convertToEntity(enrollmentDTO);
            enrollment.setCourse(getEntityById(enrollmentDTO.getCourse().getId(), courseRepository, "Course"));
            enrollment.setStudent(getEntityById(enrollmentDTO.getStudent().getId(), userRepository, "User"));
            return enrollmentMapper.convertToDTO(enrollmentRepository.save(enrollment));
        } catch (ResourceNotFoundException e) {
            logger.warn("Failed to create enrollment: {}", e.getMessage());
            throw e; // Re-throwing to let the global exception handler handle this
        } catch (Exception e) {
            logger.error("Error creating enrollment: {}", e.getMessage());
            throw new ServiceException("Error creating enrollment", e);
        }
    }

    @Override
    @Transactional
    public Optional<EnrollmentDTO> updateEnrollment(Long id, @Valid EnrollmentDTO enrollmentDTO) {
        try {
            Optional<Enrollment> optionalEnrollment = enrollmentRepository.findById(id);
            Enrollment existingEnrollment = optionalEnrollment.orElseThrow(() -> new ResourceNotFoundException("Enrollment", id));

            updateEnrollmentFields(existingEnrollment, enrollmentDTO);
            return Optional.of(enrollmentMapper.convertToDTO(enrollmentRepository.save(existingEnrollment)));
        } catch (ResourceNotFoundException e) {
            logger.warn("Failed to update enrollment: {}", e.getMessage());
            throw e; // Re-throwing to let the global exception handler handle this
        } catch (Exception e) {
            logger.error("Error updating enrollment ID {}: {}", id, e.getMessage());
            throw new ServiceException("Error updating enrollment", e);
        }
    }

    @Override
    @Transactional
    public void deleteEnrollment(Long id) {
        try {
            Enrollment enrollment = getEntityById(id, enrollmentRepository, "Enrollment");
            enrollmentRepository.delete(enrollment);
        } catch (ResourceNotFoundException e) {
            logger.warn("Failed to delete enrollment: {}", e.getMessage());
            throw e; // Re-throwing to let the global exception handler handle this
        } catch (Exception e) {
            logger.error("Error deleting enrollment ID {}: {}", id, e.getMessage());
            throw new ServiceException("Error deleting enrollment", e);
        }
    }

    // --------------- MÉTODOS AUXILIARES -----------------

    private <T> T getEntityById(Long id, JpaRepository<T, Long> repository, String entityName) {
        return findEntityById(id, repository)
                .orElseThrow(() -> new ResourceNotFoundException(entityName, id));
    }

    private <T> Optional<T> findEntityById(Long id, JpaRepository<T, Long> repository) {
        return repository.findById(id);
    }

    private void updateEnrollmentFields(Enrollment existingEnrollment, EnrollmentDTO enrollmentDTO) {
        existingEnrollment.setState(enrollmentDTO.getState());
        existingEnrollment.setLastUpdate(LocalDateTime.now());
    }

    private <T> T executeWithLogging(Supplier<T> action, String errorMessage) {
        try {
            return action.get();
        } catch (Exception e) {
            logger.error("{}: {}", errorMessage, e.getMessage());
            throw new ServiceException(errorMessage, e);
        }
    }
}
