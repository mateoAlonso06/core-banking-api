package com.banking.system.common.domain.exception;

/**
 * Base exception for infrastructure/technical failures.
 * Use for external service failures, generation failures, etc.
 * Maps to HTTP 500 Internal Server Error.
 */
public abstract class InfrastructureException extends DomainException {

    private static final String DEFAULT_ERROR_CODE = "INTERNAL_ERROR";

    protected InfrastructureException(String message) {
        super(message, DEFAULT_ERROR_CODE);
    }

    protected InfrastructureException(String message, Throwable cause) {
        super(message, cause);
    }
}
