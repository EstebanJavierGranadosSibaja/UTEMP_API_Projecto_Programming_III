package org.una.programmingIII.UTEMP_Project.configs.security.utils;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.AuthenticationException;
import org.una.programmingIII.UTEMP_Project.security.utils.CustomAuthenticationEntryPoint;

import java.io.IOException;

import static org.mockito.Mockito.*;

class CustomAuthenticationEntryPointTest {

    private CustomAuthenticationEntryPoint entryPoint;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private AuthenticationException authException;

    @BeforeEach
    void setUp() {
        entryPoint = new CustomAuthenticationEntryPoint();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        authException = mock(AuthenticationException.class);
    }

    @Test
    void commence_ShouldSendUnauthorizedError() throws IOException, ServletException {
        when(authException.getMessage()).thenReturn("Test authentication error");

        entryPoint.commence(request, response, authException);

        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized: Test authentication error");
    }
}
