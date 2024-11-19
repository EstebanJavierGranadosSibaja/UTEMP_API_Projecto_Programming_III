package org.una.programmingIII.UTEMP_Project.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.slf4j.Logger;

import static org.mockito.Mockito.*;

class EmailNotificationObserverTest {

    @InjectMocks
    private EmailNotificationObserver emailNotificationObserver;

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private MimeMessage mimeMessage;

    @Spy
    private Logger logger = org.slf4j.LoggerFactory.getLogger(EmailNotificationObserver.class);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testUpdate_WhenValidEmail_ShouldSendEmail() throws MessagingException {
        // Arrange
        String eventType = "USER_ENROLLED_QUE_TAL_BRO";
        String message = "Your enrollment is successful!";
        String mail = "estebangranados147@gmail.com";

        MimeMessageHelper helperMock = mock(MimeMessageHelper.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailNotificationObserver.update(eventType, message, mail);

        verify(mailSender, times(1)).send(mimeMessage);
        verify(mimeMessage, times(1)).setSubject(anyString());
        verify(mimeMessage, times(1)).setText(anyString());
    }

    @Test
    void testSendEmail_WhenInvalidEmail_ShouldNotSendEmail() {
        // Arrange
        String invalidEmail = "invalid-email";
        String subject = "Test Subject";
        String body = "Test Body";

        // Act
        emailNotificationObserver.sendEmail(invalidEmail, subject, body);

        // Assert
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void testCreateHtmlMessage() {
        // Arrange
        String eventType = "USER_ENROLLED";
        String message = "Your enrollment is successful!";

        // Act
        String htmlMessage = emailNotificationObserver.createHtmlMessage(eventType, message);

        // Assert
        assert(htmlMessage.contains(eventType));
        assert(htmlMessage.contains(message));
    }
}
