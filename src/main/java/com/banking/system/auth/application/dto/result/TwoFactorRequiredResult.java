package com.banking.system.auth.application.dto.result;

public record TwoFactorRequiredResult(
        String sessionToken,
        String maskedEmail,
        int expirySeconds
) {
}