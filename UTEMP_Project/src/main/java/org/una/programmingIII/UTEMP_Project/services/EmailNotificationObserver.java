package org.una.programmingIII.UTEMP_Project.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.una.programmingIII.UTEMP_Project.observers.Observer;

import java.util.Map;

@Service
public class EmailNotificationObserver implements Observer {

    private static final Logger logger = LoggerFactory.getLogger(EmailNotificationObserver.class);
    private static final Map<String, String> SUBJECTS = Map.of(
            "USER_ENROLLED", "Enrollment Completed",
            "PROFESSOR_NOTIFICATION", "New Student Enrolled",
            "USER_SUBMISSION", "New Assignment Submission",
            "SUBMISSION_GRADED", "Submission Reviewed",
            "NEW_ASSIGNMENT", "You Have a New Assignment"
    );

    private final String host = "smtp.gmail.com";

    private final int port = 587;

    private final String username = "utempjen@gmail.com";

    private final String password = "toly ryay jebb gnxz";

    private final JavaMailSender mailSender;

    public EmailNotificationObserver() {
        this.mailSender = createJavaMailSender();
    }

    private JavaMailSender createJavaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPort(port);
        mailSender.setUsername(username);
        mailSender.setPassword(password);

        mailSender.getJavaMailProperties().put("mail.smtp.auth", true);
        mailSender.getJavaMailProperties().put("mail.smtp.starttls.enable", true);
        return mailSender;
    }

    @Async("taskExecutor")
    @Override
    public void update(String eventType, String message, String mail) {
        logger.debug("Sending email for event: {} with message: {} to {}", eventType, message, mail);
        String subject = SUBJECTS.getOrDefault(eventType, "Notification");
        String htmlMessage = createHtmlMessage(eventType, message);
        sendEmail(mail, subject, htmlMessage);
        logger.debug("Email sent on thread: {}", Thread.currentThread().getName());
    }

    public String createHtmlMessage(String eventType, String message) {
        return "<html>"
                + "<head>"
                + "<style>"
                + "body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background: linear-gradient(135deg, #f4f1f8, #d1c4e9); padding: 0; margin: 0; color: #4b3f6d; text-align: center; display: flex; justify-content: center; align-items: center; height: 100vh; }" // Centrado del cuerpo de la página
                + "h1 { color: #7e4b8b; font-size: 48px; font-weight: 900; margin-bottom: 20px; letter-spacing: 3px; text-shadow: 3px 3px 10px rgba(0, 0, 0, 0.25); font-family: 'Segoe UI', sans-serif; animation: fadeIn 2s ease-out; }" // Título centrado, con sombra y animación
                + "@keyframes fadeIn { 0% { opacity: 0; } 100% { opacity: 1; } }" // Animación de entrada para el título
                + "p { font-size: 20px; line-height: 1.8; color: #4b3f6d; font-weight: 400; padding: 0 20px; margin-bottom: 30px; transition: transform 0.3s ease-in-out; }" // Párrafo con efecto de hover
                + "p:hover { transform: translateY(-5px); }" // Efecto de hover para el texto
                + ".footer { font-size: 14px; color: #aaa; text-align: center; margin-top: 40px; padding: 25px 0; font-style: italic; background-color: #f1eff9; border-radius: 15px; box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1); animation: fadeIn 3s ease-out; }" // Pie de página con sombra y animación
                + "hr { border: none; border-top: 6px solid #743885; margin: 40px auto; width: 60%; animation: pulse 1.5s infinite; }" // Línea decorativa con animación de pulso
                + "@keyframes pulse { 0% { border-color: #743885; } 50% { border-color: #7e4b8b; } 100% { border-color: #743885; } }" // Animación de pulso para la línea
                + ".content { background-color: #ffffff; padding: 50px; border-radius: 20px; box-shadow: 0 12px 30px rgba(0, 0, 0, 0.1); max-width: 750px; width: 100%; text-align: center; transition: transform 0.3s ease-in-out, box-shadow 0.3s ease-in-out; position: relative; margin: 0 auto; }" // Caja centrada con sombras y transiciones
                + ".content:hover { transform: scale(1.05); box-shadow: 0 20px 40px rgba(0, 0, 0, 0.2); }" // Efecto de hover para la caja
                + ".content:before { content: ''; position: absolute; top: 0; left: 0; width: 100%; height: 4px; background: linear-gradient(to right, #743885, #7e4b8b); border-radius: 2px; animation: gradientMove 3s ease-in-out infinite; }" // Línea superior animada dentro de la caja
                + "@keyframes gradientMove { 0% { background-position: 0% 0%; } 100% { background-position: 100% 0%; } }" // Movimiento de gradiente para la línea superior
                + ".content h1 { font-size: 36px; color: #7e4b8b; font-weight: 700; text-align: center; font-family: 'Segoe UI', sans-serif; margin-top: 0; }" // Título dentro del contenido
                + ".content p { font-size: 18px; color: #4b3f6d; line-height: 1.7; font-weight: 300; text-align: center; }" // Estilo del párrafo dentro de la caja
                + ".developers { font-size: 16px; font-weight: 300; color: #7e4b8b; margin-top: 30px; font-style: italic; text-align: center; padding-top: 20px; border-top: 2px solid #7e4b8b; opacity: 0.8; transition: opacity 0.3s ease-in-out; }" // Desarrolladores con opacidad más baja para que no sean el centro
                + ".developers a { color: #7e4b8b; text-decoration: none; font-weight: bold; transition: color 0.3s ease-in-out; }" // Estilo para los enlaces de los desarrolladores
                + ".developers a:hover { color: #2e8b57; }" // Hover en los enlaces para resaltar con verde suave
                + "</style>"
                + "</head>"
                + "<body>"
                + "<div class='content'>"
                + "<h1>Welcome to our UTEMP online platform!</h1>"
                + "<p>" + message + "</p>"
                + "<hr>"
                + "<p class='footer'>This email was generated automatically. Please do not reply to this message.</p>"
                + "<div class='developers'>"
                + "<p>Developed by: <a href='https://github.com/EstebanJavierGranadosSibaja' target='_blank'>Esteban Javier Granados Sibaja</a> and <a href='https://github.com/JuanCaUNA' target='_blank'>Juan Carlos Camacho Solano</a></p>"
                + "</div>"
                + "</div>"
                + "</body>"
                + "</html>";
    }


    public void sendEmail(String to, String subject, String body) {
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
            throw new RuntimeException("Failed to send email to " + to, e);
        }
    }

    private boolean isValidEmail(String email) {
        return email != null && email.contains("@") && email.contains(".");
    }
}