package org.una.programmingIII.UTEMP_Project.services.UniversityServices;

import jakarta.validation.Valid;
import org.una.programmingIII.UTEMP_Project.dtos.FacultyDTO;
import org.una.programmingIII.UTEMP_Project.dtos.UniversityDTO;

import java.util.List;
import java.util.Optional;

public interface UniversityService {
    List<UniversityDTO> getAllUniversities();
    Optional<UniversityDTO> getUniversityById(Long id);
    UniversityDTO createUniversity(UniversityDTO universityDTO);
    Optional<UniversityDTO> updateUniversity(Long id, @Valid UniversityDTO universityDTO);
    void deleteUniversity(Long id);
    List<FacultyDTO> getFacultiesByUniversityId(Long universityId);
    void addFacultyToUniversity(Long universityId, FacultyDTO facultyDTO);
    void removeFacultyFromUniversity(Long universityId, Long facultyId);


    }
