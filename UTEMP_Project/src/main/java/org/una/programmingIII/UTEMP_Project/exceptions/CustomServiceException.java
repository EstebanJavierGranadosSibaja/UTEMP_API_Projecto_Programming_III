package org.una.programmingIII.UTEMP_Project.exceptions;

public class CustomServiceException extends RuntimeException {
    public CustomServiceException(String message) {
        super(message);
    }

    public CustomServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
