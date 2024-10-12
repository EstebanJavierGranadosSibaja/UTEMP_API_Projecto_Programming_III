package org.una.programmingIII.UTEMP_Project.services;

import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import org.hibernate.service.spi.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.una.programmingIII.UTEMP_Project.dtos.*;
import org.una.programmingIII.UTEMP_Project.exceptions.*;
import org.una.programmingIII.UTEMP_Project.models.*;
import org.una.programmingIII.UTEMP_Project.repositories.*;
import org.una.programmingIII.UTEMP_Project.transformers.mappers.GenericMapper;
import org.una.programmingIII.UTEMP_Project.transformers.mappers.GenericMapperFactory;
import org.una.programmingIII.UTEMP_Project.validators.UserValidator;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserServiceImplementation implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImplementation.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private UserValidator userValidator;

    @Autowired
    private final GenericMapperFactory mapperFactory;

    private final GenericMapper<User, UserDTO> userMapper;
    private final GenericMapper<Notification, NotificationDTO> notificationMapper;
    private final GenericMapper<Enrollment, EnrollmentDTO> enrollmentMapper;
    private final GenericMapper<Course, CourseDTO> courseMapper;
    private final GenericMapper<Submission, SubmissionDTO> submissionMapper;

    @Autowired
    public UserServiceImplementation(GenericMapperFactory mapperFactory) {
        this.mapperFactory = mapperFactory;
        this.userMapper = mapperFactory.createMapper(User.class, UserDTO.class);
        this.notificationMapper = mapperFactory.createMapper(Notification.class, NotificationDTO.class);
        this.enrollmentMapper = mapperFactory.createMapper(Enrollment.class, EnrollmentDTO.class);
        this.courseMapper = mapperFactory.createMapper(Course.class, CourseDTO.class);
        this.submissionMapper = mapperFactory.createMapper(Submission.class, SubmissionDTO.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        return executeWithLogging(() -> userMapper.convertToDTOList(userRepository.findAll()),
                "Error fetching all users");
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserDTO> getUserById(Long id) {
        return executeWithLogging(() -> {
            User user = getEntityById(id, userRepository, "User");
            return Optional.of(userMapper.convertToDTO(user));
        }, "Error fetching user by ID");
    }

    @Override
    @Transactional
    public UserDTO createUser(UserDTO userDTO) {
        validateUser(userDTO);
        checkIfUserExists(userDTO.getIdentificationNumber());

        User user = userMapper.convertToEntity(userDTO);
        return executeWithLogging(() -> userMapper.convertToDTO(userRepository.save(user)),
                "Error creating user");
    }

    @Override
    @Transactional
    public Optional<UserDTO> updateUser(Long id, @Valid UserDTO userDTO) {
        validateUser(userDTO);

        User existingUser = getEntityById(id, userRepository, "User");
        updateUserFields(existingUser, userDTO);
        updateUserRelations(existingUser, userDTO);

        return executeWithLogging(() -> Optional.of(userMapper.convertToDTO(userRepository.save(existingUser))),
                "Error updating user");
    }

    @Override
    @Transactional
    public void deleteUser(Long id, Boolean isPermanentDelete) {
        User user = getEntityById(id, userRepository, "User");

        if (Boolean.TRUE.equals(isPermanentDelete)) {
            permanentDeleteUser(user);
        } else {
            suspendUser(user);
        }
    }

    @Override
    @Transactional
    public void enrollUserToCourse(Long userId, Long courseId) {
        User user = getEntityById(userId, userRepository, "User");
        Course course = getEntityById(courseId, courseRepository, "Course");

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

    private <T> T getEntityById(Long id, JpaRepository<T, Long> repository, String entityName) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(entityName, id));
    }

    private void updateUserFields(User existingUser, UserDTO userDTO) {
        existingUser.setName(userDTO.getName());
        existingUser.setEmail(userDTO.getEmail());
        existingUser.setIdentificationNumber(userDTO.getIdentificationNumber());
        existingUser.setPassword(userDTO.getPassword());
        existingUser.setRole(userDTO.getRole());
        existingUser.setState(userDTO.getState());
        existingUser.setLastUpdate(LocalDateTime.now());
    }

    private void updateUserRelations(User existingUser, UserDTO userDTO) {
        updateRelations(existingUser.getUserEnrollments(), enrollmentMapper.convertToEntityList(userDTO.getUserEnrollments()));
        updateRelations(existingUser.getCoursesTeaching(), courseMapper.convertToEntityList(userDTO.getCoursesTeaching()));
        updateRelations(existingUser.getNotifications(), notificationMapper.convertToEntityList(userDTO.getNotifications()));
        updateRelations(existingUser.getSubmissions(), submissionMapper.convertToEntityList(userDTO.getSubmissions()));
    }

    private <T> void updateRelations(List<T> existingList, List<T> newList) {
        existingList.retainAll(newList);
        newList.removeAll(existingList);
        existingList.addAll(newList);
    }

    private <T> T executeWithLogging(Supplier<T> action, String errorMessage) {
        try {
            return action.get();
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
