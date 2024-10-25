package org.una.programmingIII.UTEMP_Project.services.faculty;

import jakarta.validation.Valid;
import org.una.programmingIII.UTEMP_Project.dtos.DepartmentDTO;
import org.una.programmingIII.UTEMP_Project.dtos.FacultyDTO;

import java.util.List;
import java.util.Optional;

public interface FacultyService {
    List<FacultyDTO> getAllFaculties() ;
    Optional<FacultyDTO> getFacultyById(Long id);
    FacultyDTO createFaculty(FacultyDTO facultyDTO);
    Optional<FacultyDTO> updateFaculty(Long id, @Valid FacultyDTO facultyDTO);
    void deleteFaculty(Long id);
    List<DepartmentDTO> getDepartmentsByFacultyId(Long facultyId);
    void addDepartmentToFaculty(Long facultyId, DepartmentDTO departmentDTO);
    void removeDepartmentFromFaculty(Long facultyId, Long departmentId);
}
