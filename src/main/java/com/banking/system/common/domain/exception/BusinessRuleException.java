package com.banking.system.common.domain.exception;

/**
 * Base exception for business rule violations.
 * Use when a domain invariant or business constraint is violated.
 * Maps to HTTP 422 Unprocessable Entity or 400 Bad Request.
 */
public abstract class BusinessRuleException extends DomainException {

    protected BusinessRuleException(String message) {
        super(message);
    }
}
