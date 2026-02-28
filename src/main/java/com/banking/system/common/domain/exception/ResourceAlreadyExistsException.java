package com.banking.system.common.domain.exception;

/**
 * Base exception for all "already exists" scenarios.
 * Extend this for entity-specific duplicate exceptions.
 * Maps to HTTP 409 Conflict.
 */
public abstract class ResourceAlreadyExistsException extends DomainException {

    private static final String DEFAULT_ERROR_CODE = "RESOURCE_ALREADY_EXISTS";

    protected ResourceAlreadyExistsException(String message) {
        super(message, DEFAULT_ERROR_CODE);
    }

    protected ResourceAlreadyExistsException(String message, String errorCode) {
        super(message, errorCode);
    }
}
