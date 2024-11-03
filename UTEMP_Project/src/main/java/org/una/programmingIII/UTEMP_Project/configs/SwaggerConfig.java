package org.una.programmingIII.UTEMP_Project.configs;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    private static final String UTEMP_TITLE = "University Task and Evaluation Management Platform (UTEMP) API";
    private static final String UTEMP_VERSION = "BETA 1.0";

    private static final String UTEMP_DESCRIPTION_TITLE = "The University Task and Evaluation Management Platform (UTEMP) API streamlines the management and review of university assignments and evaluations.";
    private static final String UTEMP_DESCRIPTION_FEATURES =
            "Key features include:\n" +
                    " - **University Management**: Facilitates registration and organization of institutional data.\n" +
                    " - **Faculty and Department Oversight**: Empowers administrators to manage academic divisions.\n" +
                    " - **Course Management**: Enables efficient creation and modification of courses.\n" +
                    " - **User Management**: Supports diverse user roles with granular permissions.\n" +
                    " - **Assignment and Submission Management**: Simplifies tracking and feedback on assignments.\n" +
                    " - **Advanced Automated Review**: Utilizes AI and Grok technology for evaluations.\n" +
                    " - **Secure Credential Verification**: Employs JWT for secure data protection.\n" +
                    " - **Real-time Notifications**: Offers alerts on deadlines and submissions.\n" +
                    " - **Comprehensive Analytics**: Generates reports on user activity for data-driven decisions.\n" +
                    "Overall, the UTEMP API revolutionizes academic workflows by providing a user-friendly and secure system to enhance collaboration and educational outcomes.";

    private static final String CONTACT_NAME = "Support Loans-App";
    private static final String CONTACT_EMAIL = "utempjen@gmail.com";
    private static final String CONTACT_URL = "https://loansapp.com/soporte";

    private static final String LICENSE_NAME = "Apache 2.0";
    private static final String LICENSE_URL = "http://springdoc.org";

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";
    private static final String SECURITY_SCHEME_TYPE = "HTTP";
    private static final String SECURITY_SCHEME_SCHEME = "bearer";
    private static final String SECURITY_SCHEME_BEARER_FORMAT = "JWT";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(createApiInfo())
                .addSecurityItem(createSecurityRequirement())
                .components(createComponents());
    }

    private Info createApiInfo() {
        return new Info()
                .title(UTEMP_TITLE)
                .version(UTEMP_VERSION)
                .description(UTEMP_DESCRIPTION_TITLE + "\n\n" + UTEMP_DESCRIPTION_FEATURES)
                .contact(createContact())
                .license(createLicense());
    }

    private Contact createContact() {
        return new Contact()
                .name(CONTACT_NAME)
                .email(CONTACT_EMAIL)
                .url(CONTACT_URL);
    }

    private License createLicense() {
        return new License()
                .name(LICENSE_NAME)
                .url(LICENSE_URL);
    }

    private SecurityRequirement createSecurityRequirement() {
        return new SecurityRequirement().addList(SECURITY_SCHEME_NAME);
    }

    private Components createComponents() {
        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.valueOf(SECURITY_SCHEME_TYPE))
                .scheme(SECURITY_SCHEME_SCHEME)
                .bearerFormat(SECURITY_SCHEME_BEARER_FORMAT);

        return new Components().addSecuritySchemes(SECURITY_SCHEME_NAME, securityScheme);
    }
}