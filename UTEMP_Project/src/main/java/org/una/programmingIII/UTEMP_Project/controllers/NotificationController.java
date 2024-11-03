package org.una.programmingIII.UTEMP_Project.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.una.programmingIII.UTEMP_Project.dtos.NotificationDTO;
import org.una.programmingIII.UTEMP_Project.exceptions.InvalidDataException;
import org.una.programmingIII.UTEMP_Project.exceptions.ResourceNotFoundException;
import org.una.programmingIII.UTEMP_Project.services.notification.NotificationService;

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

    @Operation(
            summary = "Get all notifications",
            description = "Retrieve a paginated list of all notifications associated with users."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully fetched all notifications.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = NotificationDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"Internal server error.\"}")
                    )
            )
    })
    @GetMapping
    @PreAuthorize("hasAuthority('GET_ALL_NOTIS')")
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

    @Operation(
            summary = "Get notification by ID",
            description = "Retrieve a specific notification by its ID."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully fetched notification.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = NotificationDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Notification not found.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"Notification not found.\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"Internal server error.\"}")
                    )
            )
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('GET_NOTI_BY_ID')")
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

    @Operation(
            summary = "Create a new notification",
            description = "Create a new notification record."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Successfully created notification.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = NotificationDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid data provided.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"Invalid data for notification creation.\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"Internal server error.\"}")
                    )
            )
    })
    @PostMapping
    @PreAuthorize("hasAuthority('CREATE_NOTI')")
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

    @Operation(
            summary = "Update an existing notification",
            description = "Update a notification record by its ID."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully updated notification.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = NotificationDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Notification not found.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"Notification not found for update.\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid data provided.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"Invalid data for notification update.\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"Internal server error.\"}")
                    )
            )
    })
    @PutMapping("/{NotiId}")
    @PreAuthorize("hasAuthority('UPDATE_NOTI')")
    public ResponseEntity<NotificationDTO> updateNotification(@PathVariable Long NotiId,
                                                              @Valid @RequestBody NotificationDTO notificationDTO) {
        try {
            Optional<NotificationDTO> updatedNotification = notificationService.updateNotification(NotiId, notificationDTO);
            return updatedNotification.map(ResponseEntity::ok)
                    .orElseGet(() -> {
                        logger.warn("Notification not found for update with ID: {}", NotiId);
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
                    });
        } catch (InvalidDataException e) {
            logger.warn("Invalid data for notification update with ID {}: {}", NotiId, e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (ResourceNotFoundException e) {
            logger.warn("Notification not found with ID: {}", NotiId, e);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            logger.error("Error updating notification with ID {}: {}", NotiId, e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(
            summary = "Delete a notification",
            description = "Delete a specific notification by its ID."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Successfully deleted notification."
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Notification not found.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"Notification not found.\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"Internal server error.\"}")
                    )
            )
    })
    @DeleteMapping("/{NotiId}")
    @PreAuthorize("hasAuthority('DELETE_NOTI')")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long NotiId) {
        try {
            notificationService.deleteNotification(NotiId);
            logger.info("Deleted notification with ID: {}", NotiId);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            logger.warn("Notification not found with ID: {}", NotiId, e);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            logger.error("Error deleting notification with ID {}: {}", NotiId, e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(
            summary = "Get notifications for a specific user",
            description = "Retrieve all notifications associated with a specific user ID."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully fetched notifications for user.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = NotificationDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"Internal server error.\"}")
                    )
            )
    })
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAuthority('GET_NOTIS_OF_USER')")
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

    @Operation(
            summary = "Add notification to a specific user",
            description = "Add a new notification associated with a specific user ID."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Successfully added notification to user."
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid data provided.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"Invalid data.\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"Internal server error.\"}")
                    )
            )
    })
    @PostMapping("/user/{userId}")
    @PreAuthorize("hasAuthority('ADD_NOTI_TO_USER')")
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

    @Operation(
            summary = "Remove a notification from a specific user",
            description = "Remove a notification associated with a specific user ID and notification ID."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Successfully removed notification from user."
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Notification or user not found.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"Notification or user not found.\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"Internal server error.\"}")
                    )
            )
    })
    @DeleteMapping("/user/{userId}/{notificationId}")
    @PreAuthorize("hasAuthority('REMOVE_NOTI_TO_USER')")
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

    @Operation(
            summary = "Mark a notification as read",
            description = "Marks a specific notification as read by its ID."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Successfully marked notification as read."
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Notification not found.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"Notification not found.\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"Internal server error.\"}")
                    )
            )
    })
    @PostMapping("/markAsRead/{notificationId}")
    @PreAuthorize("hasAuthority('MARK_NOTI_AS_READ')")
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

    @Operation(
            summary = "Send a notification to a user",
            description = "Sends a notification message to a specified user by their ID."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Successfully sent notification to user."
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"Internal server error.\"}")
                    )
            )
    })
    @PostMapping("/send")
    @PreAuthorize("hasAuthority('SEND_NOTI_TO_USER')")
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