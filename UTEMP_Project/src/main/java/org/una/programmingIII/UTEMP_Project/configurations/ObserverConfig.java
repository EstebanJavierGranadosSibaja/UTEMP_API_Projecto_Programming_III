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
        if (emailNotificationObserver != null) {
            userService.addObserver(emailNotificationObserver);
        } else {
            throw new IllegalStateException("EmailNotificationObserver is not initialized.");
        }
    }
}
