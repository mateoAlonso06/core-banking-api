package com.banking.system.integration.config;

import com.banking.system.notification.domain.model.EmailNotification;
import com.banking.system.notification.domain.port.out.EmailSenderPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Slf4j
@Configuration
@ConditionalOnProperty(name = "mail.enabled", havingValue = "false")
public class TestEmailConfig {

    @Bean
    @Primary
    public EmailSenderPort mockEmailSenderPort() {
        return new EmailSenderPort() {
            @Override
            public void sendEmail(EmailNotification notification) {
                log.info("TEST MODE: Email would be sent to {} with subject: {}",
                        notification.to(), notification.subject());
                // No-op: emails are not sent in tests
            }
        };
    }
}