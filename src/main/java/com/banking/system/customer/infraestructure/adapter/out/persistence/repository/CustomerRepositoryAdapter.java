package com.banking.system.customer.infraestructure.adapter.out.persistence.repository;

import com.banking.system.common.domain.PageRequest;
import com.banking.system.common.domain.dto.PagedResult;
import com.banking.system.customer.domain.model.Customer;
import com.banking.system.customer.domain.port.out.CustomerRepositoryPort;
import com.banking.system.customer.infraestructure.adapter.out.mapper.CustomerJpaEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CustomerRepositoryAdapter implements CustomerRepositoryPort {
    private final SpringDataCustomerRepository springDataCustomerRepository;

    @Override
    public PagedResult<Customer> findAll(PageRequest pageRequest) {
        var pageable = org.springframework.data.domain.PageRequest.of(
                pageRequest.page(), pageRequest.size());
        var page = springDataCustomerRepository.findAll(pageable);

        var customers = page.getContent().stream()
                .map(CustomerJpaEntityMapper::toDomainEntity)
                .toList();

        return PagedResult.of(
                customers,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements()
        );
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
