package org.una.programmingIII.UTEMP_Project.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.una.programmingIII.UTEMP_Project.models.User;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User, Long> {
    Boolean existsByIdentificationNumber(String identificationNumber);
    @Query("SELECT u FROM User u WHERE u.identificationNumber = :identificationNumber")
    User findByIdentificationNumber(@Param("identificationNumber") String identificationNumber);
}
