package org.una.programmingIII.UTEMP_Project.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.una.programmingIII.UTEMP_Project.models.Assignment;
import org.una.programmingIII.UTEMP_Project.models.Course;

import java.util.List;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    List<Assignment> findByCourse(Course course);
}

