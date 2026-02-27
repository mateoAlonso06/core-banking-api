package com.banking.system.auth.domain.exception;

import com.banking.system.common.domain.exception.AuthenticationException;

public class InvalidRefreshTokenException extends AuthenticationException {
    public InvalidRefreshTokenException(String message) {
        super(message);
    }
}
