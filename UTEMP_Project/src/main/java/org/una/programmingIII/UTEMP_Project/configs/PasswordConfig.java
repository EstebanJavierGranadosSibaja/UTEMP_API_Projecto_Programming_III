package org.una.programmingIII.UTEMP_Project.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.una.programmingIII.UTEMP_Project.services.passwordEncryption.PasswordEncryptionService;
import org.una.programmingIII.UTEMP_Project.services.passwordEncryption.PasswordEncryptionServiceImplementation;

@Configuration
public class PasswordConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public PasswordEncryptionService passwordEncryptionService(PasswordEncoder passwordEncoder) {
        return new PasswordEncryptionServiceImplementation(passwordEncoder);
    }
}
