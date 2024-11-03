package org.una.programmingIII.UTEMP_Project.configs;

import jakarta.mail.MessagingException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailConfig {

    private static final Logger logger = LoggerFactory.getLogger(MailConfig.class);

    private final String host = "smtp.gmail.com";

    private final int port = 587;

    private final String username = "utempjen@gmail.com";

    private final String password = "thvv gzop hjna apba";

    @NotNull
    public static JavaMailSender getJavaMailSender(JavaMailSenderImpl mailSender, Logger logger) {
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        props.put("mail.debug", "true");

        try {
            mailSender.testConnection();
            logger.info("Mail sender configured successfully.");
        } catch (MessagingException e) {
            logger.error("Error testing mail connection: {}", e.getMessage());
            throw new RuntimeException("Mail configuration failed.", e);
        }

        return mailSender;
    }

    @Bean
    public JavaMailSender mailSender() {
        validateMailProperties();

        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPort(port);
        mailSender.setUsername(username);
        mailSender.setPassword(password);

        return getJavaMailSender(mailSender, logger);
    }

    private void validateMailProperties() {
        if (host == null || host.isEmpty()) {
            throw new IllegalArgumentException("Mail host cannot be null or empty.");
        }
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("Mail username cannot be null or empty.");
        }
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Mail password cannot be null or empty.");
        }
        if (port <= 0) {
            throw new IllegalArgumentException("Mail port must be a positive number.");
        }
    }
}