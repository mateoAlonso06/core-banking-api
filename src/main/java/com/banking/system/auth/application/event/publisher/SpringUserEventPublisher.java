package com.banking.system.auth.application.event.publisher;

import com.banking.system.auth.application.dto.command.RegisterCommand;
import com.banking.system.auth.application.event.UserRegisteredEvent;
import com.banking.system.auth.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SpringUserEventPublisher implements UserEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void publishUserRegisteredEvent(User user, RegisterCommand command) {
        applicationEventPublisher.publishEvent(
                new UserRegisteredEvent(
                        user.getId(),
                        command.firstName(),
                        command.lastName(),
                        command.documentType(),
                        command.documentNumber(),
                        command.birthDate(),
                        command.phone(),
                        command.address(),
                        command.city(),
                        command.country()
                )
        );
    }
}
