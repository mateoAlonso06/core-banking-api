package com.banking.system.common.domain.exception;

/**
 * Base exception for all domain-level exceptions.
 * All custom exceptions in the domain layer should extend this class.
 */
public abstract class DomainException extends RuntimeException {

    protected DomainException(String message) {
        super(message);
    }

    protected DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
