package com.banking.system.auth.domain.exception;

import com.banking.system.common.domain.exception.DomainException;

public class UserIsLockedException extends DomainException {
    public UserIsLockedException(String message) {
        super(message);
    }
}
