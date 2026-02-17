package com.banking.system.auth.infraestructure.adapter.out.security;

import com.banking.system.auth.domain.port.out.TokenGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtTokenGenerator implements TokenGenerator {
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public String generateToken(UUID userId, String email, String role) {
        return jwtTokenProvider.generateToken(userId.toString(), email, role);
    }
}