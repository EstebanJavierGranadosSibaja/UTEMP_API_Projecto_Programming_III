package org.una.programmingIII.UTEMP_Project.services.department;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.una.programmingIII.UTEMP_Project.dtos.CourseDTO;
import org.una.programmingIII.UTEMP_Project.dtos.DepartmentDTO;

import java.util.List;
import java.util.Optional;

public interface DepartmentService {
    Page<DepartmentDTO> getAllDepartments(Pageable pageable);
    Optional<DepartmentDTO> getDepartmentById(Long id);
    DepartmentDTO createDepartment(DepartmentDTO departmentDTO);
    Optional<DepartmentDTO> updateDepartment(Long id, @Valid DepartmentDTO departmentDTO);
    void deleteDepartment(Long id);
    Page<CourseDTO> getCoursesByDepartmentId(Long departmentId, Pageable pageable);
    void addCourseToDepartment(Long departmentId, CourseDTO courseDTO);
    void removeCourseFromDepartment(Long departmentId, Long courseId);
}
