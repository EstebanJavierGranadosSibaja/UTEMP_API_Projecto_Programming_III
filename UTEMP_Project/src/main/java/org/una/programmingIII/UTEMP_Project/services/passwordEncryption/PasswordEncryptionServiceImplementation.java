package org.una.programmingIII.UTEMP_Project.services.passwordEncryption;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PasswordEncryptionServiceImplementation implements PasswordEncryptionService {

    private final PasswordEncoder passwordEncoder;

    @Autowired
    public PasswordEncryptionServiceImplementation(
            PasswordEncoder passwordEncoder) {

        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public String encryptPassword(String password) {
        return passwordEncoder.encode(password);
    }

    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}

