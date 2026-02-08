package com.banking.system.auth.application.event;

import java.util.UUID;

public record TwoFactorCodeRequestedEvent(
        UUID userId,
        String email,
        String code,
        String firstName
) {
}
