package com.banking.system.auth.domain.exception;

import com.banking.system.common.domain.exception.AuthenticationException;

public class InvalidCredentialsException extends AuthenticationException {
    public InvalidCredentialsException(String message) {
        super(message);
    }
}
