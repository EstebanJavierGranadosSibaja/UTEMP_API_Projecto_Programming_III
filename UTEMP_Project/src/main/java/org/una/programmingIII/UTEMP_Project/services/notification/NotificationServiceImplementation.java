package org.una.programmingIII.UTEMP_Project.services.notification;

import jakarta.validation.Valid;
import org.hibernate.service.spi.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.una.programmingIII.UTEMP_Project.dtos.NotificationDTO;
import org.una.programmingIII.UTEMP_Project.dtos.UserDTO;
import org.una.programmingIII.UTEMP_Project.exceptions.InvalidDataException;
import org.una.programmingIII.UTEMP_Project.exceptions.ResourceNotFoundException;
import org.una.programmingIII.UTEMP_Project.models.Notification;
import org.una.programmingIII.UTEMP_Project.models.NotificationStatus;
import org.una.programmingIII.UTEMP_Project.models.User;
import org.una.programmingIII.UTEMP_Project.repositories.NotificationRepository;
import org.una.programmingIII.UTEMP_Project.repositories.UserRepository;
import org.una.programmingIII.UTEMP_Project.transformers.mappers.GenericMapper;
import org.una.programmingIII.UTEMP_Project.transformers.mappers.GenericMapperFactory;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.function.Supplier;

@Service
@Transactional
public class NotificationServiceImplementation implements NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceImplementation.class);

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    private final GenericMapper<Notification, NotificationDTO> notificationMapper;
    private final GenericMapper<User, UserDTO> userMapper;

    @Autowired
    public NotificationServiceImplementation(
            NotificationRepository notificationRepository,
            UserRepository userRepository,
            GenericMapperFactory mapperFactory) {

        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.notificationMapper = mapperFactory.createMapper(Notification.class, NotificationDTO.class);
        this.userMapper = mapperFactory.createMapper(User.class, UserDTO.class);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationDTO> getAllNotifications(Pageable pageable) {
        try {
            return executeWithLogging(() -> {
                Page<Notification> notificationPage = notificationRepository.findAll(pageable);
                return notificationPage.map(notificationMapper::convertToDTO);
            }, "Error fetching all notifications");
        } catch (DataAccessException e) {
            logger.error("Database access error occurred while fetching all notifications: {}", e.getMessage());
            throw new InvalidDataException("Error fetching notifications from the database");
        } catch (Exception e) {
            logger.error("Unexpected error occurred while fetching all notifications: {}", e.getMessage());
            throw new InvalidDataException("Error fetching all notifications");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<NotificationDTO> getNotificationById(Long id) {
        try {
            return executeWithLogging(() -> {
                Notification notification = getEntityById(id, notificationRepository, "Notification");
                return Optional.of(notificationMapper.convertToDTO(notification));
            }, "Error fetching notification by ID");
        } catch (ResourceNotFoundException e) {
            logger.warn("Failed to fetch notification: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error fetching notification by ID: {}", e.getMessage());
            throw new ServiceException("Error fetching notification", e);
        }
    }

    @Override
    @Transactional
    public NotificationDTO createNotification(@Valid NotificationDTO notificationDTO) {
        try {
            Notification notification = notificationMapper.convertToEntity(notificationDTO);
            notification.setUser(getEntityById(notificationDTO.getUser().getId(), userRepository, "User"));
            notification.setStatus(NotificationStatus.UNREAD); // Estado por defecto

            return executeWithLogging(() -> notificationMapper.convertToDTO(notificationRepository.save(notification)),
                    "Error creating notification");
        } catch (ResourceNotFoundException e) {
            logger.warn("Failed to create notification: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error creating notification: {}", e.getMessage());
            throw new ServiceException("Error creating notification", e);
        }
    }

    @Override
    @Transactional
    public Optional<NotificationDTO> updateNotification(Long id, @Valid NotificationDTO notificationDTO) {
        try {
            Notification existingNotification = notificationRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Notification", id));
            updateNotificationFields(existingNotification, notificationDTO);

            return executeWithLogging(() -> Optional.of(notificationMapper.convertToDTO(notificationRepository.save(existingNotification))),
                    "Error updating notification");
        } catch (ResourceNotFoundException e) {
            logger.warn("Failed to update notification: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error updating notification: {}", e.getMessage());
            throw new ServiceException("Error updating notification", e);
        }
    }

    @Override
    @Transactional
    public void deleteNotification(Long id) {
        try {
            Notification notification = getEntityById(id, notificationRepository, "Notification");
            executeWithLogging(() -> {
                notificationRepository.delete(notification);
                return null;
            }, "Error deleting notification");
        } catch (ResourceNotFoundException e) {
            logger.warn("Failed to delete notification: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error deleting notification: {}", e.getMessage());
            throw new ServiceException("Error deleting notification", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationDTO> getNotificationsByUserId(Long userId, Pageable pageable) {
        try {
            return executeWithLogging(() -> {
                Page<Notification> notificationPage = notificationRepository.findByUserId(userId, pageable);
                return notificationPage.map(notificationMapper::convertToDTO);
            }, "Error fetching notifications by user ID");
        } catch (DataAccessException e) {
            logger.error("Database access error occurred while fetching notifications for user ID {}: {}", userId, e.getMessage());
            throw new InvalidDataException("Error fetching notifications from the database for user ID " + userId);
        } catch (Exception e) {
            logger.error("Unexpected error occurred while fetching notifications for user ID {}: {}", userId, e.getMessage());
            throw new InvalidDataException("Error fetching notifications for user ID " + userId);
        }
    }

    @Override
    @Transactional
    public void addNotificationToUser(Long userId, NotificationDTO notificationDTO) {
        try {
            User user = getEntityById(userId, userRepository, "User");
            Notification notification = notificationMapper.convertToEntity(notificationDTO);
            notification.setUser(user);
            user.getNotifications().add(notification);

            executeWithLogging(() -> {
                notificationRepository.save(notification);
                return null;
            }, "Error adding notification to user");
        } catch (ResourceNotFoundException e) {
            logger.warn("Failed to add notification to user {}: {}", userId, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error adding notification to user {}: {}", userId, e.getMessage());
            throw new ServiceException("Error adding notification to user", e);
        }
    }

    @Override
    @Transactional
    public void removeNotificationFromUser(Long userId, Long notificationId) {
        try {
            User user = getEntityById(userId, userRepository, "User");
            Notification notification = getEntityById(notificationId, notificationRepository, "Notification");
            if (user.getNotifications().remove(notification)) { // Utiliza el retorno de remove
                executeWithLogging(() -> {
                    notificationRepository.delete(notification);
                    return null;
                }, "Error removing notification from user");
            } else {
                logger.warn("Notification {} not found in user's notifications", notificationId);
            }
        } catch (ResourceNotFoundException e) {
            logger.warn("Failed to remove notification from user {}: {}", userId, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error removing notification from user {}: {}", userId, e.getMessage());
            throw new ServiceException("Error removing notification from user", e);
        }
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId) {
        try {
            Notification notification = getEntityById(notificationId, notificationRepository, "Notification");
            notification.setStatus(NotificationStatus.READ);
            notification.setLastUpdate(LocalDateTime.now());
            executeWithLogging(() -> notificationRepository.save(notification),
                    "Error marking notification as read");
        } catch (ResourceNotFoundException e) {
            logger.warn("Failed to mark notification as read: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error marking notification as read: {}", e.getMessage());
            throw new ServiceException("Error marking notification as read", e);
        }
    }

    @Transactional
    @Override
    public void sendNotificationToUser(Long userId, String message) {
        try {
            Optional<User> userMassage = userRepository.findById(userId);
            NotificationDTO notification = new NotificationDTO();
            notification.setUser(userMapper.convertToDTO(userMassage.orElseThrow()));
            notification.setMessage(message);
            notification.setStatus(NotificationStatus.UNREAD);
            addNotificationToUser(userId, notification);
        } catch (Exception e) {
            logger.error("Error sending notification to user {}: {}", userId, e.getMessage());
            throw new ServiceException("Error sending notification", e);
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
