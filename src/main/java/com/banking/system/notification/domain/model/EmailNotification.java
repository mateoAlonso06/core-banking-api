package com.banking.system.notification.domain.model;

import java.util.Map;

public record EmailNotification(
        String to,
        String subject,
        String templateName,
        Map<String, Object> templateVariables,
        NotificationType type
) {
}

