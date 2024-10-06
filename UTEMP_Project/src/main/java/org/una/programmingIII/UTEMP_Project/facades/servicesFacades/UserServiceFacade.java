package org.una.programmingIII.UTEMP_Project.facades.servicesFacades;

import jakarta.validation.Valid;
import org.una.programmingIII.UTEMP_Project.dtos.*;
import org.una.programmingIII.UTEMP_Project.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class UserServiceFacade {

    @Autowired
    private UserService userService;

    public List<UserDTO> getAllUsers() {
        return userService.getAllUsers();
    }

    public Optional<UserDTO> getUserById(Long id) {
        return userService.getUserById(id);
    }

    public UserDTO createUser(@Valid UserDTO userDTO) {
        return userService.createUser(userDTO);
    }

    public Optional<UserDTO> updateUser(Long id, @Valid UserDTO userDTO) {
        return userService.updateUser(id, userDTO);
    }

    public void deleteUser(Long id, Boolean isPermanentDelete) {
        userService.deleteUser(id, isPermanentDelete);
    }

    public void enrollUserToCourse(Long userId, Long courseId) {
        userService.enrollUserToCourse(userId, courseId);
    }

    public List<NotificationDTO> getUserNotifications(Long userId) {
        return userService.getUserNotifications(userId);
    }

    public void addNotificationToUser(Long userId, NotificationDTO notificationDTO) {
        userService.addNotificationToUser(userId, notificationDTO);
    }
}