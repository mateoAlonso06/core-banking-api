package com.banking.system.notification.application.service;

import com.banking.system.notification.application.usecase.SendEmailUseCase;
import com.banking.system.notification.domain.model.EmailNotification;
import com.banking.system.notification.domain.port.out.EmailSenderPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountEmailService implements SendEmailUseCase {
    private final EmailSenderPort emailSenderPort;

    @Override
    public void sendEmail(EmailNotification emailNotification) {
        emailSenderPort.sendEmail(emailNotification);
    }
}
