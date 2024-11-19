package org.una.programmingIII.UTEMP_Project.services.faculty;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.una.programmingIII.UTEMP_Project.dtos.DepartmentDTO;
import org.una.programmingIII.UTEMP_Project.dtos.FacultyDTO;

import java.util.Optional;

public interface FacultyService {
    Page<FacultyDTO> getAllFaculties( Pageable pageable);

    Optional<FacultyDTO> getFacultyById(Long id);

    FacultyDTO createFaculty(FacultyDTO facultyDTO);

    Optional<FacultyDTO> updateFaculty(Long id, @Valid FacultyDTO facultyDTO);

    void deleteFaculty(Long id);

    Page<FacultyDTO> getFacultiesByUniversityId(Long universityId, Pageable pageable);

    void addDepartmentToFaculty(Long facultyId, DepartmentDTO departmentDTO);

    void removeDepartmentFromFaculty(Long facultyId, Long departmentId);
}
