package com.banking.system.auth.infraestructure.config;

import com.banking.system.auth.domain.model.*;
import com.banking.system.auth.domain.port.out.RoleRepositoryPort;
import com.banking.system.auth.domain.port.out.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminUserSeeder implements CommandLineRunner {

    private final UserRepositoryPort userRepository;
    private final RoleRepositoryPort roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.seed.email:}")
    private String adminEmail;

    @Value("${admin.seed.password:}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        if (adminEmail.isBlank() || adminPassword.isBlank()) {
            log.debug("Admin seed skipped: ADMIN_SEED_EMAIL or ADMIN_SEED_PASSWORD not configured");
            return;
        }

        if (userRepository.existsByEmail(adminEmail)) {
            log.debug("Admin seed skipped: user {} already exists", adminEmail);
            return;
        }

        Role adminRole = roleRepository.findByName(RoleName.ADMIN)
                .orElseThrow(() -> new IllegalStateException("ADMIN role not found in database"));

        String hashedPassword = passwordEncoder.encode(adminPassword);

        User admin = User.createNew(
                new Email(adminEmail),
                Password.fromHash(hashedPassword),
                adminRole
        );

        admin.activate();

        userRepository.save(admin);

        log.info("Admin user seeded: {}", adminEmail);
    }
}
