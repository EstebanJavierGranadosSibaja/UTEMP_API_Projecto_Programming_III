package org.una.programmingIII.UTEMP_Project.responses;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiResponse<T> {
    private T data;
    private int statusCode;
    private String errorMessage;

    public ApiResponse(T data) {
        this.data = data;
        this.errorMessage = null; // Sin error
        this.statusCode = 200; // Código de estado por defecto
    }

    public ApiResponse(int statusCode, String errorMessage) {
        this.statusCode = statusCode;
        this.errorMessage = errorMessage;
        this.data = null; // Sin datos
    }

    public static <T> ApiResponse<T> fromException(int statusCode, String errorMessage) {
        return new ApiResponse<>(statusCode, errorMessage);
    }
}
