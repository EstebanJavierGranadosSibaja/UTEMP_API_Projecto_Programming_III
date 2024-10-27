package org.una.programmingIII.UTEMP_Project.services.CourseServices;

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
import org.una.programmingIII.UTEMP_Project.dtos.CourseDTO;
import org.una.programmingIII.UTEMP_Project.exceptions.ResourceNotFoundException;
import org.una.programmingIII.UTEMP_Project.models.*;
import org.una.programmingIII.UTEMP_Project.observers.Subject;
import org.una.programmingIII.UTEMP_Project.repositories.AssignmentRepository;
import org.una.programmingIII.UTEMP_Project.repositories.CourseRepository;
import org.una.programmingIII.UTEMP_Project.repositories.DepartmentRepository;
import org.una.programmingIII.UTEMP_Project.repositories.UserRepository;
import org.una.programmingIII.UTEMP_Project.services.EmailNotificationObserver;
import org.una.programmingIII.UTEMP_Project.services.NotificationServices.NotificationService;
import org.una.programmingIII.UTEMP_Project.transformers.mappers.GenericMapper;
import org.una.programmingIII.UTEMP_Project.transformers.mappers.GenericMapperFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

@Service
@Transactional
public class CourseServiceImplementation extends Subject<EmailNotificationObserver> implements CourseService {

    private static final Logger logger = LoggerFactory.getLogger(CourseServiceImplementation.class);

    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private AssignmentRepository assignmentRepository;
    @Autowired
    private NotificationService notificationService;

    private final GenericMapper<Course, CourseDTO> courseMapper;
    private final GenericMapper<Assignment, AssignmentDTO> assignmentMapper;

    @Autowired
    public CourseServiceImplementation(GenericMapperFactory mapperFactory) {
        this.courseMapper = mapperFactory.createMapper(Course.class, CourseDTO.class);
        this.assignmentMapper = mapperFactory.createMapper(Assignment.class, AssignmentDTO.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseDTO> getAllCourses() {
        return executeWithLogging(() -> courseMapper.convertToDTOList(courseRepository.findAll()),
                "Error fetching all courses");
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CourseDTO> getCourseById(Long id) {
        return executeWithLogging(() -> {
            Course course = getEntityById(id, courseRepository, "Course");
            return Optional.of(courseMapper.convertToDTO(course));
        }, "Error fetching course by ID");
    }

    @Override
    @Transactional
    public CourseDTO createCourse(@Valid CourseDTO courseDTO) {
        Course course = courseMapper.convertToEntity(courseDTO);
        course.setTeacher(getEntityById(courseDTO.getTeacher().getId(), userRepository, "Teacher"));
        course.setDepartment(getEntityById(courseDTO.getDepartment().getId(), departmentRepository, "Department"));
        course.setState(CourseState.ACTIVE);
        return executeWithLogging(() -> courseMapper.convertToDTO(courseRepository.save(course)),
                "Error creating course");
    }

    @Override
    @Transactional
    public Optional<CourseDTO> updateCourse(Long id, @Valid CourseDTO courseDTO) {
        Optional<Course> optionalCourse = courseRepository.findById(id);
        Course existingCourse = optionalCourse.orElseThrow(() -> new ResourceNotFoundException("Course", id));

        updateCourseFields(existingCourse, courseDTO);
        return executeWithLogging(() -> Optional.of(courseMapper.convertToDTO(courseRepository.save(existingCourse))),
                "Error updating course");
    }

    @Override
    @Transactional
    public void deleteCourse(Long id) {
        Course course = getEntityById(id, courseRepository, "Course");
        executeWithLogging(() -> {
            courseRepository.delete(course);
            return null;
        }, "Error deleting course");
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseDTO> getCoursesByTeacherId(Long teacherId) {
        User teacher = getEntityById(teacherId, userRepository, "Teacher");
        return executeWithLogging(() -> courseMapper.convertToDTOList(courseRepository.findByTeacher(teacher)),
                "Error fetching courses by teacher ID");
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseDTO> getCoursesByDepartmentId(Long departmentId) {
        Department department = getEntityById(departmentId, departmentRepository, "Department");
        return executeWithLogging(() -> courseMapper.convertToDTOList(courseRepository.findByDepartment(department)),
                "Error fetching courses by department ID");
    }

    @Override
    @Transactional
    public void addAssignmentToCourse(Long courseId, AssignmentDTO assignmentDTO) {
        Course course = getEntityById(courseId, courseRepository, "Course");
        Assignment assignment = assignmentMapper.convertToEntity(assignmentDTO);

        assignment.setCourse(course);
        course.getAssignment().add(assignment);

        executeWithLogging(() -> {
            assignmentRepository.save(assignment);
            sendMailToAllStudents(course.getEnrollments(), assignment);
            return null;
        }, "Error adding assignment to course");
    }

    @Override
    @Transactional
    public void removeAssignmentFromCourse(Long courseId, Long assignmentId) {
        Course course = getEntityById(courseId, courseRepository, "Course");
        Assignment assignment = getEntityById(assignmentId, assignmentRepository, "Assignment");

        if (course.getAssignment().contains(assignment)) {
            course.getAssignment().remove(assignment);
            assignment.setCourse(null);

            executeWithLogging(() -> {
                assignmentRepository.delete(assignment);
                return null;
            }, "Error removing assignment from course");
        } else {
            throw new ResourceNotFoundException("Assignment not found in this course", courseId);
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

    private void updateCourseFields(Course existingCourse, CourseDTO courseDTO) {
        existingCourse.setName(courseDTO.getName());
        existingCourse.setDescription(courseDTO.getDescription());
        existingCourse.setState(courseDTO.getState());
        existingCourse.setLastUpdate(LocalDateTime.now());
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
    protected CompletableFuture<Void> sendMailToAllStudents(List<Enrollment> enrollments, Assignment assignment) {
        String message = "Assignment '" + assignment.getTitle() +
                "' was added in the " + assignment.getCourse().getName() + " course";

        for (Enrollment enrollment : enrollments) {
            try {
                notifyObservers("NEW_ASSIGNMENT", message, enrollment.getStudent().getEmail());
                notificationService.sendNotificationToUser(enrollment.getStudent(), message);
            } catch (Exception e) {
                logger.error("Error notifying student {}: {}", enrollment.getStudent().getEmail(), e.getMessage());
            }
        }
        return CompletableFuture.completedFuture(null);
    }
}
