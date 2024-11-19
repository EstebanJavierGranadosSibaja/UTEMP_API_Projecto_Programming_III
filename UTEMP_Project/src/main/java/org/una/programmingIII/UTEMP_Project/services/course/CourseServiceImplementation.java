package org.una.programmingIII.UTEMP_Project.services.course;

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
import org.una.programmingIII.UTEMP_Project.dtos.CourseDTO;
import org.una.programmingIII.UTEMP_Project.exceptions.InvalidDataException;
import org.una.programmingIII.UTEMP_Project.exceptions.ResourceNotFoundException;
import org.una.programmingIII.UTEMP_Project.models.Assignment;
import org.una.programmingIII.UTEMP_Project.models.Course;
import org.una.programmingIII.UTEMP_Project.models.Enrollment;
import org.una.programmingIII.UTEMP_Project.observers.Subject;
import org.una.programmingIII.UTEMP_Project.repositories.AssignmentRepository;
import org.una.programmingIII.UTEMP_Project.repositories.CourseRepository;
import org.una.programmingIII.UTEMP_Project.repositories.DepartmentRepository;
import org.una.programmingIII.UTEMP_Project.repositories.UserRepository;
import org.una.programmingIII.UTEMP_Project.services.EmailNotificationObserver;
import org.una.programmingIII.UTEMP_Project.services.notification.NotificationService;
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

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final AssignmentRepository assignmentRepository;
    private final NotificationService notificationService;

    private final GenericMapper<Course, CourseDTO> courseMapper;
    private final GenericMapper<Assignment, AssignmentDTO> assignmentMapper;

    @Autowired
    public CourseServiceImplementation(
            CourseRepository courseRepository,
            UserRepository userRepository,
            DepartmentRepository departmentRepository,
            AssignmentRepository assignmentRepository,
            NotificationService notificationService,
            GenericMapperFactory mapperFactory) {

        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.assignmentRepository = assignmentRepository;
        this.notificationService = notificationService;
        this.courseMapper = mapperFactory.createMapper(Course.class, CourseDTO.class);
        this.assignmentMapper = mapperFactory.createMapper(Assignment.class, AssignmentDTO.class);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CourseDTO> getAllCourses(Pageable pageable) {
        try {
            return executeWithLogging(() -> {
                Page<Course> coursePage = courseRepository.findAll(pageable);
                Page<CourseDTO> AllCoursePageDTO = coursePage.map(courseMapper::convertToDTO);
                for (int i = 0; i < AllCoursePageDTO.getNumberOfElements(); i++) {
                    AllCoursePageDTO.getContent().get(i).setUserTeacherUniqueID(coursePage.getContent().get(i).getTeacher().getId());
                    AllCoursePageDTO.getContent().get(i).setDepartmentUniqueID(coursePage.getContent().get(i).getDepartment().getId());
                    AllCoursePageDTO.getContent().get(i).setDepartmentUniqueName(coursePage.getContent().get(i).getDepartment().getName());
                }
                return AllCoursePageDTO;
            }, "Error fetching all courses");
        } catch (DataAccessException e) {
            logger.error("Database access error occurred while fetching all courses: {}", e.getMessage());
            throw new InvalidDataException("Error fetching courses from the database");
        } catch (Exception e) {
            logger.error("Unexpected error occurred while fetching all courses: {}", e.getMessage());
            throw new InvalidDataException("Error fetching all courses");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CourseDTO> getCourseById(Long id) {
        try {
            return executeWithLogging(() -> {
                Course course = getEntityById(id, courseRepository, "Course");
                return Optional.of(courseMapper.convertToDTO(course));
            }, "Error fetching course by ID");
        } catch (ResourceNotFoundException e) {
            logger.error("Course with ID {} not found: {}", id, e.getMessage());
            return Optional.empty();
        } catch (InvalidDataException e) {
            logger.error("Invalid data provided for course ID {}: {}", id, e.getMessage());
            return Optional.empty();
        } catch (Exception e) {
            logger.error("An unexpected error occurred while fetching course ID {}: {}", id, e.getMessage());
            throw new RuntimeException("An unexpected error occurred while fetching the course.", e);
        }
    }

    @Override
    @Transactional
    public CourseDTO createCourse(@Valid CourseDTO courseDTO) {
        try {
            Course course = courseMapper.convertToEntity(courseDTO);
            course.setTeacher(getEntityById(/*courseDTO.getTeacher().getId()*/43L, userRepository, "Teacher"));
            course.setDepartment(getEntityById(courseDTO.getDepartment().getId(), departmentRepository, "Department"));
            return executeWithLogging(() -> courseMapper.convertToDTO(courseRepository.save(course)),
                    "Error creating course");
        } catch (ResourceNotFoundException e) {
            logger.error("Resource not found while creating course: {}", e.getMessage());
            throw new InvalidDataException("Failed to create course: " + e.getMessage());
        } catch (InvalidDataException e) {
            logger.error("Invalid data provided for course: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("An unexpected error occurred while creating a course: {}", e.getMessage());
            throw new RuntimeException("An unexpected error occurred while creating the course.", e);
        }
    }

    @Override
    @Transactional
    public Optional<CourseDTO> updateCourse(Long id, @Valid CourseDTO courseDTO) {
        try {
            Optional<Course> optionalCourse = courseRepository.findById(id);
            Course existingCourse = optionalCourse.orElseThrow(() -> new ResourceNotFoundException("Course", id));
            updateCourseFields(existingCourse, courseDTO);

            return executeWithLogging(() -> Optional.of(courseMapper.convertToDTO(courseRepository.save(existingCourse))),
                    "Error updating course");
        } catch (ResourceNotFoundException e) {
            logger.error("Resource not found while updating course: {}", e.getMessage());
            throw e;
        } catch (InvalidDataException e) {
            logger.error("Invalid data provided for course update: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("An unexpected error occurred while updating course ID {}: {}", id, e.getMessage());
            throw new RuntimeException("An unexpected error occurred while updating the course.", e);
        }
    }

    @Override
    @Transactional
    public void deleteCourse(Long id) {
        try {
            Course course = getEntityById(id, courseRepository, "Course");

            executeWithLogging(() -> {
                courseRepository.delete(course);
                return null;
            }, "Error deleting course");

        } catch (ResourceNotFoundException e) {
            logger.error("Resource not found while deleting course: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("An unexpected error occurred while deleting course ID {}: {}", id, e.getMessage());
            throw new RuntimeException("An unexpected error occurred while deleting the course.", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CourseDTO> getCoursesByTeacherId(Long teacherId, Pageable pageable) {
        try {
            return executeWithLogging(() -> {
                Page<Course> coursePage = courseRepository.findByTeacherId(teacherId, pageable);
                return coursePage.map(courseMapper::convertToDTO);
            }, "Error fetching courses by teacher ID");
        } catch (DataAccessException e) {
            logger.error("Database access error occurred while fetching courses by teacher ID: {}", e.getMessage());
            throw new InvalidDataException("Error fetching courses from the database");
        } catch (Exception e) {
            logger.error("Unexpected error occurred while fetching courses by teacher ID: {}", e.getMessage());
            throw new InvalidDataException("Error fetching courses by teacher ID");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CourseDTO> getCoursesByDepartmentId(Long departmentId, Pageable pageable) {
        try {
            return executeWithLogging(() -> {
                Page<Course> coursePage = courseRepository.findByDepartmentIdWithTeacher(departmentId, pageable);
                Page<CourseDTO> coursePageDTO = coursePage.map(courseMapper::convertToDTO);
                for (int i = 0; i < coursePageDTO.getNumberOfElements(); i++) {
                    coursePageDTO.getContent().get(i).setUserTeacherUniqueID(coursePage.getContent().get(i).getTeacher().getId());
                    coursePageDTO.getContent().get(i).setDepartmentUniqueID(coursePage.getContent().get(i).getDepartment().getId());
                    coursePageDTO.getContent().get(i).setDepartmentUniqueName(coursePage.getContent().get(i).getDepartment().getName());
                }
                return coursePageDTO;
            }, "Error fetching courses by department ID");
        } catch (DataAccessException e) {
            logger.error("Database access error occurred while fetching courses by department ID {}: {}", departmentId, e.getMessage());
            throw new InvalidDataException("Error fetching courses from the database");
        } catch (Exception e) {
            logger.error("Unexpected error occurred while fetching courses by department ID {}: {}", departmentId, e.getMessage());
            throw new InvalidDataException("Error fetching courses");
        }
    }

    @Override
    @Transactional
    public void addAssignmentToCourse(Long courseId, AssignmentDTO assignmentDTO) {
        try {
            Course course = getEntityById(courseId, courseRepository, "Course");
            Assignment assignment = assignmentMapper.convertToEntity(assignmentDTO);
            assignment.setCourse(course);
            course.getAssignments().add(assignment);

            executeWithLogging(() -> {
                assignmentRepository.save(assignment);
                sendMailToAllStudents(course.getEnrollments(), assignment);
                return null;
            }, "Error adding assignment to course");
        } catch (ResourceNotFoundException e) {
            logger.error("Resource not found while adding assignment to course: {}", e.getMessage());
            throw e;
        } catch (InvalidDataException e) {
            logger.error("Invalid data provided for assignment: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("An unexpected error occurred while adding assignment to course ID {}: {}", courseId, e.getMessage());
            throw new RuntimeException("An unexpected error occurred while adding the assignment to the course.", e);
        }
    }

    @Override
    @Transactional
    public void removeAssignmentFromCourse(Long courseId, Long assignmentId) {
        try {
            Course course = getEntityById(courseId, courseRepository, "Course");
            Assignment assignment = getEntityById(assignmentId, assignmentRepository, "Assignment");

            if (course.getAssignments().contains(assignment)) {
                course.getAssignments().remove(assignment);
                assignment.setCourse(null);

                executeWithLogging(() -> {
                    assignmentRepository.delete(assignment);
                    return null;
                }, "Error removing assignment from course");
            } else {
                throw new ResourceNotFoundException("Assignment not found in this course", courseId);
            }
        } catch (ResourceNotFoundException e) {
            logger.error("Resource not found while removing assignment from course: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("An unexpected error occurred while removing assignment ID {} from course ID {}: {}", assignmentId, courseId, e.getMessage());
            throw new RuntimeException("An unexpected error occurred while removing the assignment from the course.", e);
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
    protected void sendMailToAllStudents(List<Enrollment> enrollments, Assignment assignment) {
        String message = "Assignment '" + assignment.getTitle() +
                "' was added in the " + assignment.getCourse().getName() + " course";

        for (Enrollment enrollment : enrollments) {
            try {
                notifyObservers("NEW_ASSIGNMENT", message, enrollment.getStudent().getEmail());
                notificationService.sendNotificationToUser(enrollment.getStudent().getId(), message);
            } catch (Exception e) {
                logger.error("Error notifying student {}: {}", enrollment.getStudent().getEmail(), e.getMessage());
            }
        }
        CompletableFuture.completedFuture(null);
    }
}