package org.una.programmingIII.UTEMP_Project.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.una.programmingIII.UTEMP_Project.observers.Observer;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.Map;
import java.util.Properties;

import static org.una.programmingIII.UTEMP_Project.configs.MailConfig.getJavaMailSender;

@Service
public class EmailNotificationObserver implements Observer {

    private final JavaMailSender mailSender;
    private static final Logger logger = LoggerFactory.getLogger(EmailNotificationObserver.class);

    private static final Map<String, String> SUBJECTS = Map.of(
            "USER_ENROLLED", "Enrollment Completed",
            "PROFESSOR_NOTIFICATION", "New Student Enrolled",
            "USER_SUBMISSION", "New Assignment Submission",
            "SUBMISSION_GRADED", "Submission Reviewed",
            "NEW_ASSIGNMENT", "You Have a New Assignment"
    );

    public EmailNotificationObserver() {
        this.mailSender = createMailSender();
    }

    private JavaMailSender createMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("smtp.gmail.com");
        mailSender.setPort(587);
        mailSender.setUsername("utempjen@gmail.com");
        mailSender.setPassword("thvv gzop hjna apba");

        return getJavaMailSender(mailSender, logger);
    }

    @Async("taskExecutor")
    @Override
    public void update(String eventType, String message, String mail) {
        String subject = SUBJECTS.getOrDefault(eventType, "Notification");
        String htmlMessage = createHtmlMessage(eventType, message);
        sendEmail(mail, subject, htmlMessage);
    }

    private String createHtmlMessage(String eventType, String message) {
        StringBuilder htmlMessage = new StringBuilder();
        htmlMessage.append("<html>")
                .append("<head>")
                .append("<style>")
                .append("body { font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px; }")
                .append(".container { background-color: #ffffff; border-radius: 8px; padding: 20px; box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1); }")
                .append("h1 { color: #333333; }")
                .append("p { color: #555555; font-size: 16px; }")
                .append(".footer { margin-top: 20px; font-size: 14px; color: #777777; }")
                .append("</style>")
                .append("</head>")
                .append("<body>")
                .append("<div class='container'>")
                .append("<h1>Notification: ").append(eventType).append("</h1>")
                .append("<p>").append(message).append("</p>")
                .append("<p>Thank you for your attention!</p>")
                .append("<div class='footer'>")
                .append("<p>Best Regards,<br>Your University</p>")
                .append("</div>")
                .append("</div>")
                .append("</body>")
                .append("</html>");

        return htmlMessage.toString();
    }

    private void sendEmail(String to, String subject, String body) {
        if (!isValidEmail(to)) {
            logger.error("Invalid email address: {}", to);
            return;
        }

        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);
            mailSender.send(message);
            logger.info("Email sent successfully to {}", to);
        } catch (MessagingException e) {
            logger.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$";
        return email != null && email.matches(emailRegex);
    }
}
