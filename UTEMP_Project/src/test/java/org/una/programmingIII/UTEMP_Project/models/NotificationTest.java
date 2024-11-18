package org.una.programmingIII.UTEMP_Project.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationTest {

    private Notification notification;

    @BeforeEach
    void setUp() {
        notification = Notification.builder()
                .user(new User())
                .message("This is a test notification.")
                .status(NotificationStatus.UNREAD)
                .build();
    }

    @Test
    void testNotificationInitialization() {
        assertThat(notification).isNotNull();
        assertThat(notification.getId()).isNull(); // ID debe ser null antes de persistir
        assertThat(notification.getUser()).isNotNull();
        assertThat(notification.getMessage()).isEqualTo("This is a test notification.");
        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.UNREAD);
    }

    @Test
    void testMessageConstraints() {
        notification.setMessage("Short message");
        assertThat(notification.getMessage()).isEqualTo("Short message");

        notification.setMessage("A".repeat(500)); // Mensaje límite permitido
        assertThat(notification.getMessage()).hasSize(500);

        // Validación de mensaje vacío o null (campo obligatorio)
        notification.setMessage("");
        assertThat(notification.getMessage()).isBlank();

        notification.setMessage(null);
        assertThat(notification.getMessage()).isNull();
    }

    @Test
    void testStatusDefault() {
        Notification newNotification = new Notification();
        assertThat(newNotification.getStatus()).isNull(); // El valor predeterminado depende de la persistencia.
    }

    @Test
    void testOnCreate() {
        notification.onCreate();
        assertThat(notification.getCreatedAt()).isNotNull();
        assertThat(notification.getLastUpdate()).isNotNull();
        assertThat(notification.getCreatedAt()).isEqualTo(notification.getLastUpdate());
    }

    @Test
    void testOnUpdate() {
        notification.onCreate();
        LocalDateTime initialLastUpdate = notification.getLastUpdate();

        // Simular actualización
        notification.onUpdate();
        assertThat(notification.getLastUpdate()).isAfter(initialLastUpdate);
        assertThat(notification.getCreatedAt()).isNotNull(); // `createdAt` no debe cambiar
    }

    @Test
    void testSetAndGetStatus() {
        notification.setStatus(NotificationStatus.READ);
        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.READ);

        notification.setStatus(NotificationStatus.UNREAD);
        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.UNREAD);
    }

    @Test
    void testSetAndGetUser() {
        User user = new User();
        notification.setUser(user);
        assertThat(notification.getUser()).isEqualTo(user);
    }

    @Test
    void testIdSetterAndGetter() {
        Long mockId = 1L;
        notification.setId(mockId);
        assertThat(notification.getId()).isEqualTo(mockId);
    }
}
