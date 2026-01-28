package com.banking.system.auth.application.event;

import java.util.UUID;

public record EmailVerificationRequestedEvent(
        UUID userId,
        String email,
        String token,
        String firstName
) {
}