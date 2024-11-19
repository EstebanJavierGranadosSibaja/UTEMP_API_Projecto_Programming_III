package org.una.programmingIII.UTEMP_Project.services.course;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.una.programmingIII.UTEMP_Project.dtos.AssignmentDTO;
import org.una.programmingIII.UTEMP_Project.dtos.CourseDTO;

import java.util.Optional;

public interface CourseService {
    Page<CourseDTO> getAllCourses(Pageable pageable);

    Optional<CourseDTO> getCourseById(Long id);

    CourseDTO createCourse(@Valid CourseDTO courseDTO);

    Optional<CourseDTO> updateCourse(Long id, @Valid CourseDTO courseDTO);

    void deleteCourse(Long id);

    Page<CourseDTO> getCoursesByTeacherId(Long teacherId, Pageable pageable);

    Page<CourseDTO> getCoursesByDepartmentId(Long departmentId, Pageable pageable);

    void addAssignmentToCourse(Long courseId, AssignmentDTO assignmentDTO);

    void removeAssignmentFromCourse(Long courseId, Long assignmentId);
}
