package org.una.programmingIII.UTEMP_Project.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.una.programmingIII.UTEMP_Project.models.Faculty;

public interface FacultyRepository extends JpaRepository<Faculty, Long> {
    @Query("SELECT f FROM Faculty f WHERE f.university.id = :universityId")
    Page<Faculty> findByUniversityId(@Param("universityId") Long universityId, Pageable pageable);
}