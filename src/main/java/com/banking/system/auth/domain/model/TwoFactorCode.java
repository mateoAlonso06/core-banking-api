package com.banking.system.auth.domain.model;

import lombok.Getter;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Getter
public class TwoFactorCode {

    private static final int EXPIRATION_MINUTES = 5;
    private static final int MAX_ATTEMPTS = 3;
    private static final int CODE_LENGTH = 6;

    private final UUID id;
    private final UUID userId;
    private final String code;
    private final String sessionToken;
    private final LocalDateTime expiresAt;
    private boolean used;
    private int attempts;

    private TwoFactorCode(UUID id, UUID userId, String code, String sessionToken,
                          LocalDateTime expiresAt, boolean used, int attempts) {
        Objects.requireNonNull(userId, "userId cannot be null");
        Objects.requireNonNull(code, "code cannot be null");
        Objects.requireNonNull(sessionToken, "sessionToken cannot be null");
        Objects.requireNonNull(expiresAt, "expiresAt cannot be null");

        this.id = id;
        this.userId = userId;
        this.code = code;
        this.sessionToken = sessionToken;
        this.expiresAt = expiresAt;
        this.used = used;
        this.attempts = attempts;
    }

    public static TwoFactorCode createNew(UUID userId) {
        return new TwoFactorCode(
                null,
                userId,
                generateCode(),
                UUID.randomUUID().toString(),
                LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES),
                false,
                0
        );
    }

    public static TwoFactorCode reconstitute(UUID id, UUID userId, String code, String sessionToken,
                                              LocalDateTime expiresAt, boolean used, int attempts) {
        return new TwoFactorCode(id, userId, code, sessionToken, expiresAt, used, attempts);
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean hasExceededMaxAttempts() {
        return attempts >= MAX_ATTEMPTS;
    }

    public boolean isValid() {
        return !isExpired() && !used && !hasExceededMaxAttempts();
    }

    public void incrementAttempts() {
        this.attempts++;
    }

    public void markUsed() {
        if (this.used) {
            throw new IllegalStateException("Two-factor code has already been used");
        }
        if (isExpired()) {
            throw new IllegalStateException("Two-factor code has expired");
        }
        if (hasExceededMaxAttempts()) {
            throw new IllegalStateException("Maximum verification attempts exceeded");
        }
        this.used = true;
    }

    public boolean verifyCode(String inputCode) {
        if (!isValid()) {
            return false;
        }
        // Constant-time comparison to prevent timing attacks
        return constantTimeEquals(this.code, inputCode);
    }

    public int getExpirySeconds() {
        return EXPIRATION_MINUTES * 60;
    }

    private static String generateCode() {
        SecureRandom random = new SecureRandom();
        StringBuilder code = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }

    private static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) {
            return false;
        }
        if (a.length() != b.length()) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }
}
