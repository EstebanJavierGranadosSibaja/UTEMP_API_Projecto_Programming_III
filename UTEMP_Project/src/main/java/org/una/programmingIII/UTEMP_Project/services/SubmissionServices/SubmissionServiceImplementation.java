package org.una.programmingIII.UTEMP_Project.services.SubmissionServices;

import jakarta.validation.Valid;
import org.hibernate.service.spi.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.una.programmingIII.UTEMP_Project.dtos.SubmissionDTO;
import org.una.programmingIII.UTEMP_Project.dtos.FileMetadatumDTO;
import org.una.programmingIII.UTEMP_Project.dtos.GradeDTO;
import org.una.programmingIII.UTEMP_Project.exceptions.ResourceNotFoundException;
import org.una.programmingIII.UTEMP_Project.models.Assignment;
import org.una.programmingIII.UTEMP_Project.models.FileMetadatum;
import org.una.programmingIII.UTEMP_Project.models.Grade;
import org.una.programmingIII.UTEMP_Project.models.Submission;
import org.una.programmingIII.UTEMP_Project.repositories.AssignmentRepository;
import org.una.programmingIII.UTEMP_Project.repositories.FileMetadatumRepository;
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
public class SubmissionServiceImplementation implements SubmissionService {

    private static final Logger logger = LoggerFactory.getLogger(SubmissionServiceImplementation.class);

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private FileMetadatumRepository fileMetadatumRepository;

    @Autowired
    private GradeRepository gradeRepository;

    private final GenericMapper<Submission, SubmissionDTO> submissionMapper;
    private final GenericMapper<FileMetadatum, FileMetadatumDTO> fileMetadatumMapper;
    private final GenericMapper<Grade, GradeDTO> gradeMapper;

    @Autowired
    public SubmissionServiceImplementation(GenericMapperFactory mapperFactory) {
        this.submissionMapper = mapperFactory.createMapper(Submission.class, SubmissionDTO.class);
        this.fileMetadatumMapper = mapperFactory.createMapper(FileMetadatum.class, FileMetadatumDTO.class);
        this.gradeMapper = mapperFactory.createMapper(Grade.class, GradeDTO.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubmissionDTO> getAllSubmissions() {
        return executeWithLogging(() -> submissionMapper.convertToDTOList(submissionRepository.findAll()),
                "Error fetching all submissions");
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SubmissionDTO> getSubmissionById(Long id) {
        return executeWithLogging(() -> {
            Submission submission = getEntityById(id, submissionRepository, "Submission");
            return Optional.of(submissionMapper.convertToDTO(submission));
        }, "Error fetching submission by ID");
    }

    @Override
    @Transactional
    public SubmissionDTO createSubmission(@Valid SubmissionDTO submissionDTO) {
        Submission submission = submissionMapper.convertToEntity(submissionDTO);
        submission.setAssignment(getEntityById(submissionDTO.getAssignment().getId(), assignmentRepository, "Assignment"));
        return executeWithLogging(() -> submissionMapper.convertToDTO(submissionRepository.save(submission)),
                "Error creating submission");
    }

    @Override
    @Transactional
    public Optional<SubmissionDTO> updateSubmission(Long id, @Valid SubmissionDTO submissionDTO) {
        Optional<Submission> optionalSubmission = submissionRepository.findById(id);
        Submission existingSubmission = optionalSubmission.orElseThrow(() -> new ResourceNotFoundException("Submission", id));

        updateSubmissionFields(existingSubmission, submissionDTO);
        return executeWithLogging(() -> Optional.of(submissionMapper.convertToDTO(submissionRepository.save(existingSubmission))),
                "Error updating submission");
    }

    @Override
    @Transactional
    public void deleteSubmission(Long id) {
        Submission submission = getEntityById(id, submissionRepository, "Submission");
        executeWithLogging(() -> {
            submissionRepository.delete(submission);
            return null;
        }, "Error deleting submission");
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubmissionDTO> getSubmissionsByAssignmentId(Long assignmentId) {
        Assignment assignment = getEntityById(assignmentId, assignmentRepository, "Assignment");
        return executeWithLogging(() -> submissionMapper.convertToDTOList(submissionRepository.findByAssignment(assignment)),
                "Error fetching submissions by assignment ID");
    }

    // Métodos adicionales para manejar las listas de FileMetadatum y Grade

    @Override
    @Transactional
    public FileMetadatumDTO addFileMetadatumToSubmission(Long submissionId, @Valid FileMetadatumDTO fileMetadatumDTO) {
        Submission submission = getEntityById(submissionId, submissionRepository, "Submission");
        FileMetadatum fileMetadatum = fileMetadatumMapper.convertToEntity(fileMetadatumDTO);
        fileMetadatum.setSubmission(submission);

        return executeWithLogging(() -> {
            FileMetadatum savedFileMetadatum = fileMetadatumRepository.save(fileMetadatum);
            submission.getFileMetadata().add(savedFileMetadatum);
            return fileMetadatumMapper.convertToDTO(savedFileMetadatum);
        }, "Error adding file metadata to submission ID: " + submissionId);
    }

    @Override
    @Transactional
    public GradeDTO addGradeToSubmission(Long submissionId, @Valid GradeDTO gradeDTO) {
        Submission submission = getEntityById(submissionId, submissionRepository, "Submission");
        Grade grade = gradeMapper.convertToEntity(gradeDTO);
        grade.setSubmission(submission);

        return executeWithLogging(() -> {
            Grade savedGrade = gradeRepository.save(grade);
            submission.getGrades().add(savedGrade);
            return gradeMapper.convertToDTO(savedGrade);
        }, "Error adding grade to submission ID: " + submissionId);
    }

    @Override
    @Transactional
    public void removeFileMetadatumFromSubmission(Long submissionId, Long fileMetadatumId) {
        Submission submission = getEntityById(submissionId, submissionRepository, "Submission");
        FileMetadatum fileMetadatum = getEntityById(fileMetadatumId, fileMetadatumRepository, "FileMetadatum");

        if (!submission.getFileMetadata().contains(fileMetadatum)) {
            throw new ResourceNotFoundException("File metadata not found in this submission", submissionId);
        }

        executeWithLogging(() -> {
            submission.getFileMetadata().remove(fileMetadatum);
            fileMetadatumRepository.delete(fileMetadatum);
            return null;
        }, "Error removing file metadata from submission ID: " + submissionId);
    }

    @Override
    @Transactional
    public void removeGradeFromSubmission(Long submissionId, Long gradeId) {
        Submission submission = getEntityById(submissionId, submissionRepository, "Submission");
        Grade grade = getEntityById(gradeId, gradeRepository, "Grade");

        if (!submission.getGrades().contains(grade)) {
            throw new ResourceNotFoundException("Grade not found in this submission", submissionId);
        }

        executeWithLogging(() -> {
            submission.getGrades().remove(grade);
            gradeRepository.delete(grade);
            return null;
        }, "Error removing grade from submission ID: " + submissionId);
    }

    // --------------- MÉTODOS AUXILIARES -----------------

    private <T> T getEntityById(Long id, JpaRepository<T, Long> repository, String entityName) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(entityName, id));
    }

    private void updateSubmissionFields(Submission existingSubmission, SubmissionDTO submissionDTO) {
        existingSubmission.setFileName(submissionDTO.getFileName());
        existingSubmission.setGrade(submissionDTO.getGrade());
        existingSubmission.setComments(submissionDTO.getComments());
        existingSubmission.setState(submissionDTO.getState());
        existingSubmission.setLastUpdate(LocalDateTime.now());
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
