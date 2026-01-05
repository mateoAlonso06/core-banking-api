package com.banking.system.customer.application.service;

import com.banking.system.customer.application.dto.result.CustomerResult;
import com.banking.system.customer.domain.model.Customer;
import com.banking.system.customer.domain.port.out.CustomerRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {
    @Mock
    private CustomerRepositoryPort customerRepository;
    @InjectMocks
    private CustomerService customerService;

    static List<Customer> customerProvider() {
        Customer customer1 = new Customer(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "John",
                "Doe",
                "DNI",
                "12345678",
                LocalDate.of(1990, 1, 1),
                "+1234567890",
                "123 Main St",
                "New York",
                "USA",
                LocalDate.now(),
                Customer.KycStatus.PENDING,
                Customer.RiskLevel.LOW
        );

        Customer customer2 = new Customer(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Jane",
                "Smith",
                "PASSPORT",
                "A9876543",
                LocalDate.of(1985, 5, 15),
                "+1987654321",
                "456 Elm St",
                "Los Angeles",
                "USA",
                LocalDate.now(),
                Customer.KycStatus.APPROVED,
                Customer.RiskLevel.MEDIUM
        );

        Customer customer3 = new Customer(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Carlos",
                "Gomez",
                "DNI",
                "87654321",
                LocalDate.of(1992, 8, 20),
                "+5491122334455",
                "789 Oak St",
                "Buenos Aires",
                "Argentina",
                LocalDate.now(),
                Customer.KycStatus.REJECTED,
                Customer.RiskLevel.HIGH
        );

        return List.of(customer1, customer2, customer3);
    }

    // methodName_whenCondition_shouldExpectedResult

    @Test
    public void deleteCustomerById_whenCustomerExists_shouldDeleteCustomer() {
        UUID id = UUID.randomUUID();
        when(customerRepository.existsById(id)).thenReturn(true);

        customerService.deleteCustomerById(id);

        verify(customerRepository, times(1)).delete(id);
    }

    @Test
    public void deleteCustomerById_whenCustomerDoesNotExist_shouldThrowException() {
        UUID id = UUID.randomUUID();
        when(customerRepository.existsById(id)).thenReturn(false);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            customerService.deleteCustomerById(id);
        });

        String expectedMessage = "Customer not found with id: " + id;
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
        verify(customerRepository, never()).delete(id);
    }

    @Test
    public void getCustomerById_whenCustomerExists_shouldReturnCustomer() {
        UUID id = UUID.randomUUID();
        Customer customer = customerProvider().getFirst();
        when(customerRepository.findById(id)).thenReturn(Optional.of(customer));

        CustomerResult result = customerService.getCustomerById(id);

        assertNotNull(result);
        assertEquals(customer.getId(), result.id());
        assertEquals(customer.getFirstName(), result.firstName());
        assertEquals(customer.getLastName(), result.lastName());
    }
}