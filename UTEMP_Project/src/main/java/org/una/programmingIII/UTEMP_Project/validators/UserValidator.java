package org.una.programmingIII.UTEMP_Project.validators;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.springframework.stereotype.Component;
import org.una.programmingIII.UTEMP_Project.dtos.UserDTO;
import org.una.programmingIII.UTEMP_Project.exceptions.InvalidDataException;

@Component
public class UserValidator {

    private static final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private static final Validator validator = factory.getValidator();

    public void validate(UserDTO userDTO) {
        var violations = validator.validate(userDTO);
        if (!violations.isEmpty()) {
            StringBuilder errorMessage = new StringBuilder("Invalid user data:");
            violations.forEach(violation -> errorMessage.append(" ").append(violation.getMessage()));
            throw new InvalidDataException(errorMessage.toString());
        }
    }
}
