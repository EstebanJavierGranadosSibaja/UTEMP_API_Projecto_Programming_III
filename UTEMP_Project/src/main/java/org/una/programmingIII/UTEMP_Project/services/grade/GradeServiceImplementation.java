package org.una.programmingIII.UTEMP_Project.services.grade;

import jakarta.validation.Valid;
import org.hibernate.service.spi.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.una.programmingIII.UTEMP_Project.dtos.GradeDTO;
import org.una.programmingIII.UTEMP_Project.exceptions.InvalidDataException;
import org.una.programmingIII.UTEMP_Project.exceptions.ResourceNotFoundException;
import org.una.programmingIII.UTEMP_Project.models.Grade;
import org.una.programmingIII.UTEMP_Project.repositories.GradeRepository;
import org.una.programmingIII.UTEMP_Project.repositories.SubmissionRepository;
import org.una.programmingIII.UTEMP_Project.transformers.mappers.GenericMapper;
import org.una.programmingIII.UTEMP_Project.transformers.mappers.GenericMapperFactory;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.function.Supplier;

@Service
@Transactional
public class GradeServiceImplementation implements GradeService {

    private static final Logger logger = LoggerFactory.getLogger(GradeServiceImplementation.class);

    private final GradeRepository gradeRepository;
    private final SubmissionRepository submissionRepository;

    private final GenericMapper<Grade, GradeDTO> gradeMapper;

    @Autowired
    public GradeServiceImplementation(
            GradeRepository gradeRepository,
            SubmissionRepository submissionRepository,
            GenericMapperFactory mapperFactory) {

        this.gradeRepository = gradeRepository;
        this.submissionRepository = submissionRepository;
        this.gradeMapper = mapperFactory.createMapper(Grade.class, GradeDTO.class);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<GradeDTO> getAllGrades(Pageable pageable) {
        try {
            return executeWithLogging(() -> {
                Page<Grade> gradePage = gradeRepository.findAll(pageable);
                return gradePage.map(gradeMapper::convertToDTO);
            }, "Error fetching all grades");
        } catch (DataAccessException e) {
            logger.error("Database access error occurred while fetching all grades: {}", e.getMessage());
            throw new InvalidDataException("Error fetching grades from the database");
        } catch (Exception e) {
            logger.error("Unexpected error occurred while fetching all grades: {}", e.getMessage());
            throw new InvalidDataException("Error fetching grades");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<GradeDTO> getGradeById(Long id) {
        try {
            return executeWithLogging(() -> {
                Grade grade = getEntityById(id, gradeRepository, "Grade");
                return Optional.of(gradeMapper.convertToDTO(grade));
            }, "Error fetching grade by ID");
        } catch (ResourceNotFoundException e) {
            logger.warn("Failed to fetch grade: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error fetching grade by ID: {}", e.getMessage());
            throw new ServiceException("Error fetching grade by ID", e);
        }
    }

    @Override
    @Transactional
    public GradeDTO createGrade(@Valid GradeDTO gradeDTO) {
        try {
            Grade grade = gradeMapper.convertToEntity(gradeDTO);
            grade.setSubmission(getEntityById(gradeDTO.getSubmission().getId(), submissionRepository, "Submission"));

            return executeWithLogging(() -> gradeMapper.convertToDTO(gradeRepository.save(grade)),
                    "Error creating grade");
        } catch (ResourceNotFoundException e) {
            logger.warn("Failed to create grade: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error creating grade: {}", e.getMessage());
            throw new ServiceException("Error creating grade", e);
        }
    }

    @Override
    @Transactional
    public Optional<GradeDTO> updateGrade(Long id, @Valid GradeDTO gradeDTO) {
        try {
            Optional<Grade> optionalGrade = gradeRepository.findById(id);
            Grade existingGrade = optionalGrade.orElseThrow(() -> new ResourceNotFoundException("Grade", id));

            updateGradeFields(existingGrade, gradeDTO);

            return executeWithLogging(() -> Optional.of(gradeMapper.convertToDTO(gradeRepository.save(existingGrade))),
                    "Error updating grade");
        } catch (ResourceNotFoundException e) {
            logger.warn("Failed to update grade: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error updating grade: {}", e.getMessage());
            throw new ServiceException("Error updating grade", e);
        }
    }

    @Override
    @Transactional
    public void deleteGrade(Long id) {
        try {
            Grade grade = getEntityById(id, gradeRepository, "Grade");

            executeWithLogging(() -> {
                gradeRepository.delete(grade);
                return null;
            }, "Error deleting grade");
        } catch (ResourceNotFoundException e) {
            logger.warn("Failed to delete grade: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error deleting grade: {}", e.getMessage());
            throw new ServiceException("Error deleting grade", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<GradeDTO> getGradesBySubmissionId(Long submissionId, Pageable pageable) {
        try {
            return executeWithLogging(() -> {
                Page<Grade> grades = gradeRepository.findBySubmissionsId(submissionId, pageable);
                return grades.map(gradeMapper::convertToDTO);
            }, "Error fetching grades by submission ID");
        } catch (DataAccessException e) {
            logger.error("Database access error occurred while fetching grades for submission ID {}: {}", submissionId, e.getMessage());
            throw new InvalidDataException("Error fetching grades from the database");
        } catch (Exception e) {
            logger.error("Unexpected error occurred while fetching grades for submission ID {}: {}", submissionId, e.getMessage());
            throw new InvalidDataException("Error fetching grades by submission ID");
        }
    }

    // --------------- MÉTODOS AUXILIARES -----------------

    private <T> T getEntityById(Long id, JpaRepository<T, Long> repository, String entityName) {
        return getEntityById(id, repository)
                .orElseThrow(() -> new ResourceNotFoundException(entityName, id));
    }

    private <T> Optional<T> getEntityById(Long id, JpaRepository<T, Long> repository) {
        return repository.findById(id);
    }

    private void updateGradeFields(Grade existingGrade, GradeDTO gradeDTO) {
        existingGrade.setGrade(gradeDTO.getGrade());
        existingGrade.setComments(gradeDTO.getComments());
        existingGrade.setReviewedByAi(gradeDTO.getReviewedByAi());
        existingGrade.setState(gradeDTO.getState());
        existingGrade.setLastUpdate(LocalDateTime.now());
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