package com.banking.system.common.infraestructure.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Filter that ensures every request has a correlation ID for tracking across logs.
 * The correlation ID can be provided by the client via X-Correlation-ID header,
 * or will be generated automatically.
 * This ID is added to:
 * - MDC (Mapped Diagnostic Context) for automatic inclusion in all logs
 * - Response headers so clients can reference it when reporting issues
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)  // Execute FIRST before any other filter
public class CorrelationIdFilter extends OncePerRequestFilter {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final String CORRELATION_ID_MDC_KEY = "correlationId";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        // Get correlation ID from header or generate a new one
        String correlationId = request.getHeader(CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        // Add to logging context (MDC) - will be included in all logs automatically
        MDC.put(CORRELATION_ID_MDC_KEY, correlationId);

        // Add to response headers so client can reference it
        response.setHeader(CORRELATION_ID_HEADER, correlationId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            // Clean up MDC after request completes
            MDC.remove(CORRELATION_ID_MDC_KEY);
        }
    }
}