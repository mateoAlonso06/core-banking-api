package com.banking.system.auth.domain.port.out;

import com.banking.system.auth.domain.model.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepositoryPort {
    List<User> findAll(int page, int size);

    boolean existsByEmail(String email);

    Optional<User> findById(UUID id);

    Optional<User> findByEmail(String email);

    User save(User user);

    void delete(UUID id);
}
