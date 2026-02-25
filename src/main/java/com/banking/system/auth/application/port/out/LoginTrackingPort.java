package com.banking.system.auth.application.port.out;

import java.time.Instant;
import java.util.UUID;

public interface LoginTrackingPort {

    /**
     * Reads the current lastLoginAt, persists Instant.now(), and returns the previous value.
     *
     * @param userId the user ID
     * @return the previous lastLoginAt (may be null for first login)
     */
    Instant registerLogin(UUID userId);
}
