package org.una.programmingIII.UTEMP_Project.services.user;

import jakarta.validation.Valid;
import org.hibernate.service.spi.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.una.programmingIII.UTEMP_Project.dtos.*;
import org.una.programmingIII.UTEMP_Project.exceptions.InvalidDataException;
import org.una.programmingIII.UTEMP_Project.exceptions.ResourceAlreadyExistsException;
import org.una.programmingIII.UTEMP_Project.exceptions.ResourceNotFoundException;
import org.una.programmingIII.UTEMP_Project.models.*;
import org.una.programmingIII.UTEMP_Project.repositories.CourseRepository;
import org.una.programmingIII.UTEMP_Project.repositories.EnrollmentRepository;
import org.una.programmingIII.UTEMP_Project.repositories.NotificationRepository;
import org.una.programmingIII.UTEMP_Project.repositories.UserRepository;
import org.una.programmingIII.UTEMP_Project.services.passwordEncryption.PasswordEncryptionService;
import org.una.programmingIII.UTEMP_Project.transformers.mappers.GenericMapper;
import org.una.programmingIII.UTEMP_Project.transformers.mappers.GenericMapperFactory;
import org.una.programmingIII.UTEMP_Project.validators.UserValidator;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

@Service
@Transactional
public class UserServiceImplementation implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImplementation.class);

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final NotificationRepository notificationRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final PasswordEncryptionService passwordEncryptionService;
    private final UserValidator userValidator;

    private final GenericMapper<User, UserDTO> userMapper;
    private final GenericMapper<Notification, NotificationDTO> notificationMapper;
    private final GenericMapper<Enrollment, EnrollmentDTO> enrollmentMapper;
    private final GenericMapper<Course, CourseDTO> courseMapper;
    private final GenericMapper<Submission, SubmissionDTO> submissionMapper;

    @Autowired
    public UserServiceImplementation(GenericMapperFactory mapperFactory, UserRepository userRepository, CourseRepository courseRepository, NotificationRepository notificationRepository, EnrollmentRepository enrollmentRepository, PasswordEncryptionService passwordEncryptionService, UserValidator userValidator) {
        this.userMapper = mapperFactory.createMapper(User.class, UserDTO.class);
        this.notificationMapper = mapperFactory.createMapper(Notification.class, NotificationDTO.class);
        this.enrollmentMapper = mapperFactory.createMapper(Enrollment.class, EnrollmentDTO.class);
        this.courseMapper = mapperFactory.createMapper(Course.class, CourseDTO.class);
        this.submissionMapper = mapperFactory.createMapper(Submission.class, SubmissionDTO.class);
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.notificationRepository = notificationRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.passwordEncryptionService = passwordEncryptionService;
        this.userValidator = userValidator;
    }

    @Override
    @Transactional
    public UserDTO createUser(UserDTO userDTO) {
        validateUser(userDTO);
        checkIfUserExists(userDTO.getIdentificationNumber());

        User user = userMapper.convertToEntity(userDTO);
        user.setPassword(passwordEncryptionService.encryptPassword(userDTO.getPassword()));

        return executeWithLogging(() -> userMapper.convertToDTO(userRepository.save(user)),
                "Error creating user");
    }

    //GetsBY()
    @Override
    @Transactional(readOnly = true)
    public Optional<UserDTO> getUserById(Long id) {
        return executeWithLogging(() -> {
            User user = getEntityById(id, userRepository, "User");
            return Optional.of(userMapper.convertToDTO(user));
        }, "Error fetching user by ID");
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserDTO> getUserByIdentificationNumber(String identificationNumber) {
        return executeWithLogging(() -> {
            User user = userRepository.findByIdentificationNumber(identificationNumber);
            if (user == null) {
                return Optional.empty();
            }
            return Optional.of(userMapper.convertToDTO(user));
        }, "Error fetching user by identification number");
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserDTO> getAllUsers(Pageable pageable) {
        return executeWithLogging(() -> {
            Page<User> userPage = userRepository.findAll(pageable);
            return userPage.map(userMapper::convertToDTO);
        }, "Error fetching all users");
    }

    @Override
    @Transactional
    public Optional<UserDTO> updateUser(Long id, @Valid UserDTO userDTO) {
        validateUser(userDTO);

        Optional<User> optionalUser = userRepository.findById(id);
        User existingUser = optionalUser.orElseThrow(() -> new ResourceNotFoundException("User", id));
        updateUserFields(existingUser, userDTO);
        updateUserRelations(existingUser, userDTO);

        return executeWithLogging(() -> Optional.of(userMapper.convertToDTO(userRepository.save(existingUser))),
                "Error updating user");
    }

    @Override
    @Transactional
    public boolean deleteUser(Long id, Boolean isPermanentDelete) {
        User user = getEntityById(id, userRepository, "User");

        if (user == null) {
            return false;
        } else {
            if (Boolean.TRUE.equals(isPermanentDelete)) {
                permanentDeleteUser(user);
            } else {
                suspendUser(user);
            }
        }
        return true;
    }

    //user elements
    @Override
    @Transactional(readOnly = true)
    public List<CourseDTO> getCoursesTeachingByUserId(Long userId) {
        User user = getEntityById(userId, userRepository, "User");
        return executeWithLogging(() -> courseMapper.convertToDTOList(user.getCoursesTeaching()),
                "Error fetching courses teaching by user ID");
    }

    @Override
    @Transactional
    public void assignCourseToTeacher(Long userId, Long courseId) {
        User user = getEntityById(userId, userRepository, "User");
        Course course = getEntityById(courseId, courseRepository, "Course");

        if (course.getTeacher() != null && !course.getTeacher().equals(user)) {
            throw new InvalidDataException("Course is already assigned to another teacher with ID: "
                    + course.getTeacher().getId());
        }

        if (!user.getCoursesTeaching().contains(course)) {
            user.getCoursesTeaching().add(course);
            course.setTeacher(user);
            userRepository.save(user);
        }
    }

    @Override
    @Transactional
    public void removeCourseFromTeacher(Long userId, Long courseId) {
        User user = getEntityById(userId, userRepository, "User");
        Course course = getEntityById(courseId, courseRepository, "Course");

        if (!user.getCoursesTeaching().contains(course)) {
            throw new InvalidDataException("The course is not assigned to this teacher.");
        }

        user.getCoursesTeaching().remove(course);
        course.setTeacher(null);

        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentDTO> getEnrollmentsByUserId(Long userId) {
        User user = getEntityById(userId, userRepository, "User");
        return executeWithLogging(() -> enrollmentMapper.convertToDTOList(user.getUserEnrollments()),
                "Error fetching enrollments by user ID");
    }

    @Override
    @Transactional
    public void enrollUserToCourse(Long userId, Long courseId) {
        User user = getEntityById(userId, userRepository, "User");
        Course course = getEntityById(courseId, courseRepository, "Course");

        boolean alreadyEnrolled = user.getUserEnrollments().stream()
                .anyMatch(enrollment -> enrollment.getCourse().equals(course));

        if (alreadyEnrolled) {
            throw new InvalidDataException("User is already enrolled in this course.");
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(user);
        enrollment.setCourse(course);
        enrollment.setState(EnrollmentState.ENROLLED);

        executeWithLogging(() -> {
            enrollmentRepository.save(enrollment);
            return null;
        }, "Error enrolling user to course");
    }

    @Override
    @Transactional
    public void unrollUserFromCourse(Long userId, Long courseId) {
        User user = getEntityById(userId, userRepository, "User");
        Course course = getEntityById(courseId, courseRepository, "Course");

        Enrollment enrollment = enrollmentRepository.findByStudentAndCourse(user, course)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found", user.getId()));

        executeWithLogging(() -> {
            enrollmentRepository.delete(enrollment);
            return null;
        }, "Error unrolling user from course");
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationDTO> getUserNotifications(Long userId) {
        return executeWithLogging(() -> notificationMapper.convertToDTOList(notificationRepository.findByUserId(userId)),
                "Error fetching user notifications");
    }

    @Override
    @Transactional
    public void addNotificationToUser(Long userId, NotificationDTO notificationDTO) {
        User user = getEntityById(userId, userRepository, "User");

        Notification notification = notificationMapper.convertToEntity(notificationDTO);

        notification.setUser(user);
        user.getNotifications().add(notification);

        executeWithLogging(() -> {
            notificationRepository.save(notification);
            return null;
        }, "Error adding notification to user");
    }

    @Override
    @Transactional
    public void removeNotificationFromUser(Long userId, Long notificationId) {
        User user = getEntityById(userId, userRepository, "User");
        Notification notification = getEntityById(notificationId, notificationRepository, "Notification");

        if (user.getNotifications().contains(notification)) {
            user.getNotifications().remove(notification);

            executeWithLogging(() -> {
                notificationRepository.delete(notification);
                return null;
            }, "Error removing notification from user");
        }
    }

    // --------------- MÉTODOS AUXILIARES -----------------

    private void validateUser(UserDTO userDTO) {
        userValidator.validate(userDTO);
        logger.info("User validated: {}", userDTO);
    }

    private void checkIfUserExists(String identificationNumber) {
        if (userRepository.existsByIdentificationNumber(identificationNumber)) {
            throw new ResourceAlreadyExistsException("User",
                    "Identification number " + identificationNumber + " is already in use.");
        }
    }

    //TODO que es ?
    private <T> T getEntityById(Long id, JpaRepository<T, Long> repository, String entityName) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(entityName, id));
    }

    private <T> void updateFieldIfChanged(BiConsumer<User, T> setter, Optional<T> newValueOpt, T existingValue, User user) {
        newValueOpt.ifPresent(newValue -> {
            if (!existingValue.equals(newValue)) {
                setter.accept(user, newValue);
            }
        });
    }

    // todo /\
    private void updateUserFields(User existingUser, UserDTO userDTO) {
        updateFieldIfChanged(User::setName, Optional.ofNullable(userDTO.getName()), existingUser.getName(), existingUser);
        updateFieldIfChanged(User::setEmail, Optional.ofNullable(userDTO.getEmail()), existingUser.getEmail(), existingUser);
        updateFieldIfChanged(User::setIdentificationNumber, Optional.ofNullable(userDTO.getIdentificationNumber()), existingUser.getIdentificationNumber(), existingUser);

        Optional<String> newPassword = Optional.ofNullable(userDTO.getPassword());
        newPassword.ifPresent(password -> {
            if (!password.isEmpty()) {
                existingUser.setPassword(passwordEncryptionService.encryptPassword(password));
            }
        });

        existingUser.setRole(userDTO.getRole());
        existingUser.setState(userDTO.getState());
        existingUser.setLastUpdate(LocalDateTime.now());
    }

    private void updateUserRelations(User existingUser, UserDTO userDTO) {
        updateUserRelation(existingUser.getUserEnrollments(), userDTO.getUserEnrollments(), enrollmentMapper);
        updateUserRelation(existingUser.getCoursesTeaching(), userDTO.getCoursesTeaching(), courseMapper);
        updateUserRelation(existingUser.getNotifications(), userDTO.getNotifications(), notificationMapper);
        updateUserRelation(existingUser.getSubmissions(), userDTO.getSubmissions(), submissionMapper);
    }

    private <T, D> void updateUserRelation(List<T> existingList, List<D> dtoList, GenericMapper<T, D> mapper) {
        List<T> newList = mapper.convertToEntityList(dtoList);
        logger.info("Updating relations for {}: existing {}, new {}", existingList.getClass().getSimpleName(), existingList.size(), newList.size());
        existingList.retainAll(newList);
        newList.removeAll(existingList);
        existingList.addAll(newList);
        logger.info("Updated relations: now {}", existingList.size());
    }

    private <T> T executeWithLogging(Supplier<T> action, String errorMessage) {
        try {
            return action.get();
        } catch (ResourceNotFoundException e) {
            logger.warn("Resource not found: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("{}: {}", errorMessage, e.getMessage());
            throw new ServiceException(errorMessage, e);
        }
    }

    private void suspendUser(User user) {
        user.setState(UserState.SUSPENDED);
        executeWithLogging(() -> userRepository.save(user), "Error suspending user");
    }

    private void permanentDeleteUser(User user) {
        executeWithLogging(() -> {
            userRepository.delete(user);
            return null;
        }, "Error permanently deleting user");
    }
}
