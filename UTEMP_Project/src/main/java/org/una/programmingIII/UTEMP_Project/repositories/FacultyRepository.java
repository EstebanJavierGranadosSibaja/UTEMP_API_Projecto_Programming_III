package org.una.programmingIII.UTEMP_Project.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.una.programmingIII.UTEMP_Project.models.Faculty;
import org.una.programmingIII.UTEMP_Project.models.University;

import java.util.List;

public interface FacultyRepository  extends JpaRepository<Faculty, Long> {
    Page<Faculty> findByUniversityId(Long universityId, Pageable pageable);
}
