package org.una.programmingIII.UTEMP_Project.services.notification;

import jakarta.validation.Valid;
import org.hibernate.service.spi.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.una.programmingIII.UTEMP_Project.dtos.NotificationDTO;
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

    @Autowired
    public NotificationServiceImplementation(GenericMapperFactory mapperFactory) {
        this.notificationMapper = mapperFactory.createMapper(Notification.class, NotificationDTO.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationDTO> getAllNotifications() {
        return executeWithLogging(() -> notificationMapper.convertToDTOList(notificationRepository.findAll()),
                "Error fetching all notifications");
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
    public List<NotificationDTO> getNotificationsByUserId(Long userId) {
        User user = getEntityById(userId, userRepository, "User");
        return executeWithLogging(() -> notificationMapper.convertToDTOList(notificationRepository.findByUser(user)),
                "Error fetching notifications by user ID");
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId) {
        Notification notification = getEntityById(notificationId, notificationRepository, "Notification");
        notification.setStatus(NotificationStatus.READ);
        notification.setLastUpdate(LocalDateTime.now());
        executeWithLogging(() -> notificationRepository.save(notification), "Error marking notification as read");
    }

    // --------------- MÉTODOS AUXILIARES -----------------

    private <T> T getEntityById(Long id, JpaRepository<T, Long> repository, String entityName) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(entityName, id));
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
