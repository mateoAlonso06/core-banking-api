package com.banking.system.auth.infraestructure.adapter.out.persistence;

import com.banking.system.auth.application.port.out.LoginTrackingPort;
import com.banking.system.auth.infraestructure.adapter.out.persistence.repository.SpringDataUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LoginTrackingAdapter implements LoginTrackingPort {

    private final SpringDataUserRepository userRepository;

    @Override
    public Instant registerLogin(UUID userId) {
        Instant previousLogin = userRepository.findLastLoginAtById(userId);
        userRepository.updateLastLoginAt(userId, Instant.now());
        return previousLogin;
    }
}
