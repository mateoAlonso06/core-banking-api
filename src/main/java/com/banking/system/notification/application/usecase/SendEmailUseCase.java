package com.banking.system.notification.application.usecase;

import com.banking.system.notification.domain.model.EmailNotification;

public interface SendEmailUseCase {
    void sendEmail(EmailNotification emailNotification);
}
