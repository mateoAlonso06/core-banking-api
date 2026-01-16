package com.banking.system.customer.infraestructure.adapter.out.persistence.repository;

import com.banking.system.customer.domain.model.Customer;
import com.banking.system.customer.domain.port.out.CustomerRepositoryPort;
import com.banking.system.customer.infraestructure.adapter.out.mapper.CustomerJpaEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CustomerRepositoryAdapter implements CustomerRepositoryPort {
    private final SpringDataCustomerRepository springDataCustomerRepository;

    @Override
    public List<Customer> findAll(int page, int size) {
        var pageable = PageRequest.of(page, size);
        return springDataCustomerRepository.findAll(pageable)
                .map(CustomerJpaEntityMapper::toDomainEntity)
                .toList();
    }

    @Override
    public Optional<Customer> findById(UUID id) {
        var entity = springDataCustomerRepository.findById(id);
        return entity.map(CustomerJpaEntityMapper::toDomainEntity);
    }

    @Override
    public Customer save(Customer customer) {
        var entityToSave = CustomerJpaEntityMapper.toJpaEntity(customer);
        var savedEntity = springDataCustomerRepository.save(entityToSave);

        return CustomerJpaEntityMapper.toDomainEntity(savedEntity);
    }

    @Override
    public void delete(UUID id) {
        springDataCustomerRepository.deleteById(id);
    }

    @Override
    public boolean existsByDocumentNumber(String documentNumber) {
        return springDataCustomerRepository.existsByDocumentNumber(documentNumber);
    }

    @Override
    public boolean existsById(UUID id) {
        return springDataCustomerRepository.existsById(id);
    }

    @Override
    public boolean existsByUserId(UUID id) {
        return springDataCustomerRepository.existsByUserId(id);
    }

    @Override
    public Optional<Customer> findByUserId(UUID id) {
        var entity = springDataCustomerRepository.findByUserId(id);
        return entity.map(CustomerJpaEntityMapper::toDomainEntity);
    }
}
