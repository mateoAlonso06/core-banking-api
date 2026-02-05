package com.banking.system.auth.domain.exception;

import com.banking.system.common.domain.exception.AuthenticationException;

public class LoginAuthenticationAccessException extends AuthenticationException {
    public LoginAuthenticationAccessException(String message) {
        super(message);
    }
}
