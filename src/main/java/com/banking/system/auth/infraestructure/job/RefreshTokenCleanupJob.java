package com.banking.system.auth.infraestructure.job;

import com.banking.system.auth.domain.port.out.RefreshTokenRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled job that purges expired or revoked refresh tokens from the database.
 *
 * <p>Tokens that are expired (past their {@code expires_at}) or explicitly revoked
 * serve no future purpose and only consume storage. This job runs nightly to keep
 * the {@code refresh_tokens} table lean.</p>
 *
 * <p>The cron expression {@code 0 0 3 * * *} fires every day at 03:00 AM server time,
 * a low-traffic window chosen to minimize contention with active user sessions.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RefreshTokenCleanupJob {

    private final RefreshTokenRepositoryPort refreshTokenRepository;

    @Scheduled(cron = "0 0 3 * * *")
    public void purgeExpiredAndRevokedTokens() {
        log.info("Starting refresh token cleanup job");
        int deleted = refreshTokenRepository.deleteExpiredOrRevoked();
        log.info("Refresh token cleanup completed: {} tokens deleted", deleted);
    }
}