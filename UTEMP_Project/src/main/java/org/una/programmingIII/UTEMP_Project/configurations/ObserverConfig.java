package org.una.programmingIII.UTEMP_Project.configurations;

import org.una.programmingIII.UTEMP_Project.services.EmailNotificationObserver;
import org.una.programmingIII.UTEMP_Project.services.UserServices.UserServiceImplementation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Configuration
public class ObserverConfig {

    @Autowired
    private UserServiceImplementation userService;

    @Autowired
    private EmailNotificationObserver emailNotificationObserver;

    @PostConstruct
    public void initObservers() {
        // Agregar el observador de notificaciones por correo al servicio de usuario
        userService.addObserver(emailNotificationObserver);
    }
}
