package org.una.programmingIII.UTEMP_Project.services.course;

import jakarta.validation.Valid;
import org.una.programmingIII.UTEMP_Project.dtos.CourseDTO;

import java.util.List;
import java.util.Optional;

public interface CourseService {
    List<CourseDTO> getAllCourses();
    Optional<CourseDTO> getCourseById(Long id);
    CourseDTO createCourse(@Valid CourseDTO courseDTO);
    Optional<CourseDTO> updateCourse(Long id, @Valid CourseDTO courseDTO);
    void deleteCourse(Long id);
    List<CourseDTO> getCoursesByTeacherId(Long teacherId);
    List<CourseDTO> getCoursesByDepartmentId(Long departmentId);
    }
