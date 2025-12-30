package com.banking.system.auth.domain.port.out;

import com.banking.system.auth.domain.model.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    List<User> getAll(int page, int size);

    boolean existsByEmail(String email);

    Optional<User> getById(UUID id);

    User save(User user);

    void update(User user);

    void delete(UUID id);
}
