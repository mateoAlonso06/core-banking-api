package com.banking.system.notification.application.service;

import com.banking.system.notification.application.usecase.SendEmailUseCase;
import com.banking.system.notification.domain.model.EmailNotification;
import com.banking.system.notification.domain.port.out.EmailSenderPort;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountEmailService implements SendEmailUseCase {
    private final EmailSenderPort emailSenderPort;

    @Override
    @CircuitBreaker(name = "emailService", fallbackMethod = "fallbackSendEmail")
    @Retry(name = "emailService")
    public void sendEmail(EmailNotification emailNotification) {
        emailSenderPort.sendEmail(emailNotification);
    }

    public void fallbackSendEmail(EmailNotification emailNotification, Exception e) {
        log.warn("Email delivery failed for {}. Reason: {}. Email will not be sent.",
                emailNotification.to(),
                e.getMessage());
    }
}
