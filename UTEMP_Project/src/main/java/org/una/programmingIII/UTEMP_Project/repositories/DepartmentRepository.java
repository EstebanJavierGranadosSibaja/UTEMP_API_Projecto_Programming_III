package org.una.programmingIII.UTEMP_Project.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.una.programmingIII.UTEMP_Project.models.Department;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
    @Query("SELECT d FROM Department d WHERE d.faculty.id = :facultyId")
    Page<Department> findByFacultyId(@Param("facultyId") Long facultyId, Pageable pageable);
}
