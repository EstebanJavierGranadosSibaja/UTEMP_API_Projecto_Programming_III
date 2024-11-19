package org.una.programmingIII.UTEMP_Project.dtos.TokenDTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenRequestDTO {
    private String refreshToken;
}


