package com.banking.system.common.domain.exception;

/**
 * Base exception for authentication failures.
 * Use for invalid credentials, expired tokens, etc.
 * Maps to HTTP 401 Unauthorized.
 */
public abstract class AuthenticationException extends DomainException {

    private static final String DEFAULT_ERROR_CODE = "AUTHENTICATION_FAILED";

    protected AuthenticationException(String message) {
        super(message, DEFAULT_ERROR_CODE);
    }

    protected AuthenticationException(String message, String errorCode) {
        super(message, errorCode);
    }
}
