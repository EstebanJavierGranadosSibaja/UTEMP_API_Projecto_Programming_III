package org.una.programmingIII.UTEMP_Project.configurations;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class MailConfig {

    private static final Logger logger = LoggerFactory.getLogger(MailConfig.class);

    @Value("${spring.mail.host}")
    private String host;

    @Value("${spring.mail.port}")
    private int port;

    @Value("${spring.mail.username}")
    private String username;

    @Value("${spring.mail.password}")
    private String password;

    @Bean
    public JavaMailSender mailSender() {
        validateMailProperties();

        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPort(port);
        mailSender.setUsername(username);
        mailSender.setPassword(password);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.smtp.ssl.trust", host);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.ssl.enable", "true");
        props.put("mail.debug", "true");
        props.put("mail.smtp.connectiontimeout", "5000");
        props.put("mail.smtp.timeout", "5000");
        props.put("mail.smtp.writetimeout", "5000");

        try {
            mailSender.testConnection();
            logger.info("Mail sender configured successfully.");
        } catch (Exception e) {
            logger.error("Error configuring mail sender: {}", e.getMessage());
        }

        return mailSender;
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