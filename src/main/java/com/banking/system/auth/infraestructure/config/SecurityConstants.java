package com.banking.system.auth.infraestructure.config;

public class SecurityConstants {
    public static final String[] PUBLIC_URLS = {
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/verify-email",
            "/api/v1/auth/resend-verification",
            "/api/v1/auth/2fa/verify",
            "/swagger-ui/**",
            "/v3/api-docs/**"
    };
}
