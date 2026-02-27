package com.banking.system.auth.domain.model;

import lombok.Getter;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a long-lived refresh token issued at login.
 * <p>
 * While access tokens (JWT) are stateless and short-lived, refresh tokens are
 * stored in the database so they can be explicitly revoked on logout, enabling
 * true server-side session termination.
 * </p>
 *
 * <p><b>Rotation:</b> Each time a refresh token is used to obtain a new access
 * token, the old refresh token is revoked and a new one is issued. This limits
 * the damage of a stolen refresh token to a single use window.</p>
 */
@Getter
public class RefreshToken {

    private static final int EXPIRATION_DAYS = 7;

    private final UUID id;
    private final UUID userId;
    private final String token;
    private final Instant expiresAt;
    private boolean revoked;

    private RefreshToken(UUID id, UUID userId, String token, Instant expiresAt, boolean revoked) {
        Objects.requireNonNull(userId, "userId cannot be null");
        Objects.requireNonNull(token, "token cannot be null");
        Objects.requireNonNull(expiresAt, "expiresAt cannot be null");

        this.id = id;
        this.userId = userId;
        this.token = token;
        this.expiresAt = expiresAt;
        this.revoked = revoked;
    }

    public static RefreshToken createNew(UUID userId) {
        return new RefreshToken(
                null,
                userId,
                UUID.randomUUID().toString(),
                Instant.now().plusSeconds(EXPIRATION_DAYS * 24L * 60 * 60),
                false
        );
    }

    public static RefreshToken reconstitute(UUID id, UUID userId, String token, Instant expiresAt, boolean revoked) {
        return new RefreshToken(id, userId, token, expiresAt, revoked);
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public void revoke() {
        this.revoked = true;
    }
}