package org.una.programmingIII.UTEMP_Project.services.notification;

import jakarta.validation.Valid;
import org.una.programmingIII.UTEMP_Project.dtos.NotificationDTO;

import java.util.List;
import java.util.Optional;

public interface NotificationService {
    List<NotificationDTO> getAllNotifications();
    Optional<NotificationDTO> getNotificationById(Long id);
    NotificationDTO createNotification(@Valid NotificationDTO notificationDTO);
    Optional<NotificationDTO> updateNotification(Long id, @Valid NotificationDTO notificationDTO);
    void deleteNotification(Long id);
    List<NotificationDTO> getNotificationsByUserId(Long userId);
    void markAsRead(Long notificationId);
}
