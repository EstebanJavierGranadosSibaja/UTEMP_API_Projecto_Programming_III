package org.una.programmingIII.UTEMP_Project.controllers.auth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthRequest {
    private String identificationNumber;
    private String password;
}
