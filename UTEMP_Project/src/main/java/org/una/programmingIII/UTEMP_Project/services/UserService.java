package org.una.programmingIII.UTEMP_Project.services;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.una.programmingIII.UTEMP_Project.dtos.CourseDTO;
import org.una.programmingIII.UTEMP_Project.dtos.EnrollmentDTO;
import org.una.programmingIII.UTEMP_Project.dtos.NotificationDTO;
import org.una.programmingIII.UTEMP_Project.dtos.UserDTO;
import org.una.programmingIII.UTEMP_Project.models.Course;
import org.una.programmingIII.UTEMP_Project.models.User;
import org.una.programmingIII.UTEMP_Project.models.UserRole;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface UserService {
    Page<UserDTO> getAllUsers(Pageable pageable);

    Page<UserDTO> getAllUsersByRole(UserRole role, Pageable pageable);

    UserDTO createUser(@Valid UserDTO userDTO);

    Optional<UserDTO> getUserById(Long id);

    Optional<UserDTO> getUserByIdentificationNumber(String identificationNumber);

    Optional<UserDTO> getUserByRole(UserRole role);

    Optional<UserDTO> updateUser(Long id, @Valid UserDTO userDTO);

    boolean deleteUser(Long id);

    Page<CourseDTO> getCoursesTeachingByUserId(Long teacherId, Pageable pageable);

    void assignCourseToTeacher(Long userId, Long courseId);

    void removeCourseFromTeacher(Long userId, Long courseId);

    Page<EnrollmentDTO> getEnrollmentsByStudentId(Long studentId, Pageable pageable);

    CompletableFuture<Void> enrollUserToCourse(Long userId, Long courseId);

    void unrollUserFromCourse(Long userId, Long courseId);

    CompletableFuture<Void> notifyUserAndProfessor(User user, Course course);
}
