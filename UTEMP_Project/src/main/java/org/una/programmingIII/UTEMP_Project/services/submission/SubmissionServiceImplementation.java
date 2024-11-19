package org.una.programmingIII.UTEMP_Project.services.submission;

import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import org.hibernate.service.spi.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.una.programmingIII.UTEMP_Project.dtos.FileMetadatumDTO;
import org.una.programmingIII.UTEMP_Project.dtos.GradeDTO;
import org.una.programmingIII.UTEMP_Project.dtos.SubmissionDTO;
import org.una.programmingIII.UTEMP_Project.exceptions.InvalidDataException;
import org.una.programmingIII.UTEMP_Project.exceptions.ResourceNotFoundException;
import org.una.programmingIII.UTEMP_Project.models.*;
import org.una.programmingIII.UTEMP_Project.observers.Subject;
import org.una.programmingIII.UTEMP_Project.repositories.*;
import org.una.programmingIII.UTEMP_Project.services.notification.NotificationService;
import org.una.programmingIII.UTEMP_Project.transformers.mappers.GenericMapper;
import org.una.programmingIII.UTEMP_Project.transformers.mappers.GenericMapperFactory;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.function.Supplier;

@Service
@Transactional
public class SubmissionServiceImplementation extends Subject implements SubmissionService {

    private static final Logger logger = LoggerFactory.getLogger(SubmissionServiceImplementation.class);

    private final SubmissionRepository submissionRepository;
    private final AssignmentRepository assignmentRepository;
    private final FileMetadatumRepository fileMetadatumRepository;
    private final GradeRepository gradeRepository;
    private final NotificationService notificationService;

    private final GenericMapper<Submission, SubmissionDTO> submissionMapper;
    private final GenericMapper<FileMetadatum, FileMetadatumDTO> fileMetadatumMapper;
    private final GenericMapper<Grade, GradeDTO> gradeMapper;
    private final UserRepository userRepository;

    @Autowired
    public SubmissionServiceImplementation(SubmissionRepository submissionRepository, AssignmentRepository assignmentRepository,
                                           FileMetadatumRepository fileMetadatumRepository, GradeRepository gradeRepository, NotificationService notificationService,
                                           GenericMapperFactory mapperFactory, UserRepository userRepository) {

        this.submissionRepository = submissionRepository;
        this.assignmentRepository = assignmentRepository;
        this.fileMetadatumRepository = fileMetadatumRepository;
        this.gradeRepository = gradeRepository;
        this.notificationService = notificationService;
        this.submissionMapper = mapperFactory.createMapper(Submission.class, SubmissionDTO.class);
        this.fileMetadatumMapper = mapperFactory.createMapper(FileMetadatum.class, FileMetadatumDTO.class);
        this.gradeMapper = mapperFactory.createMapper(Grade.class, GradeDTO.class);
        this.userRepository = userRepository;
    }


    @Override
    @Transactional(readOnly = true)
    public Page<SubmissionDTO> getAllSubmissions(Pageable pageable) {
        try {
            return executeWithLogging(() -> {
                Page<Submission> submissionPage = submissionRepository.findAll(pageable);
                return submissionPage.map(submissionMapper::convertToDTO);
            }, "Error fetching all submissions");
        } catch (DataAccessException e) {
            logger.error("Database access error occurred while fetching all submissions: {}", e.getMessage());
            throw new InvalidDataException("Error fetching submissions from the database");
        } catch (Exception e) {
            logger.error("Unexpected error occurred while fetching all submissions: {}", e.getMessage());
            throw new InvalidDataException("Error fetching all submissions");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SubmissionDTO> getSubmissionById(Long id) {
        try {
            return executeWithLogging(() -> {
                Submission submission = getEntityById(id, submissionRepository, "Submission");
                return Optional.of(submissionMapper.convertToDTO(submission));
            }, "Error fetching submission by ID");
        } catch (ResourceNotFoundException e) {
            logger.warn("Submission not found with ID {}: {}", id, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error fetching submission with ID {}: {}", id, e.getMessage());
            throw new ServiceException("Error fetching submission", e);
        }
    }

    @Override
    @Transactional
    public SubmissionDTO createSubmission(@Valid SubmissionDTO submissionDTO) {

        try {
            // Convierte el DTO a entidad
//            System.out.println(submissionDTO.toString());
            Submission submission = submissionMapper.convertToEntity(submissionDTO);
            submission.setFileName("desconocido");
            User user = getEntityById(submissionDTO.getStudent().getId(), userRepository, "User");

            submission.setAssignment(getEntityById(submissionDTO.getAssignment().getId(), assignmentRepository, "Assignment"));
            submission.setStudent(user);

            submission = submissionRepository.save(submission);

//            FileMetadatum file = FileMetadatum.builder()
//                    .submission(submission)
//                    .student(user)
//                    .fileName("desconocido")
//                    .fileType("desconocido")
//                    .storagePath(fileBasePath)
//                    .fileSize(0L)
//                    .build();


//            List<FileMetadatum> list = new ArrayList<>();

//            list.add(fileMetadataRepository.save(file));

//            submission.setFileMetadata(list);

            // Guarda la entidad Submission y convierte la entidad a DTO para devolverla
            Submission finalSubmission = submission;

            return executeWithLogging(() -> submissionMapper.convertToDTO(finalSubmission), "Error creating submission");
        } catch (ResourceNotFoundException e) {
            // Manejo de excepciones cuando no se encuentra el Assignment
            logger.warn("Failed to create submission: Assignment not found with ID {}: {}", submissionDTO.getAssignment().getId(), e.getMessage());
            throw e;
        } catch (ValidationException e) {
            // Manejo de excepciones de validación
            logger.warn("Validation error while creating submission: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            // Manejo de excepciones generales
            logger.error("Error creating submission: {}", e.getMessage());
            throw new ServiceException("Error creating submission", e);
        }
    }

    @Override
    @Transactional
    public Optional<SubmissionDTO> updateSubmission(Long id, @Valid SubmissionDTO submissionDTO) {
        try {
            Optional<Submission> optionalSubmission = submissionRepository.findById(id);
            Submission existingSubmission = optionalSubmission.orElseThrow(() -> new ResourceNotFoundException("Submission", id));
            updateSubmissionFields(existingSubmission, submissionDTO);
            return executeWithLogging(() -> Optional.of(submissionMapper.convertToDTO(submissionRepository.save(existingSubmission))),
                    "Error updating submission");
        } catch (ResourceNotFoundException e) {
            logger.warn("Failed to update submission: Submission not found with ID {}: {}", id, e.getMessage());
            throw e;
        } catch (ValidationException e) {
            logger.warn("Validation error while updating submission: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error updating submission: {}", e.getMessage());
            throw new ServiceException("Error updating submission", e);
        }
    }

    @Override
    @Transactional
    public void deleteSubmission(Long id) {
        try {
            Submission submission = getEntityById(id, submissionRepository, "Submission");
            executeWithLogging(() -> {
                submissionRepository.delete(submission);
                return null;
            }, "Error deleting submission");

        } catch (ResourceNotFoundException e) {
            logger.warn("Failed to delete submission: Submission not found with ID {}: {}", id, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error deleting submission: {}", e.getMessage());
            throw new ServiceException("Error deleting submission", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SubmissionDTO> getSubmissionsByAssignmentId(Long assignmentId, Pageable pageable) {
        try {
            if (!assignmentRepository.existsById(assignmentId)) {
                throw new ResourceNotFoundException("Assignment", assignmentId);
            }
            return executeWithLogging(() -> {
                Page<Submission> submissions = submissionRepository.findByAssignmentId(assignmentId, pageable);
                return submissions.map(submissionMapper::convertToDTO);
            }, "Error fetching submissions for assignment ID: " + assignmentId);
        } catch (ResourceNotFoundException e) {
            logger.warn("Failed to fetch submissions: Assignment not found with ID {}: {}", assignmentId, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error fetching submissions for assignment ID {}: {}", assignmentId, e.getMessage());
            throw new ServiceException("Error fetching submissions", e);
        }
    }

    @Override
    @Transactional
    public FileMetadatumDTO addFileMetadatumToSubmission(Long submissionId, @Valid FileMetadatumDTO fileMetadatumDTO) {
        try {
            Submission submission = getEntityById(submissionId, submissionRepository, "Submission");
            FileMetadatum fileMetadatum = fileMetadatumMapper.convertToEntity(fileMetadatumDTO);
            fileMetadatum.setSubmission(submission); // Establecer la relación

            return executeWithLogging(() -> {
                FileMetadatum savedFileMetadatum = fileMetadatumRepository.save(fileMetadatum);
                submission.getFileMetadata().add(savedFileMetadatum);
                return fileMetadatumMapper.convertToDTO(savedFileMetadatum);
            }, "Error adding file metadata to submission ID: " + submissionId);
        } catch (ResourceNotFoundException e) {
            logger.warn("Failed to add file metadata: Submission not found with ID {}: {}", submissionId, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error adding file metadata to submission ID {}: {}", submissionId, e.getMessage());
            throw new ServiceException("Error adding file metadata", e);
        }
    }

    @Override
    @Transactional
    public GradeDTO addGradeToSubmission(Long submissionId, @Valid GradeDTO gradeDTO) {
        Submission submission = getEntityById(submissionId, submissionRepository, "Submission");
        boolean gradeExists = submission.getGrades().stream()
                .anyMatch(existingGrade -> existingGrade.getSubmission().equals(submission));
        if (gradeExists) {
            throw new InvalidDataException("A grade for this submission already exists.");
        }
        Grade grade = gradeMapper.convertToEntity(gradeDTO);
        grade.setSubmission(submission);

        return executeWithLogging(() -> {
            Grade savedGrade = gradeRepository.save(grade);
            submission.getGrades().add(savedGrade);
            notifyAboutGrade(savedGrade, submission);
            logger.info("Grade added successfully to submission ID: {}", submissionId);
            return gradeMapper.convertToDTO(savedGrade);
        }, "Error adding grade to submission ID: " + submissionId);
    }

    @Override
    @Transactional
    public void removeFileMetadatumFromSubmission(Long submissionId, Long fileMetadatumId) {
        try {
            Submission submission = getEntityById(submissionId, submissionRepository, "Submission");
            FileMetadatum fileMetadatum = getEntityById(fileMetadatumId, fileMetadatumRepository, "FileMetadatum");
            if (!submission.getFileMetadata().contains(fileMetadatum)) {
                throw new ResourceNotFoundException("File metadata not found in this submission", fileMetadatumId);
            }
            executeWithLogging(() -> {
                submission.getFileMetadata().remove(fileMetadatum);
                fileMetadatumRepository.delete(fileMetadatum);
                return null;
            }, "Error removing file metadata from submission ID: " + submissionId);
        } catch (ResourceNotFoundException e) {
            logger.warn("Failed to remove file metadata: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error removing file metadata from submission ID {}: {}", submissionId, e.getMessage());
            throw new ServiceException("Error removing file metadata from submission", e);
        }
    }

    @Override
    @Transactional
    public void removeGradeFromSubmission(Long submissionId, Long gradeId) {
        try {
            Submission submission = getEntityById(submissionId, submissionRepository, "Submission");
            Grade grade = getEntityById(gradeId, gradeRepository, "Grade");

            if (!submission.getGrades().contains(grade)) {
                throw new ResourceNotFoundException("Grade not found in this submission", gradeId);
            }
            executeWithLogging(() -> {
                submission.getGrades().remove(grade);
                gradeRepository.delete(grade);
                return null;
            }, "Error removing grade from submission ID: " + submissionId);
        } catch (ResourceNotFoundException e) {
            logger.warn("Failed to remove grade: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error removing grade from submission ID {}: {}", submissionId, e.getMessage());
            throw new ServiceException("Error removing grade from submission", e);
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

    @Async("taskExecutor")
    protected void sendNotificationForGrade(Grade grade, Submission submission) {
        String message = "The grade of the assigment '" +
                submission.getAssignment().getTitle() + "' was " + grade.getGrade();
        try {
            notifyObservers("SUBMISSION_GRADED", message, submission.getStudent().getEmail());
            notificationService.sendNotificationToUser(submission.getStudent().getId(), message);

        } catch (Exception e) {
            logger.error("Error notifying student {}: {}", submission.getStudent().getEmail(), e.getMessage());
        }
    }


    @Override
    public Optional<Grade> manualReviewSubmission(Long submissionId, double gradeValue, String comments) {
        validateGradeValue(gradeValue);
        validateComments(comments);

        Optional<Grade> gradeOptional = gradeRepository.findBySubmissionId(submissionId);

        if (gradeOptional.isPresent()) {
            Grade grade = gradeOptional.get();
            grade.setGrade(gradeValue);
            grade.setComments(comments);
            grade.setReviewedByAi(false);
            grade.setState(GradeState.FINALIZED);
            gradeRepository.save(grade);

            logger.info("Submission reviewed successfully: Submission ID = {}, Grade = {}, Comments = {}", submissionId, gradeValue, comments);
            return Optional.of(grade);
        } else {
            logger.error("Grade not found for submission ID: {}", submissionId);
            throw new ResourceNotFoundException("No grade found for submission ID: " + submissionId, submissionId);
        }
    }

    private void validateGradeValue(double gradeValue) {
        if (gradeValue < 0 || gradeValue > 10) {
            throw new InvalidDataException("Grade value must be between 0 and 10.");
        }
    }

    private void validateComments(String comments) {
        if (comments == null || comments.trim().isEmpty()) {
            throw new InvalidDataException("Comments cannot be null or empty.");
        }
    }

    @Async("taskExecutor")
    protected void notifyAboutGrade(Grade savedGrade, Submission submission) {
        String message = "A new grade has been added for the submission by " +
                submission.getStudent().getName() +
                " in the assignment " + submission.getAssignment().getTitle();
        try {
            notifyObservers("GRADE_NOTIFICATION", message, submission.getAssignment().getCourse().getTeacher().getEmail());
            notificationService.sendNotificationToUser(submission.getAssignment().getCourse().getTeacher().getId(), message);
            logger.info("Notification sent for grade to teacher ID: {}", submission.getAssignment().getCourse().getTeacher().getId());
        } catch (Exception e) {
            logger.error("Error notifying teacher {}: {}", submission.getAssignment().getCourse().getTeacher().getEmail(), e.getMessage());
        }
    }
}
