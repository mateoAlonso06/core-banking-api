package com.banking.system.auth.domain.exception;

import com.banking.system.common.domain.exception.AuthenticationException;

public class InvalidCredentalsException extends AuthenticationException {
    public InvalidCredentalsException(String message) {
        super(message);
    }
}
