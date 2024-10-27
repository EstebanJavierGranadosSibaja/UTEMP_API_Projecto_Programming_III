package org.una.programmingIII.UTEMP_Project.controllers.responses;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;

@Getter
@Setter
public class TokenResponse {
    String token; //"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    String tokenType; //"Bearer",

    @Value("${jwt.expiration.access}")
    int expiresIn; //3600

    TokenResponse(String token) {
        this.token = token;
        tokenType = "Bearer";
    }
}
