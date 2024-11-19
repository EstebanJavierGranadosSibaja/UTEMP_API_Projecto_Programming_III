package org.una.programmingIII.UTEMP_Project.services.university;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.una.programmingIII.UTEMP_Project.dtos.FacultyDTO;
import org.una.programmingIII.UTEMP_Project.dtos.UniversityDTO;

import java.util.Optional;

public interface UniversityService {
    Page<UniversityDTO> getAllUniversities(Pageable pageable);

    Optional<UniversityDTO> getUniversityById(Long id);

    UniversityDTO createUniversity(UniversityDTO universityDTO);

    Optional<UniversityDTO> updateUniversity(Long id, @Valid UniversityDTO universityDTO);

    void deleteUniversity(Long id);

    void addFacultyToUniversity(Long universityId, FacultyDTO facultyDTO);

    void removeFacultyFromUniversity(Long universityId, Long facultyId);
}
