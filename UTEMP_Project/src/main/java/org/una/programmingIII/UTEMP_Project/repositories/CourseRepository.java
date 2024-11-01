package org.una.programmingIII.UTEMP_Project.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.una.programmingIII.UTEMP_Project.models.Course;
import org.una.programmingIII.UTEMP_Project.models.Department;
import org.una.programmingIII.UTEMP_Project.models.User;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {
    @Query("SELECT c FROM Course c WHERE c.teacher.id = :teacherId")
    Page<Course> findByTeacherId(@Param("teacherId") Long teacherId, Pageable pageable);
    @Query("SELECT c FROM Course c WHERE c.department.id = :departmentId")
    Page<Course> findByDepartmentId(@Param("departmentId") Long departmentId, Pageable pageable);
}
