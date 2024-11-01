package org.una.programmingIII.UTEMP_Project.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.una.programmingIII.UTEMP_Project.models.Grade;

import java.util.Optional;

public interface GradeRepository extends JpaRepository<Grade, Long> {
    @Query("SELECT g FROM Grade g WHERE g.submission.id = :submissionId")
    Page<Grade> findBySubmissionsId(@Param("submissionId") Long submissionId, Pageable pageable);
    Optional<Grade> findBySubmissionId(Long submissionId);
}
