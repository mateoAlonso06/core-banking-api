package com.banking.system.common.domain.exception;

/**
 * Base exception for business rule violations.
 * Use when a domain invariant or business constraint is violated.
 * Maps to HTTP 422 Unprocessable Entity or 400 Bad Request.
 */
public abstract class BusinessRuleException extends DomainException {

    private static final String DEFAULT_ERROR_CODE = "BUSINESS_RULE_VIOLATION";

    protected BusinessRuleException(String message) {
        super(message, DEFAULT_ERROR_CODE);
    }

    protected BusinessRuleException(String message, String errorCode) {
        super(message, errorCode);
    }
}
