package org.una.programmingIII.UTEMP_Project.services.notification;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.una.programmingIII.UTEMP_Project.dtos.NotificationDTO;

import java.util.Optional;

public interface NotificationService {

    Page<NotificationDTO> getAllNotifications(Pageable pageable);

    Optional<NotificationDTO> getNotificationById(Long id);

    NotificationDTO createNotification(@Valid NotificationDTO notificationDTO);

    Optional<NotificationDTO> updateNotification(Long id, @Valid NotificationDTO notificationDTO);

    void deleteNotification(Long id);

    Page<NotificationDTO> getNotificationsByUserId(Long userId, Pageable pageable);

    void addNotificationToUser(Long userId, NotificationDTO notificationDTO);

    void removeNotificationFromUser(Long userId, Long notificationId);

    void markAsRead(Long notificationId);

    void sendNotificationToUser(Long userId, String message);
}
