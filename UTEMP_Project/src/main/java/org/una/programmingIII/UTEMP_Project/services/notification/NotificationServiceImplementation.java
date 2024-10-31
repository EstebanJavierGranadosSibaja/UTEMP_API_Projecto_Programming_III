package org.una.programmingIII.UTEMP_Project.services.notification;

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
import org.una.programmingIII.UTEMP_Project.dtos.NotificationDTO;
import org.una.programmingIII.UTEMP_Project.dtos.UserDTO;
import org.una.programmingIII.UTEMP_Project.exceptions.ResourceNotFoundException;
import org.una.programmingIII.UTEMP_Project.models.Notification;
import org.una.programmingIII.UTEMP_Project.models.User;
import org.una.programmingIII.UTEMP_Project.models.NotificationStatus;
import org.una.programmingIII.UTEMP_Project.repositories.NotificationRepository;
import org.una.programmingIII.UTEMP_Project.repositories.UserRepository;
import org.una.programmingIII.UTEMP_Project.transformers.mappers.GenericMapper;
import org.una.programmingIII.UTEMP_Project.transformers.mappers.GenericMapperFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@Service
@Transactional
public class NotificationServiceImplementation implements NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceImplementation.class);

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    private final GenericMapper<Notification, NotificationDTO> notificationMapper;
    private final GenericMapper<User, UserDTO> userMapper;

    @Autowired
    public NotificationServiceImplementation(GenericMapperFactory mapperFactory) {
        this.notificationMapper = mapperFactory.createMapper(Notification.class, NotificationDTO.class);
        this.userMapper = mapperFactory.createMapper(User.class, UserDTO.class);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationDTO> getAllNotifications(Pageable pageable) {
        return executeWithLogging(() -> {
            return notificationRepository.findAll(pageable).map(notificationMapper::convertToDTO);
        }, "Error fetching all notifications");
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<NotificationDTO> getNotificationById(Long id) {
        return executeWithLogging(() -> {
            Notification notification = getEntityById(id, notificationRepository, "Notification");
            return Optional.of(notificationMapper.convertToDTO(notification));
        }, "Error fetching notification by ID");
    }

    @Override
    @Transactional
    public NotificationDTO createNotification(@Valid NotificationDTO notificationDTO) {
        Notification notification = notificationMapper.convertToEntity(notificationDTO);
        notification.setUser(getEntityById(notificationDTO.getUser().getId(), userRepository, "User"));
        notification.setStatus(NotificationStatus.UNREAD); // Default status
        return executeWithLogging(() -> notificationMapper.convertToDTO(notificationRepository.save(notification)),
                "Error creating notification");
    }

    @Override
    @Transactional
    public Optional<NotificationDTO> updateNotification(Long id, @Valid NotificationDTO notificationDTO) {
        Optional<Notification> optionalNotification = notificationRepository.findById(id);
        Notification existingNotification = optionalNotification.orElseThrow(() -> new ResourceNotFoundException("Notification", id));

        updateNotificationFields(existingNotification, notificationDTO);
        return executeWithLogging(() -> Optional.of(notificationMapper.convertToDTO(notificationRepository.save(existingNotification))),
                "Error updating notification");
    }

    @Override
    @Transactional
    public void deleteNotification(Long id) {
        Notification notification = getEntityById(id, notificationRepository, "Notification");
        executeWithLogging(() -> {
            notificationRepository.delete(notification);
            return null;
        }, "Error deleting notification");
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationDTO> getNotificationsByUserId(Long userId, Pageable pageable) {
        User user = getEntityById(userId, userRepository, "User");
        return executeWithLogging(() -> {
            return notificationRepository.findByUser(user, pageable).map(notificationMapper::convertToDTO);
        }, "Error fetching notifications by user ID");
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

    @Override
    @Transactional
    public void markAsRead(Long notificationId) {
        Notification notification = getEntityById(notificationId, notificationRepository, "Notification");
        notification.setStatus(NotificationStatus.READ);
        notification.setLastUpdate(LocalDateTime.now());
        executeWithLogging(() -> notificationRepository.save(notification), "Error marking notification as read");
    }

    @Transactional
    @Override
    public void sendNotificationToUser(User user, String message) {
        NotificationDTO notification = new NotificationDTO();
        notification.setUser(userMapper.convertToDTO(user));
        notification.setMessage(message);
        notification.setStatus(NotificationStatus.UNREAD);
        addNotificationToUser(user.getId(), notification);
    }

    // --------------- MÉTODOS AUXILIARES -----------------

    private <T> T getEntityById(Long id, JpaRepository<T, Long> repository, String entityName) {
        return findEntityById(id, repository)
                .orElseThrow(() -> new ResourceNotFoundException(entityName, id));
    }

    private <T> Optional<T> findEntityById(Long id, JpaRepository<T, Long> repository) {
        return repository.findById(id);
    }

    private void updateNotificationFields(Notification existingNotification, NotificationDTO notificationDTO) {
        existingNotification.setMessage(notificationDTO.getMessage());
        existingNotification.setStatus(notificationDTO.getStatus());
        existingNotification.setLastUpdate(LocalDateTime.now());
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
