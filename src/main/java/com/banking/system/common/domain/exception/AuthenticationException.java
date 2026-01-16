package com.banking.system.common.domain.exception;

/**
 * Base exception for authentication failures.
 * Use for invalid credentials, expired tokens, etc.
 * Maps to HTTP 401 Unauthorized.
 */
public abstract class AuthenticationException extends DomainException {

    protected AuthenticationException(String message) {
        super(message);
    }
}
