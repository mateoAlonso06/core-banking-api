package com.banking.system.common.domain.exception;

/**
 * Base exception for all "already exists" scenarios.
 * Extend this for entity-specific duplicate exceptions.
 * Maps to HTTP 409 Conflict.
 */
public abstract class ResourceAlreadyExistsException extends DomainException {

    protected ResourceAlreadyExistsException(String message) {
        super(message);
    }
}
