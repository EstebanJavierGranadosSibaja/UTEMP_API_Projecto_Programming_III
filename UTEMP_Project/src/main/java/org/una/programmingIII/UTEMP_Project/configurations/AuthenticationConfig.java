package org.una.programmingIII.UTEMP_Project.configurations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.una.programmingIII.UTEMP_Project.services.PasswordEncryptionServices.PasswordEncryptionService;
import org.una.programmingIII.UTEMP_Project.services.PasswordEncryptionServices.PasswordEncryptionServiceImplementation;

@Configuration
public class AuthenticationConfig {

    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthenticationConfig(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public PasswordEncryptionService passwordEncryptionService() {
        return new PasswordEncryptionServiceImplementation(passwordEncoder);
    }
}
