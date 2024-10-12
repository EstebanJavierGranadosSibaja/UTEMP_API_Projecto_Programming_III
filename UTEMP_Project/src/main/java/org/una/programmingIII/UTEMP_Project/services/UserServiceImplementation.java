package org.una.programmingIII.UTEMP_Project.services;

import jakarta.validation.Valid;
import org.hibernate.service.spi.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.una.programmingIII.UTEMP_Project.dtos.*;
import org.una.programmingIII.UTEMP_Project.exceptions.*;
import org.una.programmingIII.UTEMP_Project.models.*;
import org.una.programmingIII.UTEMP_Project.repositories.*;
import org.una.programmingIII.UTEMP_Project.transformers.mappers.GenericMapper;
import org.una.programmingIII.UTEMP_Project.transformers.mappers.GenericMapperFactory;
import org.una.programmingIII.UTEMP_Project.facades.transformersFacades.UserPermissionConverterFacade;
import org.una.programmingIII.UTEMP_Project.validators.UserValidator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
    private AssignmentRepository assignmentRepository;

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
    public List<UserDTO> getAllUsers() {
        try {
            return userMapper.convertToDTOList(userRepository.findAll());
        } catch (Exception e) {
            logger.error("Error fetching all users: {}", e.getMessage());
            throw new ServiceException("An error occurred while retrieving all users.", e);
        }
    }

    @Override
    public Optional<UserDTO> getUserById(Long id) {
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("User", id));

            return Optional.of(userMapper.convertToDTO(user));
        } catch (Exception e) {
            logger.error("Error fetching user by ID: {}", e.getMessage());
            throw new ServiceException("An error occurred while retrieving the user.", e);
        }
    }

    @Override
    @Transactional
    public UserDTO createUser(UserDTO userDTO) {
        try {
            validateAndLogUser(userDTO);

            if (userRepository.existsByIdentificationNumber(userDTO.getIdentificationNumber())) {
                throw new ResourceAlreadyExistsException("User", "Identification number " + userDTO.getIdentificationNumber() + " is already in use.");
            }

            User user = userMapper.convertToEntity(userDTO);
            user = userRepository.save(user);
            logger.info("User created with ID: {}", user.getId());
            return userMapper.convertToDTO(user);
        } catch (Exception e) {
            logger.error("Error creating user: {}", e.getMessage());
            throw new ServiceException("An error occurred while creating the user.", e);
        }
    }

    @Override
    @Transactional
    public Optional<UserDTO> updateUser(Long id, @Valid UserDTO userDTO) {
        try {
            User existingUser = userRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("User", id));

            validateAndLogUser(userDTO);

            updateUserFields(existingUser, userDTO);
            updateUserRelations(existingUser, userDTO);

            userRepository.save(existingUser);
            logger.info("User updated with ID: {}", id);

            return Optional.of(userMapper.convertToDTO(existingUser));
        } catch (Exception e) {
            logger.error("Error updating user: {}", e.getMessage());
            throw new ServiceException("An error occurred while updating the user.", e);
        }
    }

    @Override
    public void deleteUser(Long id, Boolean isPermanentDelete) {
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("User", id));

            if (!isPermanentDelete) {
                suspendUser(user);
                return;
            }
            if (userHasAssociatedItems(user)) {
                permanentDeleteUser(user);
                return;
            }
            logger.error("You cannot permanently delete users with associated items. User ID: {}", id);
        } catch (Exception e) {
            logger.error("Error deleting user: {}", e.getMessage());
            throw new ServiceException("An error occurred while deleting the user.", e);
        }
    }

    @Override
    public void enrollUserToCourse(Long userId, Long courseId) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", userId));
            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new ResourceNotFoundException("Course", courseId));

            Enrollment enrollment = new Enrollment();
            enrollment.setStudent(user);
            enrollment.setCourse(course);
            enrollment.setState(EnrollmentState.ENROLLED);

            user.getUserEnrollments().add(enrollment);
            course.getEnrollments().add(enrollment);

            enrollmentRepository.save(enrollment);
            userRepository.save(user);
            courseRepository.save(course);

            logger.info("User with ID: {} enrolled to course with ID: {}", userId, courseId);
        } catch (Exception e) {
            logger.error("Error enrolling user to course: {}", e.getMessage());
            throw new ServiceException("An error occurred while enrolling the user to the course.", e);
        }
    }

    @Override
    public List<NotificationDTO> getUserNotifications(Long userId) {
        try {
            return notificationMapper.convertToDTOList(notificationRepository.findByUserId(userId));
        } catch (Exception e) {
            logger.error("Error fetching user notifications: {}", e.getMessage());
            throw new ServiceException("An error occurred while retrieving the user notifications.", e);
        }
    }

    @Override
    public void addNotificationToUser(Long userId, NotificationDTO notificationDTO) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", userId));

            Notification notification = notificationMapper.convertToEntity(notificationDTO);
            notification.setUser(user);
            user.getNotifications().add(notification);

            notificationRepository.save(notification);
            logger.info("Notification added to user with ID: {}", userId);
        } catch (Exception e) {
            logger.error("Error adding notification to user: {}", e.getMessage());
            throw new ServiceException("An error occurred while adding the notification to the user.", e);
        }
    }

    private void validateAndLogUser(UserDTO userDTO) {
        try {
            userValidator.validate(userDTO);
            logger.info("User validated: {}", userDTO);
        } catch (Exception e) {
            logger.error("Error validating user: {}", e.getMessage());
            throw new ServiceException("An error occurred while validating the user.", e);
        }
    }

    private void updateUserFields(User existingUser, UserDTO userDTO) {
        try {
            existingUser.setName(userDTO.getName());
            existingUser.setEmail(userDTO.getEmail());
            existingUser.setIdentificationNumber(userDTO.getIdentificationNumber());
            existingUser.setPassword(userDTO.getPassword());
            existingUser.setRole(userDTO.getRole());
            existingUser.setState(userDTO.getState());
            existingUser.setLastUpdate(LocalDateTime.now());
        } catch (Exception e) {
            logger.error("Error updating user fields: {}", e.getMessage());
            throw new ServiceException("An error occurred while updating user fields.", e);
        }
    }

    private void updateUserRelations(User existingUser, UserDTO userDTO) {
        try {
            if (userDTO == null) return;

            updateEnrollmentRelations(existingUser, userDTO.getUserEnrollments());
            updateCourseRelations(existingUser, userDTO.getCoursesTeaching());
            updateNotificationRelations(existingUser, userDTO.getNotifications());
            updateSubmissionRelations(existingUser, userDTO.getSubmissions());
        } catch (Exception e) {
            logger.error("Error updating user relations: {}", e.getMessage());
            throw new ServiceException("An error occurred while updating user relations.", e);
        }
    }

    private void updateEnrollmentRelations(User existingUser, List<EnrollmentDTO> enrollments) {
        try {
            if (enrollments != null) {
                Set<Long> updatedEnrollmentIds = enrollments.stream()
                        .map(EnrollmentDTO::getId)
                        .collect(Collectors.toSet());

                existingUser.getUserEnrollments().removeIf(enrollment -> !updatedEnrollmentIds.contains(enrollment.getId()));
                existingUser.getUserEnrollments().addAll(enrollmentMapper.convertToEntityList(enrollments));
            }
        } catch (Exception e) {
            logger.error("Error updating enrollment relations: {}", e.getMessage());
            throw new ServiceException("An error occurred while updating enrollment relations.", e);
        }
    }

    private void updateCourseRelations(User existingUser, List<CourseDTO> courses) {
        try {
            if (courses != null) {
                Set<Long> updatedCourseIds = courses.stream()
                        .map(CourseDTO::getId)
                        .collect(Collectors.toSet());

                existingUser.getCoursesTeaching().removeIf(course -> !updatedCourseIds.contains(course.getId()));
                existingUser.getCoursesTeaching().addAll(courseMapper.convertToEntityList(courses));
            }
        } catch (Exception e) {
            logger.error("Error updating course relations: {}", e.getMessage());
            throw new ServiceException("An error occurred while updating course relations.", e);
        }
    }

    private void updateNotificationRelations(User existingUser, List<NotificationDTO> notifications) {
        try {
            if (notifications != null) {
                Set<Long> updatedNotificationIds = notifications.stream()
                        .map(NotificationDTO::getId)
                        .collect(Collectors.toSet());

                existingUser.getNotifications().removeIf(notification -> !updatedNotificationIds.contains(notification.getId()));
                existingUser.getNotifications().addAll(notificationMapper.convertToEntityList(notifications));
            }
        } catch (Exception e) {
            logger.error("Error updating notification relations: {}", e.getMessage());
            throw new ServiceException("An error occurred while updating notification relations.", e);
        }
    }

    private void updateSubmissionRelations(User existingUser, List<SubmissionDTO> submissions) {
        try {
            if (submissions != null) {
                Set<Long> updatedSubmissionIds = submissions.stream()
                        .map(SubmissionDTO::getId)
                        .collect(Collectors.toSet());

                existingUser.getSubmissions().removeIf(submission -> !updatedSubmissionIds.contains(submission.getId()));
                existingUser.getSubmissions().addAll(submissionMapper.convertToEntityList(submissions));
            }
        } catch (Exception e) {
            logger.error("Error updating submission relations: {}", e.getMessage());
            throw new ServiceException("An error occurred while updating submission relations.", e);
        }
    }

    private boolean userHasAssociatedItems(User user) {
        return user.getCoursesTeaching().isEmpty()
                && user.getUserEnrollments().isEmpty()
                && user.getNotifications().isEmpty()
                && user.getSubmissions().isEmpty();
    }

    private void suspendUser(User user) {
        try {
            user.setState(UserState.SUSPENDED);
            userRepository.save(user);
            logger.info("User with ID: {} has been suspended.", user.getId());
        } catch (Exception e) {
            logger.error("Error suspending user: {}", e.getMessage());
            throw new ServiceException("An error occurred while suspending the user.", e);
        }
    }

    private void permanentDeleteUser(User user) {
        try {
            userRepository.delete(user);
            logger.info("User with ID: {} permanently deleted.", user.getId());
        } catch (Exception e) {
            logger.error("Error permanently deleting user: {}", e.getMessage());
            throw new ServiceException("An error occurred while permanently deleting the user.", e);
        }
    }
}