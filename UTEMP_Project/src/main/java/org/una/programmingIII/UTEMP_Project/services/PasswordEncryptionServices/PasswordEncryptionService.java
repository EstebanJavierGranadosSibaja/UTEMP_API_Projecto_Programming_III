package org.una.programmingIII.UTEMP_Project.services.PasswordEncryptionServices;

public interface PasswordEncryptionService {
   String encryptPassword(String password);
   boolean matches(String rawPassword, String encryptedPassword);
}