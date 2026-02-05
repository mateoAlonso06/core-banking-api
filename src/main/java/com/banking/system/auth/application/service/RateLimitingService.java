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
    private static final int REQUEST_PER_MINUTE = 10;

    // Storage for buckets (IP address -> Bucket)
    private final ProxyManager<String> proxyManager;

    public Bucket resolveBucket(String key) {
        Supplier<BucketConfiguration> configurationSupplier = this::getConfig;

        return proxyManager.builder()
                .build(key, configurationSupplier);
    }

    /**
     * Act as factory that produces BucketConfiguration objects
     * for newly created bucket.
     */
    private BucketConfiguration getConfig() {
        var limit = Bandwidth.builder()
                .capacity(REQUEST_PER_MINUTE)
                .refillIntervally(REQUEST_PER_MINUTE, Duration.ofMinutes(1))
                .build();

        return BucketConfiguration.builder()
                .addLimit(limit)
                .build();
    }
}
