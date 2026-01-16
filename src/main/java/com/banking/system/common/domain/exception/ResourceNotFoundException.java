package com.banking.system.common.domain.exception;

/**
 * Base exception for all "not found" scenarios.
 * Extend this for entity-specific not found exceptions.
 * Maps to HTTP 404.
 */
public abstract class ResourceNotFoundException extends DomainException {

    protected ResourceNotFoundException(String message) {
        super(message);
    }
}
