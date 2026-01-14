package com.banking.system.account.application.event.publisher;

import com.banking.system.account.application.event.AccountCreatedEvent;

public interface AccountEventPublisher {

    void publishAccountCreated(AccountCreatedEvent event);
}
