package org.una.programmingIII.UTEMP_Project.controllers;


import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.una.programmingIII.UTEMP_Project.dtos.CourseDTO;
import org.una.programmingIII.UTEMP_Project.dtos.EnrollmentDTO;
import org.una.programmingIII.UTEMP_Project.dtos.NotificationDTO;
import org.una.programmingIII.UTEMP_Project.dtos.UserDTO;
import org.una.programmingIII.UTEMP_Project.controllers.responses.ApiResponse;
import org.una.programmingIII.UTEMP_Project.controllers.responses.PageResponse;
import org.una.programmingIII.UTEMP_Project.services.user.CustomUserDetails;
import org.una.programmingIII.UTEMP_Project.services.user.UserService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/utemp/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    //crud basico
    @GetMapping
    @PreAuthorize("hasAuthority('MANAGE_USERS')")
    public ResponseEntity<PageResponse<UserDTO>> getAllUsers(@PageableDefault(size = 10, page = 0) Pageable pageable) {
        Page<UserDTO> page = userService.getAllUsers(pageable);
        return ResponseEntity.ok(new PageResponse<UserDTO>(page));
    }

    // Obtener usuario por número de identificación
    @GetMapping("/identification/{identificationNumber}")
    @PreAuthorize("hasAuthority('MANAGE_USERS') or hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<UserDTO>> getUserByIdentificationNumber(@PathVariable String identificationNumber) {
        Optional<UserDTO> userDTO = userService.getUserByIdentificationNumber(identificationNumber);
        return getApiResponseResponseEntity(userDTO);
    }

    // Obtener usuario actual
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserDTO>> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long authenticatedUserId = ((CustomUserDetails) authentication.getPrincipal()).getUser().getId();

        Optional<UserDTO> userDTO = userService.getUserById(authenticatedUserId);
        return getApiResponseResponseEntity(userDTO);
    }

    // Crear usuario
    @PostMapping
    @PreAuthorize("hasAuthority('MANAGE_USERS')")
    public ResponseEntity<ApiResponse<UserDTO>> createUser(@Valid @RequestBody UserDTO userDTO) {
        UserDTO createdUser = userService.createUser(userDTO);
        ApiResponse<UserDTO> response = new ApiResponse<>(createdUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Actualizar usuario
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_USERS')")
    public ResponseEntity<ApiResponse<UserDTO>> updateUser(@PathVariable Long id, @Valid @RequestBody UserDTO userDTO) {
        Optional<UserDTO> updatedUser = userService.updateUser(id, userDTO);
        return getApiResponseResponseEntity(updatedUser);
    }

    // Eliminar usuario
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_USERS')")
    public ResponseEntity<ApiResponse<Boolean>> deleteUser(@PathVariable Long id, @RequestParam(defaultValue = "false") Boolean isPermanentDelete) {
        boolean deleted = userService.deleteUser(id, isPermanentDelete);
        if (deleted) {
            ApiResponse<Boolean> response = new ApiResponse<>(true);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
        } else {
            ApiResponse<Boolean> response = new ApiResponse<>(HttpStatus.NOT_FOUND.value(), "User not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    // Método privado para manejar la respuesta de usuario
    private ResponseEntity<ApiResponse<UserDTO>> getApiResponseResponseEntity(Optional<UserDTO> userDTO) {
        return userDTO.map(user -> {
            ApiResponse<UserDTO> response = new ApiResponse<>(user);
            return ResponseEntity.ok(response);
        }).orElseGet(() -> {
            ApiResponse<UserDTO> response = new ApiResponse<>(HttpStatus.NOT_FOUND.value(), "User not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        });
    }


    //Metodo sobre los elementos de usuario
//    @GetMapping("/{id}/notifications")
//    @PreAuthorize("hasAuthority('NOTIFICATION')")
//    public ResponseEntity<ApiResponse<List<NotificationDTO>>> getUserNotifications(@PathVariable Long id) {
//        List<NotificationDTO> notifications = userService.getUserNotifications(id);
//        ApiResponse<List<NotificationDTO>> response = new ApiResponse<>(notifications);
//        return ResponseEntity.ok(response);
//    }

    @GetMapping("/{id}/courses")
    @PreAuthorize("hasAuthority('USER_INFO')")
    public ResponseEntity<ApiResponse<List<CourseDTO>>> getCoursesTeachingByUserId(@PathVariable Long id) {
        List<CourseDTO> courses = userService.getCoursesTeachingByUserId(id);
        ApiResponse<List<CourseDTO>> response = new ApiResponse<>(courses);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/enrollments")
    @PreAuthorize("hasAuthority('USER_INFO')")
    public ResponseEntity<ApiResponse<List<EnrollmentDTO>>> getEnrollmentsByUserId(@PathVariable Long id) {
        List<EnrollmentDTO> enrollments = userService.getEnrollmentsByUserId(id);
        ApiResponse<List<EnrollmentDTO>> response = new ApiResponse<>(enrollments);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{userId}/enrollments/{courseId}")
    @PreAuthorize("hasAuthority('USER_INFO')")
    public ResponseEntity<ApiResponse<String>> enrollUserToCourse(@PathVariable Long userId, @PathVariable Long courseId) {
        userService.enrollUserToCourse(userId, courseId);
        ApiResponse<String> response = new ApiResponse<>("User enrolled successfully");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{userId}/enrollments/{courseId}")
    @PreAuthorize("hasAuthority('USER_INFO')")
    public ResponseEntity<ApiResponse<String>> unrollUserFromCourse(@PathVariable Long userId, @PathVariable Long courseId) {
        userService.unrollUserFromCourse(userId, courseId);
        ApiResponse<String> response = new ApiResponse<>("User unrolled successfully");
        return ResponseEntity.ok(response);
    }

    //cursos permiso
    @PostMapping("/{userId}/courses/{courseId}")
    @PreAuthorize("hasAuthority('MANAGER_CURSE')")//TODO
    public ResponseEntity<ApiResponse<String>> assignCourseToTeacher(@PathVariable Long userId, @PathVariable Long courseId) {
        userService.assignCourseToTeacher(userId, courseId);
        ApiResponse<String> response = new ApiResponse<>("Course assigned successfully");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{userId}/courses/{courseId}")
    @PreAuthorize("hasAuthority('MANAGER_CURSE')")
    public ResponseEntity<ApiResponse<String>> removeCourseFromTeacher(@PathVariable Long userId, @PathVariable Long courseId) {
        userService.removeCourseFromTeacher(userId, courseId);
        ApiResponse<String> response = new ApiResponse<>("Course removed successfully");
        return ResponseEntity.ok(response);
    }

//    //TODO notificaciones, uso de patron estructural de tipo subcripcion por curso, se envia a los estudiantes la dentro del un curso, se genera la lista
//    @PostMapping("/{userId}/notifications")
//    @PreAuthorize("hasAuthority('NOTIFICATION')")
//    public ResponseEntity<ApiResponse<String>> addNotificationToUser(@PathVariable Long userId, @Valid @RequestBody NotificationDTO notificationDTO) {
//        userService.addNotificationToUser(userId, notificationDTO);
//        ApiResponse<String> response = new ApiResponse<>("Notification added successfully");
//        return ResponseEntity.ok(response);
//    }
//
//    //TODO?
//    @DeleteMapping("/{userId}/notifications/{notificationId}")
//    @PreAuthorize("hasAuthority('NOTIFICATION')")
//    public ResponseEntity<ApiResponse<String>> removeNotificationFromUser(@PathVariable Long userId, @PathVariable Long notificationId) {
//        userService.removeNotificationFromUser(userId, notificationId);
//        ApiResponse<String> response = new ApiResponse<>("Notification removed successfully");
//        return ResponseEntity.ok(response);
//    }

}
