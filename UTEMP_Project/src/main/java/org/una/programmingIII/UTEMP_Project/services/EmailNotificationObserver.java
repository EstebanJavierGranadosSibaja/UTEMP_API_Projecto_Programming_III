package org.una.programmingIII.UTEMP_Project.services;

import org.springframework.scheduling.annotation.Async;
import org.una.programmingIII.UTEMP_Project.observers.Observer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class EmailNotificationObserver implements Observer {

    private final JavaMailSender mailSender;

    private static final Map<String, String> SUBJECTS = Map.of(
            "USER_ENROLLED", "Enrollment Completed",
            "PROFESSOR_NOTIFICATION", "New Student Enrolled",
            "USER_SUBMISSION", "New Assignment Submission",
            "SUBMISSION_GRADED", "Submission Reviewed",
            "NEW_ASSIGNMENT", "You Have a New Assignment"
    );

    @Autowired
    public EmailNotificationObserver(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async("taskExecutor")
    @Override
    public void update(String eventType, String message, String mail) {
        String subject = SUBJECTS.getOrDefault(eventType, "Notification");
        sendEmail(mail, subject, message);
    }

    private void sendEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }
}