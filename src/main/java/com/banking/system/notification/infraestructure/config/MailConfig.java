package com.banking.system.notification.infraestructure.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

// JavaMailSender is auto-configured by Spring Boot from spring.mail.* properties in application.yml
@Configuration
@ConditionalOnProperty(name = "mail.enabled", havingValue = "true", matchIfMissing = true)
public class MailConfig {

    @Bean
    public ResourceLoader resourceLoader() {
        return new DefaultResourceLoader();
    }
}
