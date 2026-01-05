package com.banking.system.auth.application.event.publisher;

import com.banking.system.auth.application.dto.RegisterCommand;
import com.banking.system.auth.domain.model.User;

public interface UserEventPublisher {

    void publishUserRegisteredEvent(User user, RegisterCommand command);
}
