package com.banking.system.customer.application.service;

import com.banking.system.common.domain.PageRequest;
import com.banking.system.common.domain.dto.PagedResult;
import com.banking.system.common.domain.Address;
import com.banking.system.common.domain.PersonName;
import com.banking.system.common.domain.Phone;
import com.banking.system.customer.application.dto.command.CreateCustomerCommand;
import com.banking.system.customer.application.dto.command.UpdateCustomerCommand;
import com.banking.system.customer.application.dto.result.CustomerResult;
import com.banking.system.customer.application.mapper.CustomerMapper;
import com.banking.system.customer.application.usecase.*;
import com.banking.system.customer.domain.exception.CustomerAlreadyExistsException;
import com.banking.system.customer.domain.exception.CustomerNotFoundException;
import com.banking.system.customer.domain.exception.DocumenterNumberAlreadyInUseException;
import com.banking.system.customer.domain.exception.InvalidAgeException;
import com.banking.system.customer.domain.port.out.CustomerRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class CustomerService implements
        CreateCustomerUseCase,
        GetCustomerUseCase,
        GetAllCustomerUseCase,
        DeleteCustomerUseCase,
        ApproveKycUseCase,
        RejectKycUseCase,
        UpdateCustomerUseCase {

    private final CustomerRepositoryPort customerRepository;

    @Override
    @Transactional
    // TODO: safe delete operation
    public void deleteCustomerById(UUID customerId) {
        customerRepository.findById(customerId)
                .orElseThrow(() -> {
                            log.warn("Customer with id: {} not found", customerId);
                            return new CustomerNotFoundException("Customer not found with id: " + customerId);
                        }
                );
        customerRepository.delete(customerId);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResult getCustomerById(UUID id) {
        var customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with id: " + id));

        return CustomerResult.fromDomain(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResult getCustomerByUserId(UUID userId) {
        var customer = customerRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found for user: " + userId));

        return CustomerResult.fromDomain(customer);
    }

    @Override
    @Transactional
    public void createCustomer(CreateCustomerCommand command) {
        log.info("Creating customer with userId: {}", command.userId());
        // Ensures idempotency based on userId
        if (customerRepository.existsByUserId(command.userId())) {
            log.warn("Customer already exists for userId: {}", command.userId());
            throw new CustomerAlreadyExistsException("Customer already exists for user: " + command.userId());
        }

        if (customerRepository.existsByDocumentNumber(command.documentNumber())) {
            throw new DocumenterNumberAlreadyInUseException("Document number already in use");
        }

        isUnderAge(command.birthDate());

        var customer = CustomerMapper.toDomain(command);
        var customerSaved = customerRepository.save(customer);

        log.info("Customer created with id: {}", customerSaved.getId());
    }

    @Override
    @Transactional
    public void approveKyc(UUID customerId) {
        log.info("Approving KYC for customerId: {}", customerId);
        var customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with id: " + customerId));

        customer.approveKyc();
        customerRepository.save(customer);
        log.info("KYC approved for customerId: {}", customerId);
    }

    @Override
    @Transactional
    public void rejectKyc(UUID id) {
        log.info("Rejecting KYC for customerId: {}", id);
        var customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with id: " + id));

        customer.rejectKyc();
        customerRepository.save(customer);
        log.info("KYC rejected for customerId: {}", id);
    }

    @Override
    @Transactional
    public CustomerResult updateCustomer(UpdateCustomerCommand command, UUID userId) {
        log.info("Updating customer for userId: {}", userId);
        var customer = customerRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found for user: " + userId));

        if (command.firstName() != null) {
            String firstName = command.firstName();
            if (command.lastName() != null) {
                String lastName = command.lastName();
                customer.updatePersonName(new PersonName(firstName, lastName));
            } else {
                customer.updatePersonName(new PersonName(firstName, customer.getPersonName().lastName()));
            }
        }

        if (command.lastName() != null) {
            String lastName = command.lastName();
            customer.updatePersonName(new PersonName(customer.getPersonName().firstName(), lastName));
        }

        if (command.phone() != null) {
            customer.updatePhone(new Phone(command.phone()));
        }
        if (command.address() != null) {
            String newAddress = command.address();
            if (command.city() != null) {
                String city = command.city();
                customer.updateAddress(new Address(newAddress, city, customer.getAddress().country()));
            } else {
                customer.updateAddress(new Address(newAddress, customer.getAddress().city(), customer.getAddress().country()));
            }
        }
        if (command.city() != null) {
            String city = command.city();
            customer.updateAddress(new Address(customer.getAddress().address(), city, customer.getAddress().country()));
        }

        var saved = customerRepository.save(customer);
        log.info("Customer updated for userId: {}", userId);
        return CustomerResult.fromDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResult<CustomerResult> getAllCustomers(PageRequest pageRequest) {
        var customers = customerRepository.findAll(pageRequest);

        return PagedResult.mapContent(customers, CustomerResult::fromDomain);
    }

    private void isUnderAge(LocalDate birthDate) {
        LocalDate today = LocalDate.now();
        LocalDate adultDate = today.minusYears(18);
        if (birthDate.isAfter(adultDate)) {
            throw new InvalidAgeException("Customer must be at least 18 years old");
        }
    }
}
