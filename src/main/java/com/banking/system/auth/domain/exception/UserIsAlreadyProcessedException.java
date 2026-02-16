package com.banking.system.auth.domain.exception;

import com.banking.system.common.domain.exception.BusinessRuleException;

public class UserIsAlreadyProcessedException extends BusinessRuleException {
    public UserIsAlreadyProcessedException(String message) {
        super(message);
    }
}
