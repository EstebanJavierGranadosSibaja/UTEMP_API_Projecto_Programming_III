package org.una.programmingIII.UTEMP_Project.facades.servicesFacades;

import jakarta.validation.Valid;
import org.una.programmingIII.UTEMP_Project.dtos.*;
import org.una.programmingIII.UTEMP_Project.services.UserServices.UserService;
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

    public List<CourseDTO> getCoursesTeachingByUserId(Long userId){
        return userService.getCoursesTeachingByUserId(userId);
    };

    public void assignCourseToTeacher(Long userId, Long courseId) {
        userService.assignCourseToTeacher(userId, courseId);
    }

    public void removeCourseFromTeacher(Long userId, Long courseId) {
        userService.removeCourseFromTeacher(userId, courseId);
    }

    public List<EnrollmentDTO> getEnrollmentsByUserId(Long userId){
        return userService.getEnrollmentsByUserId(userId);
    };

    public void enrollUserToCourse(Long userId, Long courseId) {
        userService.enrollUserToCourse(userId, courseId);
    }

    public void unrollUserFromCourse(Long userId, Long courseId) {
        userService.unrollUserFromCourse(userId, courseId);
    }

    public List<NotificationDTO> getUserNotifications(Long userId) {
        return userService.getUserNotifications(userId);
    }

    public void addNotificationToUser(Long userId, NotificationDTO notificationDTO) {
        userService.addNotificationToUser(userId, notificationDTO);
    }

    public void removeNotificationFromUser(Long userId, Long notificationId) {
        userService.removeNotificationFromUser(userId, notificationId);
    }
}