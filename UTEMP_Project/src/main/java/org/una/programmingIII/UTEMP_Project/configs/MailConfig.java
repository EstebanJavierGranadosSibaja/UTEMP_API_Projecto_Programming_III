package org.una.programmingIII.UTEMP_Project.configs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
public class MailConfig {

    private static final Logger logger = LoggerFactory.getLogger(MailConfig.class);

    private final String host = "smtp.gmail.com";
    private final int port = 587;
    private final String username = "utempjen@gmail.com";
    private final String password = "toly ryay jebb gnxz";

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPort(port);
        mailSender.setUsername(username);
        mailSender.setPassword(password);

        mailSender.getJavaMailProperties().put("mail.smtp.auth", true);
        mailSender.getJavaMailProperties().put("mail.smtp.starttls.enable", true);

        return mailSender;
    }
}
