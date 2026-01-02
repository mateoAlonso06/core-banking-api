package com.banking.system.auth.domain.port.out;

public interface PasswordHasher {
    String hash(String rawPassword);

    boolean verify(String rawPassword, String hashedPassword);
}
