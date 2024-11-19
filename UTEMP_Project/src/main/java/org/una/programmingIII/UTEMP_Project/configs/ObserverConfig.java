package org.una.programmingIII.UTEMP_Project.configs;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.una.programmingIII.UTEMP_Project.services.EmailNotificationObserver;
import org.una.programmingIII.UTEMP_Project.services.UserServiceImplementation;

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
