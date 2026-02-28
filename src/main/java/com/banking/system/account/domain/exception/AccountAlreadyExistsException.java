package com.banking.system.account.domain.exception;

import com.banking.system.common.domain.exception.ResourceAlreadyExistsException;

public class AccountAlreadyExistsException extends ResourceAlreadyExistsException {
    public AccountAlreadyExistsException(String message) {
        super(message, "ACCOUNT_ALREADY_EXISTS");
    }
}
