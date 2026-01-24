package com.banking.system.customer.application.service;

import com.banking.system.common.domain.exception.BusinessRuleException;
import com.banking.system.customer.application.dto.command.CreateCustomerCommand;
import com.banking.system.customer.application.dto.result.CustomerResult;
import com.banking.system.customer.application.mapper.CustomerMapper;
import com.banking.system.customer.application.usecase.*;
import com.banking.system.customer.domain.exception.CustomerAlreadyExistsException;
import com.banking.system.customer.domain.exception.CustomerNotFoundException;
import com.banking.system.customer.domain.exception.KycIsAlreadyProccedException;
import com.banking.system.customer.domain.model.RiskLevel;
import com.banking.system.customer.domain.port.out.CustomerRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerService implements
        CreateCustomerUseCase,
        GetCustomerUseCase,
        DeleteCustomerUseCase,
        ApproveKycUseCase,
        RejectKycUseCase {

    private final CustomerRepositoryPort customerRepository;

    @Override
    @Transactional
    public void deleteCustomerById(UUID id) {
        if (!customerRepository.existsById(id)) {
            throw new CustomerNotFoundException("Customer not found with id: " + id);
        }

        customerRepository.delete(id);
    }

    @Override
    @Transactional
    public CustomerResult getCustomerById(UUID id) {
        var customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with id: " + id));

        return CustomerResult.fromDomain(customer);
    }

    @Override
    @Transactional
    public List<CustomerResult> getAll(int page, int size) {
        if (page < 0 || size <= 0) {
            throw new IllegalArgumentException("Page must be non-negative and size must be positive");
        }

        var customers = customerRepository.findAll(page, size);

        return customers.stream()
                .map(CustomerResult::fromDomain)
                .toList();
    }

    @Override
    @Transactional
    public CustomerResult createCustomer(CreateCustomerCommand command) {
        // Ensures idempotency based on userId
        if (customerRepository.existsByUserId(command.userId())) {
            return customerRepository.findByUserId(command.userId())
                    .map(CustomerResult::fromDomain)
                    .orElseThrow(); // nunca debería pasar
        }

        if (customerRepository.existsByDocumentNumber(command.documentNumber())) {
            throw new CustomerAlreadyExistsException("Customer already exists with document number: " + command.documentNumber());
        }

        var customer = CustomerMapper.toDomain(command);
        var customerSaved = customerRepository.save(customer);

        return CustomerResult.fromDomain(customerSaved);
    }

    @Override
    public void approveKyc(UUID customerId) {
        var customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with id: " + customerId));

        customer.approveKyc();
        customerRepository.save(customer);
    }

    @Override
    public void rejectKyc(UUID id) {
        var customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with id: " + id));

        customer.rejectKyc();
        customerRepository.save(customer);
    }
}
