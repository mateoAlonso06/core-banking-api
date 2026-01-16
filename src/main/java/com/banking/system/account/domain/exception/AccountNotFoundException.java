package com.banking.system.account.domain.exception;

import com.banking.system.common.domain.exception.ResourceNotFoundException;

public class AccountNotFoundException extends ResourceNotFoundException {
    public AccountNotFoundException(String message) {
        super(message);
    }
}
