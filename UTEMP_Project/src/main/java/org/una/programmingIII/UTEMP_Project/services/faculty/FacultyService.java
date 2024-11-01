package org.una.programmingIII.UTEMP_Project.services.faculty;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.una.programmingIII.UTEMP_Project.dtos.DepartmentDTO;
import org.una.programmingIII.UTEMP_Project.dtos.FacultyDTO;

import java.util.List;
import java.util.Optional;

public interface FacultyService {
    Page<FacultyDTO> getAllFaculties(@PageableDefault(size = 10, page = 0) Pageable pageable) ;
    Optional<FacultyDTO> getFacultyById(Long id);
    FacultyDTO createFaculty(FacultyDTO facultyDTO);
    Optional<FacultyDTO> updateFaculty(Long id, @Valid FacultyDTO facultyDTO);
    void deleteFaculty(Long id);
    Page<FacultyDTO> getFacultiesByUniversityId(Long universityId, @PageableDefault(size = 10, page = 0) Pageable pageable);
    void addDepartmentToFaculty(Long facultyId, DepartmentDTO departmentDTO);
    void removeDepartmentFromFaculty(Long facultyId, Long departmentId);
}
