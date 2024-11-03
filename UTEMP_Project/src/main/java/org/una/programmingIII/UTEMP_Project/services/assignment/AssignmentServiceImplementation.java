package org.una.programmingIII.UTEMP_Project.services.assignment;

import jakarta.validation.Valid;
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
import org.una.programmingIII.UTEMP_Project.dtos.AssignmentDTO;
import org.una.programmingIII.UTEMP_Project.dtos.SubmissionDTO;
import org.una.programmingIII.UTEMP_Project.exceptions.InvalidDataException;
import org.una.programmingIII.UTEMP_Project.exceptions.ResourceNotFoundException;
import org.una.programmingIII.UTEMP_Project.models.Assignment;
import org.una.programmingIII.UTEMP_Project.models.Course;
import org.una.programmingIII.UTEMP_Project.models.Submission;
import org.una.programmingIII.UTEMP_Project.observers.Subject;
import org.una.programmingIII.UTEMP_Project.repositories.AssignmentRepository;
import org.una.programmingIII.UTEMP_Project.repositories.CourseRepository;
import org.una.programmingIII.UTEMP_Project.repositories.SubmissionRepository;
import org.una.programmingIII.UTEMP_Project.services.EmailNotificationObserver;
import org.una.programmingIII.UTEMP_Project.services.autoReview.AutoReviewService;
import org.una.programmingIII.UTEMP_Project.services.notification.NotificationService;
import org.una.programmingIII.UTEMP_Project.transformers.mappers.GenericMapper;
import org.una.programmingIII.UTEMP_Project.transformers.mappers.GenericMapperFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@Service
@Transactional
public class AssignmentServiceImplementation extends Subject<EmailNotificationObserver> implements AssignmentService {

    private static final Logger logger = LoggerFactory.getLogger(AssignmentServiceImplementation.class);

    private final AssignmentRepository assignmentRepository;
    private final CourseRepository courseRepository;
    private final SubmissionRepository submissionRepository;
    private final NotificationService notificationService;
    private final AutoReviewService autoReviewService;

    private final GenericMapper<Assignment, AssignmentDTO> assignmentMapper;
    private final GenericMapper<Submission, SubmissionDTO> submissionMapper;

    @Autowired
    public AssignmentServiceImplementation(
            AssignmentRepository assignmentRepository,
            CourseRepository courseRepository,
            SubmissionRepository submissionRepository,
            NotificationService notificationService,
            AutoReviewService autoReviewService,
            GenericMapperFactory mapperFactory) {

        this.assignmentRepository = assignmentRepository;
        this.courseRepository = courseRepository;
        this.submissionRepository = submissionRepository;
        this.notificationService = notificationService;
        this.autoReviewService = autoReviewService;
        this.assignmentMapper = mapperFactory.createMapper(Assignment.class, AssignmentDTO.class);
        this.submissionMapper = mapperFactory.createMapper(Submission.class, SubmissionDTO.class);
    }
    @Override
    @Transactional(readOnly = true)
    public Page<AssignmentDTO> getAllAssignments(Pageable pageable) {
        try {
            return executeWithLogging(() -> {
                Page<Assignment> assignments = assignmentRepository.findAll(pageable);
                return assignments.map(assignmentMapper::convertToDTO);
            }, "Error fetching all assignments");
        } catch (DataAccessException e) {
            logger.error("Database access error occurred while fetching all assignments: {}", e.getMessage());
            throw new InvalidDataException("Error fetching assignments from the database");
        } catch (Exception e) {
            logger.error("Unexpected error occurred while fetching all assignments: {}", e.getMessage());
            throw new InvalidDataException("Error fetching all assignments");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AssignmentDTO> getAssignmentById(Long id) {
        try {
            return executeWithLogging(() -> {
                Assignment assignment = getEntityById(id, assignmentRepository, "Assignment");
                return Optional.of(assignmentMapper.convertToDTO(assignment));
            }, "Error fetching assignment by ID");
        } catch (ResourceNotFoundException e) {
            logger.warn("Assignment with ID {} not found: {}", id, e.getMessage());
            throw e;
        } catch (DataAccessException e) {
            logger.error("Database access error occurred while fetching assignment by ID {}: {}", id, e.getMessage());
            throw new InvalidDataException("Error accessing assignment data");
        } catch (Exception e) {
            logger.error("Unexpected error occurred while fetching assignment by ID {}: {}", id, e.getMessage());
            throw new InvalidDataException("Error fetching assignment by ID");
        }
    }

    @Override
    @Transactional
    public AssignmentDTO createAssignment(@Valid AssignmentDTO assignmentDTO) {
        return executeWithLogging(() -> {
            try {
                Assignment assignment = assignmentMapper.convertToEntity(assignmentDTO);

                Long courseId = assignmentDTO.getCourse().getId();
                Course course = getEntityById(courseId, courseRepository, "Course");
                assignment.setCourse(course);

                Assignment savedAssignment = assignmentRepository.save(assignment);
                return assignmentMapper.convertToDTO(savedAssignment);
            } catch (ResourceNotFoundException e) {
                logger.error("Error creating assignment: Course with ID {} not found", assignmentDTO.getCourse().getId());
                throw e;
            } catch (InvalidDataException e) {
                logger.error("Error creating assignment: Invalid data provided - {}", e.getMessage());
                throw e;
            } catch (Exception e) {
                logger.error("Unexpected error creating assignment: {}", e.getMessage());
                throw new RuntimeException("An unexpected error occurred while creating assignment", e);
            }
        }, "Error creating assignment");
    }

    @Override
    @Transactional
    public Optional<AssignmentDTO> updateAssignment(Long id, @Valid AssignmentDTO assignmentDTO) {
        return executeWithLogging(() -> {
            try {
                Assignment existingAssignment = assignmentRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Assignment", id));

                updateAssignmentFields(existingAssignment, assignmentDTO);

                Assignment updatedAssignment = assignmentRepository.save(existingAssignment);
                return Optional.of(assignmentMapper.convertToDTO(updatedAssignment));
            } catch (ResourceNotFoundException e) {
                logger.error("Error updating assignment: Assignment with ID {} not found", id);
                throw e;
            } catch (InvalidDataException e) {
                logger.error("Error updating assignment: Invalid data provided - {}", e.getMessage());
                throw e;
            } catch (Exception e) {
                logger.error("Unexpected error updating assignment: {}", e.getMessage());
                throw new RuntimeException("An unexpected error occurred while updating the assignment", e);
            }
        }, "Error updating assignment");
    }

    @Override
    @Transactional
    public void deleteAssignment(Long id) {
        executeWithLogging(() -> {
            try {
                Assignment assignment = getEntityById(id, assignmentRepository, "Assignment");

                assignmentRepository.delete(assignment);
                return null;
            } catch (ResourceNotFoundException e) {
                logger.error("Error deleting assignment: Assignment with ID {} not found", id);
                throw e;
            } catch (Exception e) {
                logger.error("Unexpected error deleting assignment: {}", e.getMessage());
                throw new RuntimeException("An unexpected error occurred while deleting the assignment", e);
            }
        }, "Error deleting assignment");
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AssignmentDTO> getAssignmentsByCourseId(Long courseId, Pageable pageable) {
        try {
            return executeWithLogging(() -> {
                Page<Assignment> assignments = assignmentRepository.findByCourseId(courseId, pageable);
                return assignments.map(assignmentMapper::convertToDTO);
            }, "Error fetching assignments by course ID");
        } catch (DataAccessException e) {
            logger.error("Database access error occurred while fetching assignments for course ID {}: {}", courseId, e.getMessage());
            throw new InvalidDataException("Error fetching assignments from the database");
        } catch (Exception e) {
            logger.error("Unexpected error occurred while fetching assignments for course ID {}: {}", courseId, e.getMessage());
            throw new InvalidDataException("Error fetching assignments by course ID");
        }
    }

    @Override
    @Transactional
    public SubmissionDTO addSubmissionToAssignment(Long assignmentId, @Valid SubmissionDTO submissionDTO) {
        Assignment assignment = getEntityById(assignmentId, assignmentRepository, "Assignment");
        Submission submission = submissionMapper.convertToEntity(submissionDTO);
        submission.setAssignment(assignment);

        return executeWithLogging(() -> {
            try {
                Submission savedSubmission = submissionRepository.save(submission);
                assignment.getSubmissions().add(savedSubmission);

                autoReviewService.autoReviewSubmission(savedSubmission.getId());

                sendNotificationForSubmission(assignment, submission);
                return submissionMapper.convertToDTO(savedSubmission);
            } catch (ResourceNotFoundException e) {
                logger.error("Error adding submission: Assignment with ID {} not found", assignmentId);
                throw e;
            } catch (InvalidDataException e) {
                logger.error("Invalid submission data: {}", e.getMessage());
                throw e;
            } catch (Exception e) {
                logger.error("Unexpected error adding submission: {}", e.getMessage());
                throw new RuntimeException("An unexpected error occurred while adding the submission", e);
            }
        }, "Error adding submission to assignment ID: " + assignmentId);
    }

    @Override
    @Transactional
    public void removeSubmissionFromAssignment(Long assignmentId, Long submissionId) {
        Assignment assignment = getEntityById(assignmentId, assignmentRepository, "Assignment");
        Submission submission = getEntityById(submissionId, submissionRepository, "Submission");

        if (!assignment.getSubmissions().contains(submission)) {
            throw new ResourceNotFoundException("Submission not found in this assignment", submissionId);
        }

        executeWithLogging(() -> {
            try {
                assignment.getSubmissions().remove(submission);
                submissionRepository.delete(submission);
                return null;
            } catch (ResourceNotFoundException e) {
                logger.error("Error deleting submission: Submission with ID {} not found", submissionId);
                throw e;
            } catch (Exception e) {
                logger.error("Unexpected error deleting submission: {}", e.getMessage());
                throw new RuntimeException("An unexpected error occurred while deleting the submission", e);
            }
        }, "Error deleting submission ID: " + submissionId + " from assignment ID: " + assignmentId);
    }

    // --------------- MÉTODOS AUXILIARES -----------------

    private <T> T getEntityById(Long id, JpaRepository<T, Long> repository, String entityName) {
        return findEntityById(id, repository)
                .orElseThrow(() -> new ResourceNotFoundException(entityName, id));
    }

    private <T> Optional<T> findEntityById(Long id, JpaRepository<T, Long> repository) {
        return repository.findById(id);
    }

    private void updateAssignmentFields(Assignment existingAssignment, AssignmentDTO assignmentDTO) {
        existingAssignment.setTitle(assignmentDTO.getTitle());
        existingAssignment.setDescription(assignmentDTO.getDescription());
        existingAssignment.setDeadline(assignmentDTO.getDeadline());
        existingAssignment.setState(assignmentDTO.getState());
        existingAssignment.setLastUpdate(LocalDateTime.now());
    }

    private <T> T executeWithLogging(Supplier<T> action, String errorMessage) {
        try {
            return action.get();
        } catch (ResourceNotFoundException e) {
            logger.warn("{}: {}", errorMessage, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("{}: {}", errorMessage, e.getMessage());
            throw new ServiceException(errorMessage, e);
        }
    }

    @Async("taskExecutor")
    protected void sendNotificationForSubmission(Assignment assignment, Submission submission) {
        String message = "The student " + submission.getStudent().getName() +
                " added a new submission in the assignment " + assignment.getTitle();
        try {
            notifyObservers("USER_SUBMISSION", message, assignment.getCourse().getTeacher().getEmail());
            notificationService.sendNotificationToUser(assignment.getCourse().getTeacher().getId(), message);
        } catch (Exception e) {
            logger.error("Error notifying teacher {}: {}", assignment.getCourse().getTeacher().getEmail(), e.getMessage());
        }
    }
}
