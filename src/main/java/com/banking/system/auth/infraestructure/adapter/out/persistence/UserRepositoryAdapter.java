package com.banking.system.auth.infraestructure.adapter.out.persistence;

import com.banking.system.auth.domain.port.out.UserRepositoryPort;
import com.banking.system.auth.domain.model.User;
import com.banking.system.auth.infraestructure.adapter.out.persistence.entity.UserJpaEntity;
import com.banking.system.auth.infraestructure.adapter.out.mapper.UserMapper;
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
    private final UserMapper userMapper;

    @Override
    public List<User> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        return springDataUserRepository.findAll(pageable)
                .map(userMapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsByEmail(String email) {
        return springDataUserRepository.existsByEmail(email);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return springDataUserRepository.findById(id)
                .map(userMapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        Optional<UserJpaEntity> entity = springDataUserRepository.findByEmail(email);

        return entity.map(userMapper::toDomain);
    }

    @Override
    public User save(User user) {
        UserJpaEntity userJpaEntity = userMapper.toJpaEntity(user);
        UserJpaEntity savedEntity = springDataUserRepository.save(userJpaEntity);

        return userMapper.toDomain(savedEntity);
    }

    @Override
    public void update(User user) {
        UserJpaEntity entity = userMapper.toJpaEntity(user);

        springDataUserRepository.save(entity);
    }

    @Override
    public void delete(UUID id) {
        springDataUserRepository.deleteById(id);
    }
}
