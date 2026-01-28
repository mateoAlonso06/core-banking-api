package com.banking.system.auth.domain.exception;

import com.banking.system.common.domain.exception.AccessDeniedException;

public class UserNotVerifiedException extends AccessDeniedException {
    public UserNotVerifiedException(String message) {
        super(message);
    }
}