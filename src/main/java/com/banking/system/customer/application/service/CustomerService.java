package com.banking.system.customer.application.service;

import com.banking.system.customer.application.dto.command.CreateCustomerCommand;
import com.banking.system.customer.application.dto.command.UpdateCustommerCommand;
import com.banking.system.customer.application.dto.result.CustomerResult;
import com.banking.system.customer.application.mapper.CustomerMapper;
import com.banking.system.customer.application.usecase.CreateCustomerUseCase;
import com.banking.system.customer.application.usecase.DeleteCustomerUseCase;
import com.banking.system.customer.application.usecase.GetCustomerUseCase;
import com.banking.system.customer.application.usecase.UpdateCustomerUseCase;
import com.banking.system.customer.domain.exception.CustomerAlreadyExistsException;
import com.banking.system.customer.domain.exception.CustomerNotFoundException;
import com.banking.system.customer.domain.model.Customer;
import com.banking.system.customer.domain.port.out.CustomerRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerService implements CreateCustomerUseCase, GetCustomerUseCase, DeleteCustomerUseCase, UpdateCustomerUseCase {

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
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with id: " + id));

        return CustomerMapper.toResult(customer);
    }

    @Override
    @Transactional
    public List<CustomerResult> getAll(int page, int size) {
        if (page < 0 || size <= 0) {
            throw new IllegalArgumentException("Page must be non-negative and size must be positive");
        }

        List<Customer> customers = customerRepository.findAll(page, size);

        return customers.stream()
                .map(CustomerMapper::toResult)
                .toList();
    }

    @Override
    @Transactional
    public void updateCustomer(UUID id, UpdateCustommerCommand command) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with id: " + id));

        Customer updatedCustomer = new Customer(
                customer.getId(),
                customer.getUserId(),
                command.firstName(),
                command.lastName(),
                command.documentType(),
                command.documentNumber(),
                command.birthDate(),
                command.phone(),
                command.address(),
                command.city(),
                command.country(),
                customer.getCustomerSince(),
                customer.getKycStatus(),
                customer.getRiskLevel()
        );

        customerRepository.save(updatedCustomer);
    }

    @Override
    @Transactional
    public CustomerResult createCustomer(CreateCustomerCommand command) {
        // Ensures idempotency based on userId
        if (customerRepository.existsByUserId(command.userId())) {
            return customerRepository.findByUserId(command.userId())
                    .map(CustomerMapper::toResult)
                    .orElseThrow(); // nunca debería pasar
        }

        if (customerRepository.existsByDocumentNumber(command.documentNumber())) {
            throw new CustomerAlreadyExistsException("Customer already exists with document number: " + command.documentNumber());
        }

        Customer customer = CustomerMapper.toDomain(command);
        Customer customerSaved = customerRepository.save(customer);

        return CustomerMapper.toResult(customerSaved);
    }
}
