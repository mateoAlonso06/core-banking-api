package com.banking.system.auth.infraestructure.adapter.out.filter;

import com.banking.system.auth.application.service.RateLimitingService;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "rate-limiting.enabled", havingValue = "true", matchIfMissing = true)
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitingService rateLimitingService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            String clientIp = this.getClientIp(request);
            String requestPath = request.getRequestURI();

            // Select appropriate rate limit based on endpoint type
            Bucket tokenBucket = resolveBucketForEndpoint(clientIp, requestPath);

            var probe = tokenBucket.tryConsumeAndReturnRemaining(1);

            if (probe.isConsumed()) {
                response.addHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
                filterChain.doFilter(request, response);
                return;
            }

            long waitToRefill = (long) Math.ceil(probe.getNanosToWaitForRefill() / 1_000_000_000.0);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.addHeader("X-Rate-Limit-Retry-After-Seconds", String.valueOf(waitToRefill));
            response.addHeader("Retry-After", String.valueOf(waitToRefill));
            response.setContentType("application/json");

            String jsonResponse = """
                    {
                        "status": %s,
                        "error": "Too Many Requests",
                        "message": "You have exhausted your API Request Quota. Please try again in %d seconds."
                    }
                    """.formatted(HttpStatus.TOO_MANY_REQUESTS.value(), waitToRefill);
            response.getWriter().write(jsonResponse);
        } catch (Exception ex) {
            logger.error("Rate limiting failed, allowing request: " + ex.getMessage());
            filterChain.doFilter(request, response);
        }
    }

    /**
     * Resolves the appropriate rate limit bucket based on endpoint type.
     * Authentication endpoints get stricter limits to prevent brute force.
     * Authenticated API endpoints get generous limits for legitimate users.
     * Public endpoints get moderate limits.
     *
     * Each endpoint type uses a separate bucket key to prevent interference.
     *
     * @param clientIp    Client IP address (unique key for rate limiting)
     * @param requestPath Request URI path
     * @return Bucket configured with appropriate rate limit
     */
    private Bucket resolveBucketForEndpoint(String clientIp, String requestPath) {
        // Strict limits for authentication endpoints (prevent brute force)
        if (isAuthenticationEndpoint(requestPath)) {
            return rateLimitingService.resolveBucketForLogin(clientIp + ":auth");
        }

        // Moderate limits for public endpoints
        if (isPublicEndpoint(requestPath)) {
            return rateLimitingService.resolveBucketForPublic(clientIp + ":public");
        }

        // Generous limits for authenticated API endpoints (normal usage)
        return rateLimitingService.resolveBucketForAuthenticatedApi(clientIp + ":api");
    }

    /**
     * Checks if the endpoint is authentication-related (login, register, verification, 2FA).
     * These endpoints need strict rate limiting to prevent brute force attacks.
     *
     * @param path Request URI path
     * @return true if authentication endpoint
     */
    private boolean isAuthenticationEndpoint(String path) {
        return path.startsWith("/api/v1/auth/login") ||
                path.startsWith("/api/v1/auth/register") ||
                path.startsWith("/api/v1/auth/verify-email") ||
                path.startsWith("/api/v1/auth/resend-verification") ||
                path.startsWith("/api/v1/auth/2fa/verify");
    }

    /**
     * Checks if the endpoint is public (health checks, documentation, etc.).
     * These endpoints need moderate rate limiting.
     *
     * @param path Request URI path
     * @return true if public endpoint
     */
    private boolean isPublicEndpoint(String path) {
        return path.startsWith("/actuator/health") ||
                path.startsWith("/actuator/info") ||
                path.startsWith("/api-docs") ||
                path.startsWith("/swagger-ui");
    }

    private String getClientIp(HttpServletRequest request) {
        // Check for X-Forwarded-For header in case of proxies/load balancers
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty()) {
            return request.getRemoteAddr();
        }

        return xfHeader.split(",")[0].trim();
    }
}
