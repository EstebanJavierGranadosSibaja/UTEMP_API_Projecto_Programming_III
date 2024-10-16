package org.una.programmingIII.UTEMP_Project.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.una.programmingIII.UTEMP_Project.dtos.ErrorDTOs.ErrorDTO;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorDTO> handleResourceNotFoundException(ResourceNotFoundException e) {
        logger.error("Resource not found: {}", e.getMessage());
        ErrorDTO error = new ErrorDTO(e.getMessage(), HttpStatus.NOT_FOUND.value(), "Resource Not Found", e.getLocalizedMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<ErrorDTO> handleResourceAlreadyExistsException(ResourceAlreadyExistsException e) {
        logger.error("Resource already exists: {}", e.getMessage());
        ErrorDTO error = new ErrorDTO(e.getMessage(), HttpStatus.CONFLICT.value(), "Resource Already Exists", e.getLocalizedMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(InvalidDataException.class)
    public ResponseEntity<ErrorDTO> handleInvalidDataException(InvalidDataException e) {
        logger.error("Invalid data error: {}", e.getMessage());
        ErrorDTO error = new ErrorDTO(e.getMessage(), HttpStatus.BAD_REQUEST.value(), "Invalid Data", e.getLocalizedMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(RelationConflictException.class)
    public ResponseEntity<ErrorDTO> handleRelationConflictException(RelationConflictException e) {
        logger.error("Relation conflict: {}", e.getMessage());
        ErrorDTO error = new ErrorDTO(e.getMessage(), HttpStatus.FORBIDDEN.value(), "Relation Conflict", e.getLocalizedMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<ErrorDTO> handleTokenExpiredException(TokenExpiredException e) {
        logger.error("Token expired: {}", e.getMessage());
        ErrorDTO error = new ErrorDTO(e.getMessage(), HttpStatus.UNAUTHORIZED.value(), "Token Expired", e.getLocalizedMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ErrorDTO> handleInvalidTokenException(InvalidTokenException e) {
        logger.error("Invalid token: {}", e.getMessage());
        ErrorDTO error = new ErrorDTO(e.getMessage(), HttpStatus.UNAUTHORIZED.value(), "Invalid Token", e.getLocalizedMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDTO> handleGlobalException(Exception e) {
        logger.error("An unexpected error occurred: {}", e.getMessage());
        ErrorDTO error = new ErrorDTO("An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal Server Error", e.getLocalizedMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}