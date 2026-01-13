package com.banking.system.notification.infraestructure.adapter.out.template;

import com.banking.system.notification.domain.model.NotificationType;
import com.banking.system.notification.domain.port.out.TemplateRenderedPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class ThymeleafTemplateRenderer implements TemplateRenderedPort {
    private final TemplateEngine templateEngine;

    @Override
    public String render(NotificationType type, Map<String, Object> variables) {
        Context context = new Context();
        context.setVariables(variables);

        return templateEngine.process(type.getTemplateName(), context);
    }
}
