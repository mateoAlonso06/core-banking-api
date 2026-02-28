package com.banking.system.common.domain.exception;

/**
 * Base exception for all "not found" scenarios.
 * Extend this for entity-specific not found exceptions.
 * Maps to HTTP 404.
 */
public abstract class ResourceNotFoundException extends DomainException {

    private static final String DEFAULT_ERROR_CODE = "RESOURCE_NOT_FOUND";

    protected ResourceNotFoundException(String message) {
        super(message, DEFAULT_ERROR_CODE);
    }

    protected ResourceNotFoundException(String message, String errorCode) {
        super(message, errorCode);
    }
}
