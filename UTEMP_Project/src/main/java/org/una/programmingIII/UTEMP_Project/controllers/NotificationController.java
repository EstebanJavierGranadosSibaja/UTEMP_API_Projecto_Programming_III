package org.una.programmingIII.UTEMP_Project.controllers;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.una.programmingIII.UTEMP_Project.dtos.NotificationDTO;
import org.una.programmingIII.UTEMP_Project.dtos.UserDTO;
import org.una.programmingIII.UTEMP_Project.models.User;
import org.una.programmingIII.UTEMP_Project.services.notification.NotificationService;
import org.una.programmingIII.UTEMP_Project.transformers.mappers.GenericMapper;
import org.una.programmingIII.UTEMP_Project.transformers.mappers.GenericMapperFactory;
import org.una.programmingIII.UTEMP_Project.exceptions.ResourceNotFoundException;
import org.una.programmingIII.UTEMP_Project.exceptions.InvalidDataException;

import java.util.Optional;

@RestController
@RequestMapping("/utemp/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final GenericMapper<User, UserDTO> userMapper;

    @Autowired
    NotificationController(GenericMapperFactory mapperFactory, NotificationService notificationService) {
        this.userMapper = mapperFactory.createMapper(User.class, UserDTO.class);
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<Page<NotificationDTO>> getAllNotifications(Pageable pageable) {
        try {
            Page<NotificationDTO> notifications = notificationService.getAllNotifications(pageable);
            return new ResponseEntity<>(notifications, HttpStatus.OK);
        } catch (Exception e) {
            throw new InvalidDataException("Error retrieving notifications: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationDTO> getNotificationById(@PathVariable Long id) {
        try {
            Optional<NotificationDTO> notificationDTO = notificationService.getNotificationById(id);
            return notificationDTO.map(dto -> new ResponseEntity<>(dto, HttpStatus.OK))
                    .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " , id));
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidDataException("Error retrieving notification: " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<NotificationDTO> createNotification(@Valid @RequestBody NotificationDTO notificationDTO) {
        try {
            NotificationDTO createdNotification = notificationService.createNotification(notificationDTO);
            return new ResponseEntity<>(createdNotification, HttpStatus.CREATED);
        } catch (Exception e) {
            throw new InvalidDataException("Error creating notification: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<NotificationDTO> updateNotification(@PathVariable Long id,
                                                              @Valid @RequestBody NotificationDTO notificationDTO) {
        try {
            Optional<NotificationDTO> updatedNotification = notificationService.updateNotification(id, notificationDTO);
            return updatedNotification.map(dto -> new ResponseEntity<>(dto, HttpStatus.OK))
                    .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " , id));
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidDataException("Error updating notification: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id) {
        try {
            notificationService.deleteNotification(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidDataException("Error deleting notification: " + e.getMessage());
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<NotificationDTO>> getNotificationsByUserId(@PathVariable Long userId, Pageable pageable) {
        try {
            Page<NotificationDTO> notifications = notificationService.getNotificationsByUserId(userId, pageable);
            return new ResponseEntity<>(notifications, HttpStatus.OK);
        } catch (Exception e) {
            throw new InvalidDataException("Error retrieving notifications for user: " + e.getMessage());
        }
    }

    @PostMapping("/user/{userId}")
    public ResponseEntity<Void> addNotificationToUser(@PathVariable Long userId, @Valid @RequestBody NotificationDTO notificationDTO) {
        try {
            notificationService.addNotificationToUser(userId, notificationDTO);
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (Exception e) {
            throw new InvalidDataException("Error adding notification to user: " + e.getMessage());
        }
    }

    @DeleteMapping("/user/{userId}/notification/{notificationId}")
    public ResponseEntity<Void> removeNotificationFromUser(@PathVariable Long userId, @PathVariable Long notificationId) {
        try {
            notificationService.removeNotificationFromUser(userId, notificationId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            throw new InvalidDataException("Error removing notification from user: " + e.getMessage());
        }
    }

    @PutMapping("/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long notificationId) {
        try {
            notificationService.markAsRead(notificationId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            throw new InvalidDataException("Error marking notification as read: " + e.getMessage());
        }
    }

    @PostMapping("/send")
    public ResponseEntity<Void> sendNotificationToUser(@Valid @RequestBody NotificationDTO notificationDTO, @Valid @RequestBody UserDTO userDTO) {
        try {
            notificationService.sendNotificationToUser(userMapper.convertToEntity(userDTO), notificationDTO.getMessage());
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (Exception e) {
            throw new InvalidDataException("Error sending notification to user: " + e.getMessage());
        }
    }
}