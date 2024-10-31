package org.una.programmingIII.UTEMP_Project.controllers.responses;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.una.programmingIII.UTEMP_Project.dtos.UserDTO;

@Getter
@Setter
@Builder
public class TokenResponse {
    @NotNull(message = "Password must not be null")
    String token; //"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    @NotNull(message = "Password must not be null")
    String tokenType; //"Bearer",
    @NotNull(message = "Password must not be null")
    UserDTO user;

    @Value("${jwt.expiration.access}")
    int expiresIn; //3600

    TokenResponse(String token, UserDTO user) {
        this.token = token;
        tokenType = "Bearer";
        this.user = user;
    }
}
