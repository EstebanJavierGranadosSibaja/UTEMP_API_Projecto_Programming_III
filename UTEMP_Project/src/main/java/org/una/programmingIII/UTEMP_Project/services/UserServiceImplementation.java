package org.una.programmingIII.UTEMP_Project.services;

import jakarta.validation.Valid;
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
    private final GenericMapper<FileMetadatum, FileMetadatumDTO> fileMetadataMapper;
    private final GenericMapper<Enrollment, EnrollmentDTO> enrollmentMapper;
    private final GenericMapper<Course, CourseDTO> courseMapper;
    private final GenericMapper<Submission, SubmissionDTO> submissionMapper;

    @Autowired
    public UserServiceImplementation(GenericMapperFactory mapperFactory) {
        this.mapperFactory = mapperFactory;
        this.userMapper = mapperFactory.createMapper(User.class, UserDTO.class);
        this.notificationMapper = mapperFactory.createMapper(Notification.class, NotificationDTO.class);
        this.fileMetadataMapper = mapperFactory.createMapper(FileMetadatum.class, FileMetadatumDTO.class);
        this.enrollmentMapper = mapperFactory.createMapper(Enrollment.class, EnrollmentDTO.class);
        this.courseMapper = mapperFactory.createMapper(Course.class, CourseDTO.class);
        this.submissionMapper = mapperFactory.createMapper(Submission.class, SubmissionDTO.class);
    }

    @Override
    public List<UserDTO> getAllUsers() {
        return userMapper.convertToDTOList(userRepository.findAll());
    }

    @Override
    public Optional<UserDTO> getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        return Optional.of(userMapper.convertToDTO(user));
    }

    @Override
    @Transactional
    public UserDTO createUser(UserDTO userDTO) {
        validateAndLogUser(userDTO);

        if (userRepository.existsByIdentificationNumber(userDTO.getIdentificationNumber())) {
            throw new ResourceAlreadyExistsException("User", "Identification number " + userDTO.getIdentificationNumber() + " is already in use.");
        }

        User user = userMapper.convertToEntity(userDTO);
        user = userRepository.save(user);
        logger.info("User created with ID: {}", user.getId());
        return userMapper.convertToDTO(user);
    }

    @Override
    @Transactional
    public Optional<UserDTO> updateUser(Long id, @Valid UserDTO userDTO) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        validateAndLogUser(userDTO);

        updateUserFields(existingUser, userDTO);
        updateUserRelations(existingUser, userDTO);

        userRepository.save(existingUser);
        logger.info("User updated with ID: {}", id);

        return Optional.of(userMapper.convertToDTO(existingUser));
    }

    @Override
    public void deleteUser(Long id, Boolean isPermanentDelete) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User", id));
        if (!isPermanentDelete) {
            suspendUser(user);
            return;
        }
        if (userHasAssociatedItems(user)) {
            permanentDeleteUser(user);
            return;
        }
        logger.error("You cannot permanently delete users with associated items. User ID: {}", id);
    }

    @Override
    public void enrollUserToCourse(Long userId, Long courseId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", userId));
        Course course = courseRepository.findById(courseId).orElseThrow(() -> new ResourceNotFoundException("Course", courseId));

        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(user);
        enrollment.setCourse(course);
        enrollment.setState(EnrollmentState.ENROLLED);

        user.getEnrollments().add(enrollment);
        course.getEnrollments().add(enrollment);

        enrollmentRepository.save(enrollment);
        userRepository.save(user);
        courseRepository.save(course);

        logger.info("User with ID: {} enrolled to course with ID: {}", userId, courseId);
    }

    @Override
    public List<NotificationDTO> getUserNotifications(Long userId) {
        return notificationMapper.convertToDTOList(notificationRepository.findByUserId(userId));
    }

    @Override
    public void addNotificationToUser(Long userId, NotificationDTO notificationDTO) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", userId));

        Notification notification = notificationMapper.convertToEntity(notificationDTO);
        notification.setUser(user);
        user.getNotifications().add(notification);

        notificationRepository.save(notification);
        logger.info("Notification added to user with ID: {}", userId);
    }

    private void validateAndLogUser(UserDTO userDTO) {
        userValidator.validate(userDTO);
        logger.info("User validated: {}", userDTO);
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
        if (userDTO == null) return;

        updateFileMetadataRelations(existingUser, userDTO.getFileMetadata());
        updateEnrollmentRelations(existingUser, userDTO.getEnrollments());
        updateCourseRelations(existingUser, userDTO.getCourses());
        updateNotificationRelations(existingUser, userDTO.getNotifications());
        updateSubmissionRelations(existingUser, userDTO.getSubmissions());
    }

    private void updateFileMetadataRelations(User existingUser, List<FileMetadatumDTO> newFileMetadata) {
        Set<Long> existingFileMetadataIds = existingUser.getFileMetadata() != null ?
                existingUser.getFileMetadata().stream()
                        .map(Identifiable::getId)
                        .collect(Collectors.toSet()) :
                Set.of();

        existingUser.setFileMetadata(addNewEntitiesIfNotExists(existingUser.getFileMetadata(), newFileMetadata, fileMetadataMapper, existingFileMetadataIds));
    }

    private void updateEnrollmentRelations(User existingUser, List<EnrollmentDTO> newEnrollments) {
        Set<Long> existingEnrollmentIds = existingUser.getEnrollments() != null ?
                existingUser.getEnrollments().stream()
                        .map(Identifiable::getId)
                        .collect(Collectors.toSet()) :
                Set.of();

        existingUser.setEnrollments(addNewEntitiesIfNotExists(existingUser.getEnrollments(), newEnrollments, enrollmentMapper, existingEnrollmentIds));
    }

    private void updateCourseRelations(User existingUser, List<CourseDTO> newCourses) {
        Set<Long> existingCourseIds = existingUser.getCourses() != null ?
                existingUser.getCourses().stream()
                        .map(Identifiable::getId)
                        .collect(Collectors.toSet()) :
                Set.of();

        existingUser.setCourses(addNewEntitiesIfNotExists(existingUser.getCourses(), newCourses, courseMapper, existingCourseIds));
    }

    private void updateNotificationRelations(User existingUser, List<NotificationDTO> newNotifications) {
        Set<Long> existingNotificationIds = existingUser.getNotifications() != null ?
                existingUser.getNotifications().stream()
                        .map(Identifiable::getId)
                        .collect(Collectors.toSet()) :
                Set.of();

        existingUser.setNotifications(addNewEntitiesIfNotExists(existingUser.getNotifications(), newNotifications, notificationMapper, existingNotificationIds));
    }

    private void updateSubmissionRelations(User existingUser, List<SubmissionDTO> newSubmissions) {
        Set<Long> existingSubmissionIds = existingUser.getSubmissions() != null ?
                existingUser.getSubmissions().stream()
                        .map(Identifiable::getId)
                        .collect(Collectors.toSet()) :
                Set.of();

        existingUser.setSubmissions(addNewEntitiesIfNotExists(existingUser.getSubmissions(), newSubmissions, submissionMapper, existingSubmissionIds));
    }

    private <E extends Identifiable, D> List<E> addNewEntitiesIfNotExists(List<E> existingList, List<D> newList, GenericMapper<E, D> mapper, Set<Long> existingIds) {
        if (newList == null || newList.isEmpty()) {
            return existingList == null ? new ArrayList<>() : existingList;
        }

        List<E> newEntities = mapper.convertToEntityList(newList);

        return newEntities.stream()
                .filter(newEntity -> !existingIds.contains(newEntity.getId()))
                .collect(Collectors.toList());
    }

    private boolean userHasAssociatedItems(User user) {
        return !user.getCourses().isEmpty() ||
                !user.getEnrollments().isEmpty() ||
                !user.getNotifications().isEmpty() ||
                !user.getSubmissions().isEmpty();
    }

    private void permanentDeleteUser(User user) {
        userRepository.delete(user);
        logger.info("User with ID: {} permanently deleted.", user.getId());
    }

    private void suspendUser(User user) {
        user.setState(UserState.SUSPENDED);
        userRepository.save(user);
        logger.info("User with ID: {} suspended.", user.getId());
    }
}