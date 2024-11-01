package org.una.programmingIII.UTEMP_Project.controllers;

import org.una.programmingIII.UTEMP_Project.dtos.NotificationDTO;
import org.una.programmingIII.UTEMP_Project.services.notification.NotificationService;
import org.una.programmingIII.UTEMP_Project.exceptions.InvalidDataException;
import org.una.programmingIII.UTEMP_Project.exceptions.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Optional;

@RestController
@RequestMapping("/utemp/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final Logger logger = LoggerFactory.getLogger(NotificationController.class);

    @Autowired
    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<Page<NotificationDTO>> getAllNotifications(Pageable pageable) {
        try {
            Page<NotificationDTO> notifications = notificationService.getAllNotifications(pageable);
            logger.info("Fetched all notifications successfully.");
            return new ResponseEntity<>(notifications, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error retrieving notifications: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationDTO> getNotificationById(@PathVariable Long id) {
        try {
            Optional<NotificationDTO> notification = notificationService.getNotificationById(id);
            return notification.map(ResponseEntity::ok)
                    .orElseGet(() -> {
                        logger.warn("Notification not found with ID: {}", id);
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
                    });
        } catch (Exception e) {
            logger.error("Error retrieving notification with ID {}: {}", id, e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping
    public ResponseEntity<NotificationDTO> createNotification(@Valid @RequestBody NotificationDTO notificationDTO) {
        try {
            NotificationDTO createdNotification = notificationService.createNotification(notificationDTO);
            logger.info("Created new notification: {}", createdNotification);
            return new ResponseEntity<>(createdNotification, HttpStatus.CREATED);
        } catch (InvalidDataException e) {
            logger.warn("Invalid data for notification creation: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Error creating notification: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<NotificationDTO> updateNotification(@PathVariable Long id,
                                                              @Valid @RequestBody NotificationDTO notificationDTO) {
        try {
            Optional<NotificationDTO> updatedNotification = notificationService.updateNotification(id, notificationDTO);
            return updatedNotification.map(ResponseEntity::ok)
                    .orElseGet(() -> {
                        logger.warn("Notification not found for update with ID: {}", id);
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
                    });
        } catch (InvalidDataException e) {
            logger.warn("Invalid data for notification update with ID {}: {}", id, e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (ResourceNotFoundException e) {
            logger.warn("Notification not found with ID: {}", id, e);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            logger.error("Error updating notification with ID {}: {}", id, e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id) {
        try {
            notificationService.deleteNotification(id);
            logger.info("Deleted notification with ID: {}", id);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            logger.warn("Notification not found with ID: {}", id, e);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            logger.error("Error deleting notification with ID {}: {}", id, e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<NotificationDTO>> getNotificationsByUserId(@PathVariable Long userId, Pageable pageable) {
        try {
            Page<NotificationDTO> notifications = notificationService.getNotificationsByUserId(userId, pageable);
            logger.info("Fetched notifications for user ID: {}", userId);
            return new ResponseEntity<>(notifications, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error retrieving notifications for user ID {}: {}", userId, e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/user/{userId}")
    public ResponseEntity<Void> addNotificationToUser(@PathVariable Long userId,
                                                      @Valid @RequestBody NotificationDTO notificationDTO) {
        try {
            notificationService.addNotificationToUser(userId, notificationDTO);
            logger.info("Added notification to user ID: {}", userId);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (InvalidDataException e) {
            logger.warn("Invalid data for adding notification to user ID {}: {}", userId, e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Error adding notification to user ID {}: {}", userId, e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/user/{userId}/{notificationId}")
    public ResponseEntity<Void> removeNotificationFromUser(@PathVariable Long userId,
                                                           @PathVariable Long notificationId) {
        try {
            notificationService.removeNotificationFromUser(userId, notificationId);
            logger.info("Removed notification with ID: {} from user ID: {}", notificationId, userId);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            logger.warn("Notification or user not found: userId={}, notificationId={}", userId, notificationId, e);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            logger.error("Error removing notification from user ID {}: {}", userId, e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/markAsRead/{notificationId}")
    public ResponseEntity<Void> markAsRead(@PathVariable Long notificationId) {
        try {
            notificationService.markAsRead(notificationId);
            logger.info("Marked notification as read with ID: {}", notificationId);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            logger.warn("Notification not found with ID: {}", notificationId, e);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            logger.error("Error marking notification as read with ID {}: {}", notificationId, e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/send")
    public ResponseEntity<Void> sendNotificationToUser(@RequestParam Long userId,
                                                       @RequestParam String message) {
        try {
            notificationService.sendNotificationToUser(userId, message);
            logger.info("Sent notification to user ID: {}", userId);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (Exception e) {
            logger.error("Error sending notification to user ID {}: {}", userId, e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}