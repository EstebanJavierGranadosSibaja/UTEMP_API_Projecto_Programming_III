package org.una.programmingIII.UTEMP_Project.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.una.programmingIII.UTEMP_Project.models.Faculty;
import org.una.programmingIII.UTEMP_Project.models.User;
import org.una.programmingIII.UTEMP_Project.models.UserRole;

public interface UserRepository extends JpaRepository<User, Long> {
    Boolean existsByIdentificationNumber(String identificationNumber);

    @Query("SELECT u FROM User u WHERE u.role = :role")
    Page<User> getAllUsersByRole(@Param("role") UserRole role, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.identificationNumber = :identificationNumber")
    User findByIdentificationNumber(@Param("identificationNumber") String identificationNumber);

    @Query("SELECT u FROM User u WHERE u.role = :role")
    User findByRole(@Param("role") String role);
}
