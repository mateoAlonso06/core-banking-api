package com.banking.system.auth.application.service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "rate-limiting.enabled", havingValue = "true", matchIfMissing = true)
public class RateLimitingService {

    // Rate limit configurations for different endpoint types
    public static final int LOGIN_REQUEST_PER_MINUTE = 5;      // Strict - prevent brute force attacks
    public static final int API_REQUEST_PER_MINUTE = 60;       // Generous - legitimate authenticated users
    public static final int PUBLIC_REQUEST_PER_MINUTE = 10;    // Moderate - unauthenticated endpoints

    private final ProxyManager<String> proxyManager;

    /**
     * Creates a rate limit bucket for login endpoints (strict limit).
     * Use for: /login, /register, password reset endpoints
     *
     * @param key Unique identifier (typically IP address)
     * @return Bucket with 5 requests/minute limit
     */
    public Bucket resolveBucketForLogin(String key) {
        return resolveBucket(key, LOGIN_REQUEST_PER_MINUTE);
    }

    /**
     * Creates a rate limit bucket for authenticated API endpoints (generous limit).
     * Use for: All authenticated endpoints after successful login
     *
     * @param key Unique identifier (typically userId or IP)
     * @return Bucket with 60 requests/minute limit
     */
    public Bucket resolveBucketForAuthenticatedApi(String key) {
        return resolveBucket(key, API_REQUEST_PER_MINUTE);
    }

    /**
     * Creates a rate limit bucket for public endpoints (moderate limit).
     * Use for: Health checks, public documentation, etc.
     *
     * @param key Unique identifier (typically IP address)
     * @return Bucket with 10 requests/minute limit
     */
    public Bucket resolveBucketForPublic(String key) {
        return resolveBucket(key, PUBLIC_REQUEST_PER_MINUTE);
    }

    /**
     * Generic method to create a bucket with custom capacity.
     * Uses token bucket algorithm with 1-minute refill interval.
     * Buckets are stored in Redis for distributed rate limiting.
     *
     * @param key      Unique identifier for the bucket
     * @param capacity Maximum requests allowed per minute
     * @return Bucket instance for rate limiting
     */
    public Bucket resolveBucket(String key, long capacity) {
        Supplier<BucketConfiguration> configurationSupplier = () -> getConfig(capacity);

        return proxyManager.builder()
                .build(key, configurationSupplier);
    }

    /**
     * Factory method that produces BucketConfiguration with specified capacity.
     * Token bucket refills completely every minute.
     *
     * @param capacity Number of tokens (requests) allowed per minute
     * @return BucketConfiguration instance
     */
    private BucketConfiguration getConfig(long capacity) {
        Bandwidth limit = Bandwidth.builder()
                .capacity(capacity)
                .refillIntervally(capacity, Duration.ofMinutes(1))
                .build();

        return BucketConfiguration.builder()
                .addLimit(limit)
                .build();
    }
}
