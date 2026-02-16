package com.banking.system.auth.infraestructure.adapter.in.rest.dto.request;

import com.banking.system.common.infraestructure.utils.SanitizeHtml;
import jakarta.validation.constraints.NotBlank;

public record VerifyEmailRequest(
        @NotBlank(message = "Token is required")
        @SanitizeHtml
        String token
) {
}
