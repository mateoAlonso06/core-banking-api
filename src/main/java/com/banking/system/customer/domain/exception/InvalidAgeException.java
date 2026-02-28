package com.banking.system.customer.domain.exception;

import com.banking.system.common.domain.exception.BusinessRuleException;

public class InvalidAgeException extends BusinessRuleException {
    public InvalidAgeException(String message) {
        super(message, "INVALID_AGE");
    }
}
