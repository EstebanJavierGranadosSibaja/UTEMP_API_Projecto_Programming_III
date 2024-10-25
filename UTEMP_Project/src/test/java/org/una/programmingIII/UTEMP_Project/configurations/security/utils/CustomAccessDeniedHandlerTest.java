package org.una.programmingIII.UTEMP_Project.configurations.security.utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.io.IOException;

import static org.mockito.Mockito.*;

class CustomAccessDeniedHandlerTest {

    private CustomAccessDeniedHandler accessDeniedHandler;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private AccessDeniedException accessDeniedException;

    @BeforeEach
    void setUp() {
        accessDeniedHandler = new CustomAccessDeniedHandler();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        accessDeniedException = mock(AccessDeniedException.class);
    }

    @Test
    void handle_ShouldSendForbiddenError() throws IOException {
        when(accessDeniedException.getMessage()).thenReturn("Test access denied error");

        accessDeniedHandler.handle(request, response, accessDeniedException);

        verify(response).sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied: Test access denied error");
    }
}

