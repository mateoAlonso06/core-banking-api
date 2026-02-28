package com.banking.system.common.domain.exception;

/**
 * Base exception for resource ownership violations.
 * Use when an authenticated user attempts to access a resource they do not own.
 * Maps to HTTP 403 Forbidden.
 */
public abstract class AccessDeniedException extends DomainException {

    private static final String DEFAULT_ERROR_CODE = "ACCESS_DENIED";

    protected AccessDeniedException(String message) {
        super(message, DEFAULT_ERROR_CODE);
    }

    protected AccessDeniedException(String message, String errorCode) {
        super(message, errorCode);
    }
}
