package com.banking.system.auth.application.dto.result;

import com.banking.system.auth.domain.model.RoleName;

import java.util.UUID;

public record LoginResult(
        UUID id,
        String email,
        RoleName role,
        String token,
        boolean requiresTwoFactor,
        TwoFactorRequiredResult twoFactorData
) {
    public static LoginResult withToken(UUID id, String email, RoleName role, String token) {
        return new LoginResult(id, email, role, token, false, null);
    }

    public static LoginResult withTwoFactorRequired(UUID id, String email, TwoFactorRequiredResult twoFactorData) {
        return new LoginResult(id, email, null, null, true, twoFactorData);
    }
}
