package org.una.programmingIII.UTEMP_Project.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.una.programmingIII.UTEMP_Project.models.Course;
import org.una.programmingIII.UTEMP_Project.models.Enrollment;
import org.una.programmingIII.UTEMP_Project.models.User;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    Optional<Enrollment> findByStudentAndCourse(User user, Course course);
    Page<Enrollment> findByCourseId(Long courseId, Pageable pageable);
    Page<Enrollment> findByStudentId(Long studentId, Pageable pageable);
}
