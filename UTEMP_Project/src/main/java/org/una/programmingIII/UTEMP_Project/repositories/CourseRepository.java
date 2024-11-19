package org.una.programmingIII.UTEMP_Project.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.una.programmingIII.UTEMP_Project.models.Course;

public interface CourseRepository extends JpaRepository<Course, Long> {
    @Query("SELECT c FROM Course c WHERE c.teacher.id = :teacherId")
    Page<Course> findByTeacherId(@Param("teacherId") Long teacherId, Pageable pageable);

    @Query("SELECT c FROM Course c JOIN FETCH c.teacher WHERE c.department.id = :departmentId")
    Page<Course> findByDepartmentIdWithTeacher(@Param("departmentId") Long departmentId, Pageable pageable);

}
