package org.una.programmingIII.UTEMP_Project.services;

import jakarta.validation.Valid;
import org.una.programmingIII.UTEMP_Project.dtos.*;
import java.util.List;
import java.util.Optional;

public interface UserService {
    List<UserDTO> getAllUsers();
    Optional<UserDTO> getUserById(Long id);
    UserDTO createUser(@Valid UserDTO userDTO);
    Optional<UserDTO> updateUser(Long id, @Valid UserDTO userDTO);
    void deleteUser(Long id, Boolean isPermanentDelete);
    void enrollUserToCourse(Long userId, Long courseId);
    List<NotificationDTO> getUserNotifications(Long userId);
    void addNotificationToUser(Long userId, NotificationDTO notificationDTO);
}
