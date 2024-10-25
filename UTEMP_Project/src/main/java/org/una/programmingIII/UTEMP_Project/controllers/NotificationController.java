package org.una.programmingIII.UTEMP_Project.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.una.programmingIII.UTEMP_Project.dtos.NotificationDTO;
import org.una.programmingIII.UTEMP_Project.services.notification.NotificationService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/notifications")
@Validated
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<NotificationDTO>> getAllNotifications() {
        List<NotificationDTO> notifications = notificationService.getAllNotifications();
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationDTO> getNotificationById(@PathVariable Long id) {
        Optional<NotificationDTO> notification = notificationService.getNotificationById(id);
        return notification.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<NotificationDTO> createNotification(@RequestBody NotificationDTO notificationDTO) {
        NotificationDTO createdNotification = notificationService.createNotification(notificationDTO);
        return ResponseEntity.ok(createdNotification);
    }

    @PutMapping("/{id}")
    public ResponseEntity<NotificationDTO> updateNotification(@PathVariable Long id, @RequestBody NotificationDTO notificationDTO) {
        Optional<NotificationDTO> updatedNotification = notificationService.updateNotification(id, notificationDTO);
        return updatedNotification.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationDTO>> getNotificationsByUserId(@PathVariable Long userId) {
        List<NotificationDTO> notifications = notificationService.getNotificationsByUserId(userId);
        return ResponseEntity.ok(notifications);
    }

    @PutMapping("/{id}/mark-as-read")
    public ResponseEntity<Void> markNotificationAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.noContent().build();
    }
}
