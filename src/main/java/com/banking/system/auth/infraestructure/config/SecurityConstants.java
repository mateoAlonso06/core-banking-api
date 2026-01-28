package com.banking.system.auth.infraestructure.config;

public class SecurityConstants {
    public static final String[] PUBLIC_URLS = {
            "/api/v1/auth/**",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/actuator/**"
    };
}
