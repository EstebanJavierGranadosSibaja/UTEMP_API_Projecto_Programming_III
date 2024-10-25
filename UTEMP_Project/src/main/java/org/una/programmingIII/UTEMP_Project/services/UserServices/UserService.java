package org.una.programmingIII.UTEMP_Project.services.UserServices;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.una.programmingIII.UTEMP_Project.dtos.CourseDTO;
import org.una.programmingIII.UTEMP_Project.dtos.EnrollmentDTO;
import org.una.programmingIII.UTEMP_Project.dtos.UserDTO;

import java.util.List;
import java.util.Optional;

public interface UserService {

    //CRUD
    UserDTO createUser(@Valid UserDTO userDTO);

    //GetsBY()
    Optional<UserDTO> getUserById(Long id);

    Optional<UserDTO> getUserByIdentificationNumber(String identificationNumber);

    //Gets list(Page)
    Page<UserDTO> getAllUsers(Pageable pageable);

    Optional<UserDTO> updateUser(Long id, @Valid UserDTO userDTO);

    boolean deleteUser(Long id, Boolean isPermanentDelete);

    /// ---------------///
    List<CourseDTO> getCoursesTeachingByUserId(Long userId);

    void assignCourseToTeacher(Long userId, Long courseId);

    void removeCourseFromTeacher(Long userId, Long courseId);

    List<EnrollmentDTO> getEnrollmentsByUserId(Long userId);

    void enrollUserToCourse(Long userId, Long courseId);

    void unrollUserFromCourse(Long userId, Long courseId);

}
