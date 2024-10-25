package org.una.programmingIII.UTEMP_Project.services.grade;

import jakarta.validation.Valid;
import org.hibernate.service.spi.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.una.programmingIII.UTEMP_Project.dtos.GradeDTO;
import org.una.programmingIII.UTEMP_Project.exceptions.ResourceNotFoundException;
import org.una.programmingIII.UTEMP_Project.models.Grade;
import org.una.programmingIII.UTEMP_Project.models.Submission;
import org.una.programmingIII.UTEMP_Project.repositories.GradeRepository;
import org.una.programmingIII.UTEMP_Project.repositories.SubmissionRepository;
import org.una.programmingIII.UTEMP_Project.transformers.mappers.GenericMapper;
import org.una.programmingIII.UTEMP_Project.transformers.mappers.GenericMapperFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@Service
@Transactional
public class GradeServiceImplementation implements GradeService {

    private static final Logger logger = LoggerFactory.getLogger(GradeServiceImplementation.class);

    @Autowired
    private GradeRepository gradeRepository;

    @Autowired
    private SubmissionRepository submissionRepository;

    private final GenericMapper<Grade, GradeDTO> gradeMapper;

    @Autowired
    public GradeServiceImplementation(GenericMapperFactory mapperFactory) {
        this.gradeMapper = mapperFactory.createMapper(Grade.class, GradeDTO.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GradeDTO> getAllGrades() {
        return executeWithLogging(() -> gradeMapper.convertToDTOList(gradeRepository.findAll()),
                "Error fetching all grades");
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<GradeDTO> getGradeById(Long id) {
        return executeWithLogging(() -> {
            Grade grade = getEntityById(id, gradeRepository, "Grade");
            return Optional.of(gradeMapper.convertToDTO(grade));
        }, "Error fetching grade by ID");
    }

    @Override
    @Transactional
    public GradeDTO createGrade(@Valid GradeDTO gradeDTO) {
        Grade grade = gradeMapper.convertToEntity(gradeDTO);
        grade.setSubmission(getEntityById(gradeDTO.getSubmission().getId(), submissionRepository, "Submission"));
        return executeWithLogging(() -> gradeMapper.convertToDTO(gradeRepository.save(grade)),
                "Error creating grade");
    }

    @Override
    @Transactional
    public Optional<GradeDTO> updateGrade(Long id, @Valid GradeDTO gradeDTO) {
        Optional<Grade> optionalGrade = gradeRepository.findById(id);
        Grade existingGrade = optionalGrade.orElseThrow(() -> new ResourceNotFoundException("Grade", id));

        updateGradeFields(existingGrade, gradeDTO);
        return executeWithLogging(() -> Optional.of(gradeMapper.convertToDTO(gradeRepository.save(existingGrade))),
                "Error updating grade");
    }

    @Override
    @Transactional
    public void deleteGrade(Long id) {
        Grade grade = getEntityById(id, gradeRepository, "Grade");
        executeWithLogging(() -> {
            gradeRepository.delete(grade);
            return null;
        }, "Error deleting grade");
    }

    @Override
    @Transactional(readOnly = true)
    public List<GradeDTO> getGradesBySubmissionId(Long submissionId) {
        Submission submission = getEntityById(submissionId, submissionRepository, "Submission");
        return executeWithLogging(() -> gradeMapper.convertToDTOList(gradeRepository.findBySubmission(submission)),
                "Error fetching grades by submission ID");
    }

    // --------------- MÉTODOS AUXILIARES -----------------

    private <T> T getEntityById(Long id, JpaRepository<T, Long> repository, String entityName) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(entityName, id));
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
