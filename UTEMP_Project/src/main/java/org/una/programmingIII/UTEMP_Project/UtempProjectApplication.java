package org.una.programmingIII.UTEMP_Project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(scanBasePackages = "org.una.programmingIII.UTEMP_Project")
@EnableAsync(proxyTargetClass = true)
public class UtempProjectApplication {

	public static void main(String[] args) {
		SpringApplication.run(UtempProjectApplication.class, args);
	}
}
