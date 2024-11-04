package org.una.programmingIII.UTEMP_Project.services.user;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.una.programmingIII.UTEMP_Project.dtos.CourseDTO;
import org.una.programmingIII.UTEMP_Project.dtos.EnrollmentDTO;
import org.una.programmingIII.UTEMP_Project.dtos.UserDTO;

import java.util.Optional;

public interface UserService {
    Page<UserDTO> getAllUsers(Pageable pageable);

    UserDTO createUser(@Valid UserDTO userDTO);

    Optional<UserDTO> getUserById(Long id);

    Optional<UserDTO> getUserByIdentificationNumber(String identificationNumber);

    Optional<UserDTO> updateUser(Long id, @Valid UserDTO userDTO);

    boolean deleteUser(Long id, Boolean isPermanentDelete);

    Page<CourseDTO> getCoursesTeachingByUserId(Long teacherId, Pageable pageable);

    void assignCourseToTeacher(Long userId, Long courseId);

    void removeCourseFromTeacher(Long userId, Long courseId);

    Page<EnrollmentDTO> getEnrollmentsByStudentId(Long studentId, Pageable pageable);

    void enrollUserToCourse(Long userId, Long courseId);

    void unrollUserFromCourse(Long userId, Long courseId);
}
