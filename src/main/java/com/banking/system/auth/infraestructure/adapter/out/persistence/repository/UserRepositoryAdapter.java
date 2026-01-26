package com.banking.system.auth.infraestructure.adapter.out.persistence.repository;

import com.banking.system.auth.domain.port.out.UserRepositoryPort;
import com.banking.system.auth.domain.model.User;
import com.banking.system.auth.infraestructure.adapter.out.mapper.UserJpaMapper;
import com.banking.system.auth.infraestructure.adapter.out.persistence.entity.RoleJpaEntity;
import com.banking.system.auth.infraestructure.adapter.out.persistence.entity.UserJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepositoryPort {

    private final SpringDataUserRepository springDataUserRepository;
    private final SpringDataRoleRepository springDataRoleRepository;

    @Override
    public List<User> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        return springDataUserRepository.findAll(pageable)
                .map(UserJpaMapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsByEmail(String email) {
        return springDataUserRepository.existsByEmail(email);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return springDataUserRepository.findById(id)
                .map(UserJpaMapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        Optional<UserJpaEntity> entity = springDataUserRepository.findByEmail(email);

        return entity.map(UserJpaMapper::toDomain);
    }

    @Override
    public User save(User user) {
        RoleJpaEntity roleEntity = springDataRoleRepository.findByName(user.getRole().getName())
                .orElseThrow(() -> new IllegalStateException(
                        "Role not found in database: " + user.getRole().getName()));

        UserJpaEntity userJpaEntity = UserJpaMapper.toJpaEntity(user, roleEntity);
        UserJpaEntity savedEntity = springDataUserRepository.save(userJpaEntity);

        return UserJpaMapper.toDomain(savedEntity);
    }

    @Override
    public void delete(UUID id) {
        springDataUserRepository.deleteById(id);
    }
}
