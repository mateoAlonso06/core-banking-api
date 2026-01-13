package com.banking.system.notification.domain.port.out;

import com.banking.system.notification.domain.model.NotificationType;

import java.util.Map;

public interface TemplateRenderedPort {
    String render(NotificationType type, Map<String, Object> variables);
}
