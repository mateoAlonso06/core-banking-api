package com.banking.system.notification.application.service;

import com.banking.system.notification.application.usecase.SendEmailUseCase;
import com.banking.system.notification.domain.exception.EmailRateLimitExceededException;
import com.banking.system.notification.domain.model.EmailNotification;
import com.banking.system.notification.domain.port.out.EmailSenderPort;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.function.Supplier;

@Service
@Slf4j
public class AccountEmailService implements SendEmailUseCase {
    private final EmailSenderPort emailSenderPort;
    private final ProxyManager<String> proxyManager;

    public AccountEmailService(EmailSenderPort emailSenderPort,
                               @Nullable ProxyManager<String> proxyManager) {
        this.emailSenderPort = emailSenderPort;
        this.proxyManager = proxyManager;
    }

    @Override
    @CircuitBreaker(name = "emailService", fallbackMethod = "fallbackSendEmail")
    @Retry(name = "emailService")
    public void sendEmail(EmailNotification emailNotification) {
        if (proxyManager != null) {
            String rateLimitKey = "email:" + emailNotification.to();
            Bucket bucket = resolveBucket(rateLimitKey, 5, Duration.ofHours(1));

            if (!bucket.tryConsume(1)) {
                log.warn("Rate limit exceeded for email: {}", emailNotification.to());
                throw new EmailRateLimitExceededException(
                        "Too many emails sent. Please try again later."
                );
            }
        }
        emailSenderPort.sendEmail(emailNotification);
    }

    public void fallbackSendEmail(EmailNotification emailNotification, Exception e) {
        log.warn("Email delivery failed for {}. Reason: {}. Email will not be sent.",
                emailNotification.to(),
                e.getMessage());
    }

    /**
     * Creates or retrieves a rate limit bucket for the given key.
     * Uses distributed storage (Redis) to track limits across app instances.
     *
     * @param key      Unique identifier for the bucket (e.g., "email:user@example.com")
     * @param capacity Max number of tokens (emails allowed in the time window)
     * @param refill   Time window for refilling tokens
     * @return Bucket instance for rate limiting
     */
    private Bucket resolveBucket(String key, long capacity, Duration refill) {
        Supplier<BucketConfiguration> configSupplier = () -> {
            Bandwidth limit = Bandwidth.builder()
                    .capacity(capacity)
                    .refillIntervally(capacity, refill)
                    .build();

            return BucketConfiguration.builder()
                    .addLimit(limit)
                    .build();
        };

        return proxyManager.builder().build(key, configSupplier);
    }
}
