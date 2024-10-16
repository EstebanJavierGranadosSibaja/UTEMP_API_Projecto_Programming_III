package org.una.programmingIII.UTEMP_Project.dtos.ErrorDTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorDTO {
    private String message;
    private int status;
    private String errorType;
    private String details;
}