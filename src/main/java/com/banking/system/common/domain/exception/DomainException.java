package com.banking.system.common.domain.exception;

/**
 * Base exception for all domain-level exceptions.
 * All custom exceptions in the domain layer should extend this class.
 */
public abstract class DomainException extends RuntimeException {

    private final String errorCode;

    protected DomainException(String message) {
        super(message);
        this.errorCode = null;
    }

    protected DomainException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    protected DomainException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = null;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
