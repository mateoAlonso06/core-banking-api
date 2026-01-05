package com.banking.system.unit.customer.domain.model;

import com.banking.system.customer.domain.model.Customer;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CustomerTest {

    @Test
    public void createNewCustomer_whenValidData_shouldCreateCustomerWithDefaults() {
        UUID userId = UUID.randomUUID();
        String firstName = "John";
        String lastName = "Doe";
        String documentType = "DNI";
        String documentNumber = "12345678";
        LocalDate birthDate = LocalDate.of(1990, 1, 1);
        String phone = "+1234567890";
        String address = "123 Main St";
        String city = "New York";
        String country = "USA";

        Customer customer = Customer.createNewCustomer(
                userId, firstName, lastName, documentType, documentNumber,
                birthDate, phone, address, city, country
        );

        assertNotNull(customer);
        assertNull(customer.getId());
        assertEquals(userId, customer.getUserId());
        assertEquals(firstName, customer.getFirstName());
        assertEquals(lastName, customer.getLastName());
        assertEquals(documentType, customer.getDocumentType());
        assertEquals(documentNumber, customer.getDocumentNumber());
        assertEquals(birthDate, customer.getBirthDate());
        assertEquals(phone, customer.getPhone());
        assertEquals(address, customer.getAddress());
        assertEquals(city, customer.getCity());
        assertEquals(country, customer.getCountry());
        assertEquals(LocalDate.now(), customer.getCustomerSince());
        assertEquals(Customer.KycStatus.PENDING, customer.getKycStatus());
        assertEquals(Customer.RiskLevel.LOW, customer.getRiskLevel());
    }

    @Test
    public void createNewCustomer_whenUserIdIsNull_shouldThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            Customer.createNewCustomer(
                    null, "John", "Doe", "DNI", "12345678",
                    LocalDate.of(1990, 1, 1), "+1234567890",
                    "123 Main St", "New York", "USA"
            );
        });

        String expectedMessage = "User ID cannot be null";
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    public void createNewCustomer_whenFirstNameIsNull_shouldThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            Customer.createNewCustomer(
                    UUID.randomUUID(), null, "Doe", "DNI", "12345678",
                    LocalDate.of(1990, 1, 1), "+1234567890",
                    "123 Main St", "New York", "USA"
            );
        });

        String expectedMessage = "First name cannot be null or blank";
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    public void createNewCustomer_whenFirstNameIsBlank_shouldThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            Customer.createNewCustomer(
                    UUID.randomUUID(), "   ", "Doe", "DNI", "12345678",
                    LocalDate.of(1990, 1, 1), "+1234567890",
                    "123 Main St", "New York", "USA"
            );
        });

        String expectedMessage = "First name cannot be null or blank";
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    public void createNewCustomer_whenLastNameIsNull_shouldThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            Customer.createNewCustomer(
                    UUID.randomUUID(), "John", null, "DNI", "12345678",
                    LocalDate.of(1990, 1, 1), "+1234567890",
                    "123 Main St", "New York", "USA"
            );
        });

        String expectedMessage = "Last name cannot be null or blank";
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    public void createNewCustomer_whenLastNameIsBlank_shouldThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            Customer.createNewCustomer(
                    UUID.randomUUID(), "John", "  ", "DNI", "12345678",
                    LocalDate.of(1990, 1, 1), "+1234567890",
                    "123 Main St", "New York", "USA"
            );
        });

        String expectedMessage = "Last name cannot be null or blank";
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    public void createNewCustomer_whenDocumentTypeIsNull_shouldThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            Customer.createNewCustomer(
                    UUID.randomUUID(), "John", "Doe", null, "12345678",
                    LocalDate.of(1990, 1, 1), "+1234567890",
                    "123 Main St", "New York", "USA"
            );
        });

        String expectedMessage = "Document type cannot be null or blank";
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    public void createNewCustomer_whenDocumentTypeIsBlank_shouldThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            Customer.createNewCustomer(
                    UUID.randomUUID(), "John", "Doe", "  ", "12345678",
                    LocalDate.of(1990, 1, 1), "+1234567890",
                    "123 Main St", "New York", "USA"
            );
        });

        String expectedMessage = "Document type cannot be null or blank";
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    public void createNewCustomer_whenDocumentNumberIsNull_shouldThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            Customer.createNewCustomer(
                    UUID.randomUUID(), "John", "Doe", "DNI", null,
                    LocalDate.of(1990, 1, 1), "+1234567890",
                    "123 Main St", "New York", "USA"
            );
        });

        String expectedMessage = "Document number cannot be null or blank";
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    public void createNewCustomer_whenDocumentNumberIsBlank_shouldThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            Customer.createNewCustomer(
                    UUID.randomUUID(), "John", "Doe", "DNI", "   ",
                    LocalDate.of(1990, 1, 1), "+1234567890",
                    "123 Main St", "New York", "USA"
            );
        });

        String expectedMessage = "Document number cannot be null or blank";
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    public void createNewCustomer_whenBirthDateIsNull_shouldThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            Customer.createNewCustomer(
                    UUID.randomUUID(), "John", "Doe", "DNI", "12345678",
                    null, "+1234567890",
                    "123 Main St", "New York", "USA"
            );
        });

        String expectedMessage = "Birth date cannot be null or in the future";
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    public void createNewCustomer_whenBirthDateIsInTheFuture_shouldThrowException() {
        LocalDate futureDate = LocalDate.now().plusDays(1);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            Customer.createNewCustomer(
                    UUID.randomUUID(), "John", "Doe", "DNI", "12345678",
                    futureDate, "+1234567890",
                    "123 Main St", "New York", "USA"
            );
        });

        String expectedMessage = "Birth date cannot be null or in the future";
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    public void createNewCustomer_whenPhoneIsNull_shouldThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            Customer.createNewCustomer(
                    UUID.randomUUID(), "John", "Doe", "DNI", "12345678",
                    LocalDate.of(1990, 1, 1), null,
                    "123 Main St", "New York", "USA"
            );
        });

        String expectedMessage = "Phone cannot be null or blank";
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    public void createNewCustomer_whenPhoneIsBlank_shouldThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            Customer.createNewCustomer(
                    UUID.randomUUID(), "John", "Doe", "DNI", "12345678",
                    LocalDate.of(1990, 1, 1), "  ",
                    "123 Main St", "New York", "USA"
            );
        });

        String expectedMessage = "Phone cannot be null or blank";
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    public void createNewCustomer_whenAddressIsNull_shouldThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            Customer.createNewCustomer(
                    UUID.randomUUID(), "John", "Doe", "DNI", "12345678",
                    LocalDate.of(1990, 1, 1), "+1234567890",
                    null, "New York", "USA"
            );
        });

        String expectedMessage = "Address cannot be null or blank";
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    public void createNewCustomer_whenAddressIsBlank_shouldThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            Customer.createNewCustomer(
                    UUID.randomUUID(), "John", "Doe", "DNI", "12345678",
                    LocalDate.of(1990, 1, 1), "+1234567890",
                    "   ", "New York", "USA"
            );
        });

        String expectedMessage = "Address cannot be null or blank";
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    public void createNewCustomer_whenCityIsNull_shouldThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            Customer.createNewCustomer(
                    UUID.randomUUID(), "John", "Doe", "DNI", "12345678",
                    LocalDate.of(1990, 1, 1), "+1234567890",
                    "123 Main St", null, "USA"
            );
        });

        String expectedMessage = "City cannot be null or blank";
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    public void createNewCustomer_whenCityIsBlank_shouldThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            Customer.createNewCustomer(
                    UUID.randomUUID(), "John", "Doe", "DNI", "12345678",
                    LocalDate.of(1990, 1, 1), "+1234567890",
                    "123 Main St", "  ", "USA"
            );
        });

        String expectedMessage = "City cannot be null or blank";
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    public void createNewCustomer_whenCountryIsNull_shouldThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            Customer.createNewCustomer(
                    UUID.randomUUID(), "John", "Doe", "DNI", "12345678",
                    LocalDate.of(1990, 1, 1), "+1234567890",
                    "123 Main St", "New York", null
            );
        });

        String expectedMessage = "Country cannot be null or blank";
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    public void createNewCustomer_whenCountryIsBlank_shouldThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            Customer.createNewCustomer(
                    UUID.randomUUID(), "John", "Doe", "DNI", "12345678",
                    LocalDate.of(1990, 1, 1), "+1234567890",
                    "123 Main St", "New York", "  "
            );
        });

        String expectedMessage = "Country cannot be null or blank";
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    public void approveKyc_whenStatusIsPending_shouldApproveKyc() {
        Customer customer = Customer.createNewCustomer(
                UUID.randomUUID(), "John", "Doe", "DNI", "12345678",
                LocalDate.of(1990, 1, 1), "+1234567890",
                "123 Main St", "New York", "USA"
        );

        customer.approveKyc();

        assertEquals(Customer.KycStatus.APPROVED, customer.getKycStatus());
    }

    @Test
    public void approveKyc_whenStatusIsApproved_shouldThrowException() {
        Customer customer = new Customer(
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
                Customer.KycStatus.APPROVED,
                Customer.RiskLevel.LOW
        );

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            customer.approveKyc();
        });

        String expectedMessage = "KYC can only be approved from PENDING";
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    public void approveKyc_whenStatusIsRejected_shouldThrowException() {
        Customer customer = new Customer(
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
                Customer.KycStatus.REJECTED,
                Customer.RiskLevel.LOW
        );

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            customer.approveKyc();
        });

        String expectedMessage = "KYC can only be approved from PENDING";
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    public void rejectKyc_whenStatusIsPending_shouldRejectKyc() {
        Customer customer = Customer.createNewCustomer(
                UUID.randomUUID(), "John", "Doe", "DNI", "12345678",
                LocalDate.of(1990, 1, 1), "+1234567890",
                "123 Main St", "New York", "USA"
        );

        customer.rejectKyc();

        assertEquals(Customer.KycStatus.REJECTED, customer.getKycStatus());
    }

    @Test
    public void rejectKyc_whenStatusIsApproved_shouldThrowException() {
        Customer customer = new Customer(
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
                Customer.KycStatus.APPROVED,
                Customer.RiskLevel.LOW
        );

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            customer.rejectKyc();
        });

        String expectedMessage = "KYC can only be rejected from PENDING";
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    public void rejectKyc_whenStatusIsRejected_shouldThrowException() {
        Customer customer = new Customer(
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
                Customer.KycStatus.REJECTED,
                Customer.RiskLevel.LOW
        );

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            customer.rejectKyc();
        });

        String expectedMessage = "KYC can only be rejected from PENDING";
        assertEquals(expectedMessage, exception.getMessage());
    }
}
