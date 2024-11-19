package org.una.programmingIII.UTEMP_Project.controllers.responses;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ApiResponseTest {

    @Test
    void testDefaultConstructor() {
        ApiResponse<Object> response = new ApiResponse<>();

        assertNull(response.getData(), "El campo 'data' debe ser nulo por defecto");
        assertEquals(0, response.getStatusCode(), "El campo 'statusCode' debe ser 0 por defecto");
        assertEquals("", response.getMessage(), "El campo 'message' debe ser una cadena vacía por defecto");
    }

    @Test
    void testConstructorWithData() {
        String testData = "Test data";
        ApiResponse<String> response = new ApiResponse<>(testData);

        assertEquals(testData, response.getData(), "El campo 'data' debe contener los datos proporcionados");
        assertEquals(200, response.getStatusCode(), "El campo 'statusCode' debe ser 200 por defecto");
        assertEquals("", response.getMessage(), "El campo 'message' debe ser una cadena vacía por defecto");
    }

    @Test
    void testConstructorWithStatusCodeAndMessage() {
        int statusCode = 404;
        String errorMessage = "Not Found";
        ApiResponse<String> response = new ApiResponse<>(statusCode, errorMessage);

        assertNull(response.getData(), "El campo 'data' debe ser nulo");
        assertEquals(statusCode, response.getStatusCode(), "El campo 'statusCode' debe ser el proporcionado");
        assertEquals(errorMessage, response.getMessage(), "El campo 'message' debe ser el proporcionado");
    }

    @Test
    void testFromException() {
        int statusCode = 500;
        String errorMessage = "Internal Server Error";
        ApiResponse<String> response = ApiResponse.fromException(statusCode, errorMessage);

        assertNull(response.getData(), "El campo 'data' debe ser nulo");
        assertEquals(statusCode, response.getStatusCode(), "El campo 'statusCode' debe ser el proporcionado");
        assertEquals(errorMessage, response.getMessage(), "El campo 'message' debe ser el proporcionado");
    }

    @Test
    void testToString() {
        String testData = "Test data";
        ApiResponse<String> response = new ApiResponse<>(testData);

        String expectedString = "ApiResponse(data=Test data, statusCode=200, message=)";
        assertEquals(expectedString, response.toString(), "El método toString debe devolver la representación correcta del objeto");
    }
}
