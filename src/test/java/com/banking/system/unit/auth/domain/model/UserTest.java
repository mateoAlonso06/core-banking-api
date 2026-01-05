package com.banking.system.unit.auth.domain.model;

import com.banking.system.auth.domain.model.Role;
import com.banking.system.auth.domain.model.User;
import com.banking.system.auth.domain.model.UserStatus;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    public void createNew_whenValidEmailAndPassword_shouldCreateUserWithDefaults() {
        String email = "test@example.com";
        String passwordHash = "hashedPassword123";

        User user = User.createNew(email, passwordHash);

        assertNotNull(user);
        assertNull(user.getId());
        assertEquals(email, user.getEmail());
        assertEquals(passwordHash, user.getPasswordHash());
        assertEquals(UserStatus.PENDING_VERIFICATION, user.getStatus());
        assertEquals(Role.CUSTOMER, user.getRole());
    }

    @Test
    public void createNew_whenEmailIsNull_shouldThrowException() {
        String passwordHash = "hashedPassword123";

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            User.createNew(null, passwordHash);
        });

        String expectedMessage = "Email and passwordHash cannot be null";
        String actualMessage = exception.getMessage();

        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    public void createNew_whenPasswordHashIsNull_shouldThrowException() {
        String email = "test@example.com";

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            User.createNew(email, null);
        });

        String expectedMessage = "Email and passwordHash cannot be null";
        String actualMessage = exception.getMessage();

        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    public void createNew_whenBothEmailAndPasswordAreNull_shouldThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            User.createNew(null, null);
        });

        String expectedMessage = "Email and passwordHash cannot be null";
        String actualMessage = exception.getMessage();

        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    public void activate_whenStatusIsPendingVerification_shouldActivateUser() {
        User user = User.createNew("test@example.com", "hashedPassword");

        user.activate();

        assertEquals(UserStatus.ACTIVE, user.getStatus());
    }

    @Test
    public void activate_whenStatusIsActive_shouldThrowException() {
        User user = new User(
                UUID.randomUUID(),
                "test@example.com",
                "hashedPassword",
                UserStatus.ACTIVE,
                Role.CUSTOMER
        );

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            user.activate();
        });

        String expectedMessage = "Only users with PENDING_VERIFICATION status can be activated";
        String actualMessage = exception.getMessage();

        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    public void activate_whenStatusIsBlocked_shouldThrowException() {
        User user = new User(
                UUID.randomUUID(),
                "test@example.com",
                "hashedPassword",
                UserStatus.BLOCKED,
                Role.CUSTOMER
        );

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            user.activate();
        });

        String expectedMessage = "Only users with PENDING_VERIFICATION status can be activated";
        String actualMessage = exception.getMessage();

        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    public void block_whenStatusIsActive_shouldBlockUser() {
        User user = new User(
                UUID.randomUUID(),
                "test@example.com",
                "hashedPassword",
                UserStatus.ACTIVE,
                Role.CUSTOMER
        );

        user.block();

        assertEquals(UserStatus.BLOCKED, user.getStatus());
    }

    @Test
    public void block_whenStatusIsPendingVerification_shouldThrowException() {
        User user = User.createNew("test@example.com", "hashedPassword");

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            user.block();
        });

        String expectedMessage = "Only ACTIVE users can be blocked";
        String actualMessage = exception.getMessage();

        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    public void block_whenStatusIsBlocked_shouldThrowException() {
        User user = new User(
                UUID.randomUUID(),
                "test@example.com",
                "hashedPassword",
                UserStatus.BLOCKED,
                Role.CUSTOMER
        );

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            user.block();
        });

        String expectedMessage = "Only ACTIVE users can be blocked";
        String actualMessage = exception.getMessage();

        assertEquals(expectedMessage, actualMessage);
    }
}
