package com.banking.system.notification.domain.port.out;

import com.banking.system.notification.domain.model.EmailNotification;

public interface EmailSenderPort {
    void sendEmail(EmailNotification notification);
}

