package org.una.programmingIII.UTEMP_Project.services.enrollment;

import jakarta.validation.Valid;
import org.hibernate.service.spi.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.List;
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
    public List<EnrollmentDTO> getAllEnrollments() {
        return executeWithLogging(() -> enrollmentMapper.convertToDTOList(enrollmentRepository.findAll()),
                "Error fetching all enrollments");
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EnrollmentDTO> getEnrollmentById(Long id) {
        return executeWithLogging(() -> {
            Enrollment enrollment = getEntityById(id, enrollmentRepository, "Enrollment");
            return Optional.of(enrollmentMapper.convertToDTO(enrollment));
        }, "Error fetching enrollment by ID");
    }

    @Override
    @Transactional
    public EnrollmentDTO createEnrollment(@Valid EnrollmentDTO enrollmentDTO) {
        Enrollment enrollment = enrollmentMapper.convertToEntity(enrollmentDTO);
        enrollment.setCourse(getEntityById(enrollmentDTO.getCourse().getId(), courseRepository, "Course"));
        enrollment.setStudent(getEntityById(enrollmentDTO.getStudent().getId(), userRepository, "User"));
        return executeWithLogging(() -> enrollmentMapper.convertToDTO(enrollmentRepository.save(enrollment)),
                "Error creating enrollment");
    }

    @Override
    @Transactional
    public Optional<EnrollmentDTO> updateEnrollment(Long id, @Valid EnrollmentDTO enrollmentDTO) {
        Optional<Enrollment> optionalEnrollment = enrollmentRepository.findById(id);
        Enrollment existingEnrollment = optionalEnrollment.orElseThrow(() -> new ResourceNotFoundException("Enrollment", id));

        updateEnrollmentFields(existingEnrollment, enrollmentDTO);
        return executeWithLogging(() -> Optional.of(enrollmentMapper.convertToDTO(enrollmentRepository.save(existingEnrollment))),
                "Error updating enrollment");
    }

    @Override
    @Transactional
    public void deleteEnrollment(Long id) {
        Enrollment enrollment = getEntityById(id, enrollmentRepository, "Enrollment");
        executeWithLogging(() -> {
            enrollmentRepository.delete(enrollment);
            return null;
        }, "Error deleting enrollment");
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
