package org.una.programmingIII.UTEMP_Project.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.una.programmingIII.UTEMP_Project.models.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Boolean existsByIdentificationNumber(String identificationNumber);
}
