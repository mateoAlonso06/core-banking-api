package com.banking.system.customer.infraestructure.adapter.out.persistence;

import com.banking.system.customer.domain.model.Customer;
import com.banking.system.customer.domain.port.out.CustomerRepositoryPort;
import com.banking.system.customer.infraestructure.adapter.out.mapper.CustomerEntityMapper;
import com.banking.system.customer.infraestructure.adapter.out.persistence.entity.CustomerJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CustomerRepositoryAdapter implements CustomerRepositoryPort {
    private final SpringDataCustomerRepository springDataCustomerRepository;
    private final CustomerEntityMapper customerEntityMapper;

    @Override
    public List<Customer> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return springDataCustomerRepository.findAll(pageable)
                .map(customerEntityMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Customer> findById(UUID id) {
        Optional<CustomerJpaEntity> entity = springDataCustomerRepository.findById(id);
        return entity.map(customerEntityMapper::toDomain);
    }

    @Override
    public Customer save(Customer customer) {
        CustomerJpaEntity entityToSave = customerEntityMapper.toEntity(customer);
        CustomerJpaEntity savedEntity = springDataCustomerRepository.save(entityToSave);
        return customerEntityMapper.toDomain(savedEntity);
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
}
