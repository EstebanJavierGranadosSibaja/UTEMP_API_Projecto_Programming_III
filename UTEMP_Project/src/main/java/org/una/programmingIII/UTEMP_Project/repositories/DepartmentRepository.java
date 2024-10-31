package org.una.programmingIII.UTEMP_Project.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.una.programmingIII.UTEMP_Project.models.Department;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
    Page<Department> findByFacultyId(Long facultyId, Pageable pageable);
}
