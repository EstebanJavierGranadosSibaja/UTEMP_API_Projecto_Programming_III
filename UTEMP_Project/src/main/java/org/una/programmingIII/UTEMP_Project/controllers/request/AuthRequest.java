package org.una.programmingIII.UTEMP_Project.controllers.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthRequest {
    private String identificationNumber;
    private String password;
}
