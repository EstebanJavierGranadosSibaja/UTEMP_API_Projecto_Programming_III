package org.una.programmingIII.UTEMP_Project.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.una.programmingIII.UTEMP_Project.models.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    @Query("SELECT n FROM Notification n WHERE n.user.id = :userId")
    Page<Notification> findByUserId(@Param("userId") Long userId, Pageable pageable);
}
