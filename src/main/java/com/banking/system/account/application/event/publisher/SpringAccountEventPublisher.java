package com.banking.system.account.application.event.publisher;

import com.banking.system.account.application.event.AccountCreatedEvent;
import com.banking.system.account.domain.model.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SpringAccountEventPublisher implements AccountEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void publishAccountCreated(AccountCreatedEvent event) {
        applicationEventPublisher.publishEvent(event);
    }
}
