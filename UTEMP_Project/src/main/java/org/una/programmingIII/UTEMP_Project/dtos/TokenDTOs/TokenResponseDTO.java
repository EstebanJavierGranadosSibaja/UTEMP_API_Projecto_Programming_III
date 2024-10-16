package org.una.programmingIII.UTEMP_Project.dtos.TokenDTOs;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenResponseDTO {
        private String accessToken;
        private String refreshToken;
}
