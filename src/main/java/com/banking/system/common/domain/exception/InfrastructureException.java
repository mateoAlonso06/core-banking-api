package com.banking.system.common.domain.exception;

/**
 * Base exception for infrastructure/technical failures.
 * Use for external service failures, generation failures, etc.
 * Maps to HTTP 500 Internal Server Error.
 */
public abstract class InfrastructureException extends DomainException {

    protected InfrastructureException(String message) {
        super(message);
    }

    protected InfrastructureException(String message, Throwable cause) {
        super(message, cause);
    }
}
