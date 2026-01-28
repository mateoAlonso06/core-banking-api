package com.banking.system.auth.domain.model;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Getter
public class VerificationToken {

    private static final int EXPIRATION_MINUTES = 15;

    private final UUID id;
    private final UUID userId;
    private final String token;
    private final LocalDateTime expiresAt;
    private boolean used;

    private VerificationToken(UUID id, UUID userId, String token, LocalDateTime expiresAt, boolean used) {
        Objects.requireNonNull(userId, "userId cannot be null");
        Objects.requireNonNull(token, "token cannot be null");
        Objects.requireNonNull(expiresAt, "expiresAt cannot be null");

        this.id = id;
        this.userId = userId;
        this.token = token;
        this.expiresAt = expiresAt;
        this.used = used;
    }

    public static VerificationToken createNew(UUID userId) {
        return new VerificationToken(
                null,
                userId,
                UUID.randomUUID().toString(),
                LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES),
                false
        );
    }

    public static VerificationToken reconstitute(UUID id, UUID userId, String token, LocalDateTime expiresAt, boolean used) {
        return new VerificationToken(id, userId, token, expiresAt, used);
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public void markUsed() {
        if (this.used) {
            throw new IllegalStateException("Verification token has already been used");
        }
        if (isExpired()) {
            throw new IllegalStateException("Verification token has expired");
        }
        this.used = true;
    }
}