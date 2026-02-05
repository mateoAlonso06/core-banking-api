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

            Bucket tokenBucket = rateLimitingService.resolveBucket(clientIp);

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

    private String getClientIp(HttpServletRequest request) {
        // Check for X-Forwarded-For header in case of proxies/load balancers
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty()) {
            return request.getRemoteAddr();
        }

        return xfHeader.split(",")[0].trim();
    }
}
