package org.una.programmingIII.UTEMP_Project.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.una.programmingIII.UTEMP_Project.responses.ApiResponse;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFoundException(ResourceNotFoundException e) {
        logger.error("Resource not found: {}", e.getMessage());
        ApiResponse<Void> response = new ApiResponse<>(HttpStatus.NOT_FOUND.value(), "Resource not found");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceAlreadyExistsException(ResourceAlreadyExistsException e) {
        logger.error("Resource already exists: {}", e.getMessage());
        ApiResponse<Void> response = new ApiResponse<>(HttpStatus.CONFLICT.value(), "Resource already exists");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(InvalidDataException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidDataException(InvalidDataException e) {
        logger.error("Invalid data error: {}", e.getMessage());
        ApiResponse<Void> response = new ApiResponse<>(HttpStatus.BAD_REQUEST.value(), "Invalid data");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(RelationConflictException.class)
    public ResponseEntity<ApiResponse<Void>> handleRelationConflictException(RelationConflictException e) {
        logger.error("Relation conflict: {}", e.getMessage());
        ApiResponse<Void> response = new ApiResponse<>(HttpStatus.FORBIDDEN.value(), "Relation conflict");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<ApiResponse<Void>> handleTokenExpiredException(TokenExpiredException e) {
        logger.error("Token expired: {}", e.getMessage());
        ApiResponse<Void> response = new ApiResponse<>(HttpStatus.UNAUTHORIZED.value(), "Token expired");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidTokenException(InvalidTokenException e) {
        logger.error("Invalid token: {}", e.getMessage());
        ApiResponse<Void> response = new ApiResponse<>(HttpStatus.UNAUTHORIZED.value(), "Invalid token");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGlobalException(Exception e) {
        logger.error("An unexpected error occurred: {}", e.getMessage());
        ApiResponse<Void> response = new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "An unexpected error occurred");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

//    @ExceptionHandler(ResourceNotFoundException.class)
//    public ResponseEntity<ApiResponse<Void>> handleResourceNotFoundException(ResourceNotFoundException e) {
//        logger.error("Resource not found: {}", e.getMessage());
//        ApiResponse<Void> response = new ApiResponse<>(HttpStatus.NOT_FOUND.value(), "RESOURCE_NOT_FOUND");
//        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
//    }
}
