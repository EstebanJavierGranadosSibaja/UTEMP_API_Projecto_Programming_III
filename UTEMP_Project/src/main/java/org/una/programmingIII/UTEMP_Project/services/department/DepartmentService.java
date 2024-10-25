package org.una.programmingIII.UTEMP_Project.services.department;

import jakarta.validation.Valid;
import org.una.programmingIII.UTEMP_Project.dtos.CourseDTO;
import org.una.programmingIII.UTEMP_Project.dtos.DepartmentDTO;

import java.util.List;
import java.util.Optional;

public interface DepartmentService {
    List<DepartmentDTO> getAllDepartments();
    Optional<DepartmentDTO> getDepartmentById(Long id);
    DepartmentDTO createDepartment(DepartmentDTO departmentDTO);
    Optional<DepartmentDTO> updateDepartment(Long id, @Valid DepartmentDTO departmentDTO);
    void deleteDepartment(Long id);
    List<CourseDTO> getCoursesByDepartmentId(Long departmentId);
    void addCourseToDepartment(Long departmentId, CourseDTO courseDTO);
    void removeCourseFromDepartment(Long departmentId, Long courseId);
}
