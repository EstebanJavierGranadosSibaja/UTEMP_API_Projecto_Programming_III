package org.una.programmingIII.UTEMP_Project.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.una.programmingIII.UTEMP_Project.models.Course;
import org.una.programmingIII.UTEMP_Project.models.Enrollment;
import org.una.programmingIII.UTEMP_Project.models.User;

import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    Optional<Enrollment> findByStudentAndCourse(User user, Course course);
}
