package org.una.programmingIII.UTEMP_Project.services.assignment;

import jakarta.validation.Valid;
import org.hibernate.service.spi.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.una.programmingIII.UTEMP_Project.dtos.AssignmentDTO;
import org.una.programmingIII.UTEMP_Project.dtos.SubmissionDTO;
import org.una.programmingIII.UTEMP_Project.exceptions.ResourceNotFoundException;
import org.una.programmingIII.UTEMP_Project.models.Assignment;
import org.una.programmingIII.UTEMP_Project.models.Course;
import org.una.programmingIII.UTEMP_Project.models.Submission;
import org.una.programmingIII.UTEMP_Project.observers.Subject;
import org.una.programmingIII.UTEMP_Project.repositories.AssignmentRepository;
import org.una.programmingIII.UTEMP_Project.repositories.CourseRepository;
import org.una.programmingIII.UTEMP_Project.repositories.SubmissionRepository;
import org.una.programmingIII.UTEMP_Project.services.EmailNotificationObserver;
import org.una.programmingIII.UTEMP_Project.services.NotificationServices.NotificationService;
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

    @Autowired
    private AssignmentRepository assignmentRepository;
    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private SubmissionRepository submissionRepository;
    @Autowired
    private NotificationService notificationService;

    private final GenericMapper<Assignment, AssignmentDTO> assignmentMapper;
    private final GenericMapper<Submission, SubmissionDTO> submissionMapper;

    @Autowired
    public AssignmentServiceImplementation(GenericMapperFactory mapperFactory) {
        this.assignmentMapper = mapperFactory.createMapper(Assignment.class, AssignmentDTO.class);
        this.submissionMapper = mapperFactory.createMapper(Submission.class, SubmissionDTO.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssignmentDTO> getAllAssignments() {
        return executeWithLogging(() -> assignmentMapper.convertToDTOList(assignmentRepository.findAll()),
                "Error fetching all assignments");
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AssignmentDTO> getAssignmentById(Long id) {
        return executeWithLogging(() -> {
            Assignment assignment = getEntityById(id, assignmentRepository, "Assignment");
            return Optional.of(assignmentMapper.convertToDTO(assignment));
        }, "Error fetching assignment by ID");
    }

    @Override
    @Transactional
    public AssignmentDTO createAssignment(@Valid AssignmentDTO assignmentDTO) {
        Assignment assignment = assignmentMapper.convertToEntity(assignmentDTO);
        assignment.setCourse(getEntityById(assignmentDTO.getCourse().getId(), courseRepository, "Course"));
        return executeWithLogging(() -> assignmentMapper.convertToDTO(assignmentRepository.save(assignment)),
                "Error creating assignment");
    }

    @Override
    @Transactional
    public Optional<AssignmentDTO> updateAssignment(Long id, @Valid AssignmentDTO assignmentDTO) {
        Optional<Assignment> optionalAssignment = assignmentRepository.findById(id);
        Assignment existingAssignment = optionalAssignment.orElseThrow(() -> new ResourceNotFoundException("Assignment", id));

        updateAssignmentFields(existingAssignment, assignmentDTO);
        return executeWithLogging(() -> Optional.of(assignmentMapper.convertToDTO(assignmentRepository.save(existingAssignment))),
                "Error updating assignment");
    }

    @Override
    @Transactional
    public void deleteAssignment(Long id) {
        Assignment assignment = getEntityById(id, assignmentRepository, "Assignment");
        executeWithLogging(() -> {
            assignmentRepository.delete(assignment);
            return null;
        }, "Error deleting assignment");
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssignmentDTO> getAssignmentsByCourseId(Long courseId) {
        Course course = getEntityById(courseId, courseRepository, "Course");
        return executeWithLogging(() -> assignmentMapper.convertToDTOList(assignmentRepository.findByCourse(course)),
                "Error fetching assignments by course ID");
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubmissionDTO> getSubmissionsByAssignmentId(Long assignmentId) {
        Assignment assignment = getEntityById(assignmentId, assignmentRepository, "Assignment");
        return executeWithLogging(() -> submissionMapper.convertToDTOList(assignment.getSubmissions()),
                "Error fetching submissions for assignment ID: " + assignmentId);
    }

    @Override
    @Transactional
    public SubmissionDTO addSubmissionToAssignment(Long assignmentId, @Valid SubmissionDTO submissionDTO) {
        Assignment assignment = getEntityById(assignmentId, assignmentRepository, "Assignment");
        Submission submission = submissionMapper.convertToEntity(submissionDTO);
        submission.setAssignment(assignment);

        return executeWithLogging(() -> {
            Submission savedSubmission = submissionRepository.save(submission);
            assignment.getSubmissions().add(savedSubmission);
            sendNotificationForSubmission(assignment, submission);
            return submissionMapper.convertToDTO(savedSubmission);
        }, "Error adding submission to assignment ID: " + assignmentId);
    }

    @Override
    @Transactional
    public void deleteSubmissionFromAssignment(Long assignmentId, Long submissionId) {
        Assignment assignment = getEntityById(assignmentId, assignmentRepository, "Assignment");
        Submission submission = getEntityById(submissionId, submissionRepository, "Submission");

        if (!assignment.getSubmissions().contains(submission)) {
            throw new ResourceNotFoundException("Submission not found in this assignment", assignmentId);
        }

        executeWithLogging(() -> {
            assignment.getSubmissions().remove(submission);
            submissionRepository.delete(submission);
            return null;
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
            notificationService.sendNotificationToUser(assignment.getCourse().getTeacher(), message);
        } catch (Exception e) {
            logger.error("Error notifying teacher {}: {}", assignment.getCourse().getTeacher().getEmail(), e.getMessage());
        }
    }
}
