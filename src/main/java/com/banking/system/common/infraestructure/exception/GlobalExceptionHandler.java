package com.banking.system.common.infraestructure.exception;

import com.banking.system.auth.domain.exception.UserIsLockedException;
import com.banking.system.common.domain.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global exception handler for the banking system.
 *
 * Security considerations:
 * - In production, internal exception messages are NOT exposed to clients
 * - All responses include correlation ID for request tracking
 * - Detailed stack traces are logged server-side but never sent to clients
 * - Generic error messages prevent information disclosure attacks
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    /**
     * Checks if the application is running in production mode.
     * In production, we hide internal error details from clients.
     */
    private boolean isProduction() {
        return "prod".equalsIgnoreCase(activeProfile);
    }

    /**
     * Sanitizes error messages to prevent information disclosure in production.
     *
     * @param message The original error message
     * @param fallbackMessage The generic message to use in production
     * @return The sanitized message
     */
    private String sanitizeMessage(String message, String fallbackMessage) {
        if (isProduction()) {
            return fallbackMessage;
        }
        return message;
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage());
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleConflict(ResourceAlreadyExistsException ex) {
        log.warn("Resource conflict: {}", ex.getMessage());
        return buildResponse(HttpStatus.CONFLICT, "Conflict", ex.getMessage());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthentication(AuthenticationException ex) {
        log.warn("Authentication failed: {}", ex.getMessage());
        return buildResponse(HttpStatus.UNAUTHORIZED, "Unauthorized", ex.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());
        return buildResponse(HttpStatus.FORBIDDEN, "Forbidden", ex.getMessage());
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<Map<String, Object>> handleBusinessRule(BusinessRuleException ex) {
        log.warn("Business rule violation: {}", ex.getMessage());
        return buildResponse(HttpStatus.UNPROCESSABLE_ENTITY, "Unprocessable Entity", ex.getMessage());
    }

    @ExceptionHandler(InfrastructureException.class)
    public ResponseEntity<Map<String, Object>> handleInfrastructure(InfrastructureException ex) {
        log.error("Infrastructure error: {}", ex.getMessage(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
                "An unexpected error occurred. Please try again later.");
    }

    // Catches @Valid validation errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        log.warn("Validation failed: {}", errors);
        return buildResponse(HttpStatus.BAD_REQUEST, "Bad Request", errors);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Invalid argument: {}", ex.getMessage());
        // Sanitize message to prevent exposing internal validation logic
        String message = sanitizeMessage(ex.getMessage(), "Invalid request parameters");
        return buildResponse(HttpStatus.BAD_REQUEST, "Bad Request", message);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalState(IllegalStateException ex) {
        log.warn("Invalid state: {}", ex.getMessage());
        // Sanitize message to prevent exposing internal state information
        String message = sanitizeMessage(ex.getMessage(), "Operation not allowed in current state");
        return buildResponse(HttpStatus.CONFLICT, "Conflict", message);
    }

    /**
     * Catch-all handler for unexpected exceptions.
     *
     * SECURITY CRITICAL: This handler MUST NOT expose internal error details in production.
     * Exposing ex.getMessage() can reveal:
     * - SQL table/column names
     * - Internal file paths
     * - Network topology (IPs, ports)
     * - Technology stack details
     * - Business logic internals
     *
     * The full stack trace is logged server-side for debugging, but clients only
     * receive a generic message in production.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        log.error("Unexpected error", ex);
        // NEVER expose internal exception messages in production
        String message = sanitizeMessage(
            ex.getMessage(),
            "An unexpected error occurred. Please contact support with the correlation ID."
        );
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", message);
    }

    @ExceptionHandler(UserIsLockedException.class)
    public ResponseEntity<Map<String, Object>> handleAccountLocked(UserIsLockedException ex) {
        log.warn("Account locked: {}", ex.getMessage());
        return buildResponse(HttpStatus.LOCKED, "Locked", ex.getMessage());
    }

    /**
     * Builds a standardized error response with correlation ID for tracking.
     * The correlation ID allows users to reference specific errors when contacting support,
     * and allows developers to find the exact request in logs.
     */
    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String error, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", status.value());
        body.put("error", error);
        body.put("message", message);

        // Include correlation ID if available (set by CorrelationIdFilter)
        String correlationId = MDC.get("correlationId");
        if (correlationId != null) {
            body.put("correlationId", correlationId);
        }

        return ResponseEntity.status(status).body(body);
    }
}
