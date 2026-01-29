package com.banking.system.unit.auth.domain.model;

import com.banking.system.auth.domain.model.Password;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Password Value Object Tests")
class PasswordTest {

    private static final String VALID_PLAIN_PASSWORD = "SecurePass123!";
    private static final String VALID_BCRYPT_HASH = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";

    @Nested
    @DisplayName("Factory Method: fromPlainPassword")
    class FromPlainPasswordTests {

        @Test
        @DisplayName("Should create password from valid plain password")
        void shouldCreatePassword_whenPlainPasswordIsValid() {
            Password password = Password.fromPlainPassword(VALID_PLAIN_PASSWORD);

            assertNotNull(password);
            assertTrue(password.isPlain());
            assertFalse(password.isHashed());
            assertEquals(VALID_PLAIN_PASSWORD, password.plainPassword());
            assertNull(password.hashedPassword());
        }

        @Test
        @DisplayName("Should throw exception when plain password is null")
        void shouldThrowException_whenPlainPasswordIsNull() {
            assertThrows(IllegalArgumentException.class, () ->
                    Password.fromPlainPassword(null)
            );
        }

        @Test
        @DisplayName("Should throw exception when password is too short")
        void shouldThrowException_whenPasswordIsTooShort() {
            String shortPassword = "Short1!";

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                    Password.fromPlainPassword(shortPassword)
            );

            assertTrue(exception.getMessage().contains("at least 8 characters"));
        }

        @Test
        @DisplayName("Should throw exception when password exceeds max length")
        void shouldThrowException_whenPasswordExceedsMaxLength() {
            String longPassword = "A1!" + "a".repeat(126);

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                    Password.fromPlainPassword(longPassword)
            );

            assertTrue(exception.getMessage().contains("cannot exceed 128 characters"));
        }

        @Test
        @DisplayName("Should throw exception when password contains whitespace")
        void shouldThrowException_whenPasswordContainsWhitespace() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                    Password.fromPlainPassword("Password 123!")
            );

            assertTrue(exception.getMessage().contains("cannot contain whitespace"));
        }

        @Test
        @DisplayName("Should throw exception when password lacks uppercase letter")
        void shouldThrowException_whenPasswordLacksUppercase() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                    Password.fromPlainPassword("password123!")
            );

            assertTrue(exception.getMessage().contains("at least one uppercase letter"));
        }

        @Test
        @DisplayName("Should throw exception when password lacks lowercase letter")
        void shouldThrowException_whenPasswordLacksLowercase() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                    Password.fromPlainPassword("PASSWORD123!")
            );

            assertTrue(exception.getMessage().contains("at least one lowercase letter"));
        }

        @Test
        @DisplayName("Should throw exception when password lacks digit")
        void shouldThrowException_whenPasswordLacksDigit() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                    Password.fromPlainPassword("PasswordAbc!")
            );

            assertTrue(exception.getMessage().contains("at least one digit"));
        }

        @Test
        @DisplayName("Should throw exception when password lacks special character")
        void shouldThrowException_whenPasswordLacksSpecialChar() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                    Password.fromPlainPassword("Password123")
            );

            assertTrue(exception.getMessage().contains("at least one special character"));
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "Secure123!",
                "MyP@ssw0rd",
                "C0mpl3x!Pass",
                "Str0ng#Password",
                "V@lid1Pass",
                "T3st_Pass!",
                "P@ssw0rd+Test"
        })
        @DisplayName("Should accept valid passwords with different special characters")
        void shouldAcceptValidPasswords_withDifferentSpecialChars(String password) {
            Password result = Password.fromPlainPassword(password);

            assertNotNull(result);
            assertTrue(result.isPlain());
        }
    }

    @Nested
    @DisplayName("Factory Method: fromHash")
    class FromHashTests {

        @Test
        @DisplayName("Should create password from valid hash")
        void shouldCreatePassword_whenHashIsValid() {
            Password password = Password.fromHash(VALID_BCRYPT_HASH);

            assertNotNull(password);
            assertTrue(password.isHashed());
            assertFalse(password.isPlain());
            assertEquals(VALID_BCRYPT_HASH, password.hashedPassword());
            assertNull(password.plainPassword());
        }

        @Test
        @DisplayName("Should throw exception when hash is null")
        void shouldThrowException_whenHashIsNull() {
            assertThrows(IllegalArgumentException.class, () ->
                    Password.fromHash(null)
            );
        }

        @Test
        @DisplayName("Should throw exception when hash is blank")
        void shouldThrowException_whenHashIsBlank() {
            assertThrows(IllegalArgumentException.class, () ->
                    Password.fromHash("   ")
            );
        }

        @Test
        @DisplayName("Should throw exception when hash is empty string")
        void shouldThrowException_whenHashIsEmptyString() {
            assertThrows(IllegalArgumentException.class, () ->
                    Password.fromHash("")
            );
        }
    }

    @Nested
    @DisplayName("State Validation Tests")
    class StateValidationTests {

        @Test
        @DisplayName("Should throw exception when both plain and hashed are null")
        void shouldThrowException_whenBothPlainAndHashedAreNull() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                    new Password(null, null)
            );

            assertTrue(exception.getMessage().contains("either plain or hashed"));
        }

        @Test
        @DisplayName("Should throw exception when both plain and hashed are provided")
        void shouldThrowException_whenBothPlainAndHashedAreProvided() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                    new Password(VALID_PLAIN_PASSWORD, VALID_BCRYPT_HASH)
            );

            assertTrue(exception.getMessage().contains("cannot have both"));
        }
    }

    @Nested
    @DisplayName("State Query Methods")
    class StateQueryMethodsTests {

        @Test
        @DisplayName("Should return true for isPlain when created from plain password")
        void shouldReturnTrue_whenIsPlainCalledOnPlainPassword() {
            Password password = Password.fromPlainPassword(VALID_PLAIN_PASSWORD);

            assertTrue(password.isPlain());
            assertFalse(password.isHashed());
        }

        @Test
        @DisplayName("Should return true for isHashed when created from hash")
        void shouldReturnTrue_whenIsHashedCalledOnHashedPassword() {
            Password password = Password.fromHash(VALID_BCRYPT_HASH);

            assertTrue(password.isHashed());
            assertFalse(password.isPlain());
        }
    }

    @Nested
    @DisplayName("value() Method Tests")
    class ValueMethodTests {

        @Test
        @DisplayName("Should return plain password when in plain state")
        void shouldReturnPlainPassword_whenInPlainState() {
            Password password = Password.fromPlainPassword(VALID_PLAIN_PASSWORD);

            assertEquals(VALID_PLAIN_PASSWORD, password.value());
        }

        @Test
        @DisplayName("Should return hashed password when in hashed state")
        void shouldReturnHashedPassword_whenInHashedState() {
            Password password = Password.fromHash(VALID_BCRYPT_HASH);

            assertEquals(VALID_BCRYPT_HASH, password.value());
        }
    }

    @Nested
    @DisplayName("toString() Security Tests")
    class ToStringSecurityTests {

        @Test
        @DisplayName("Should not expose plain password in toString")
        void shouldNotExposePlainPassword_inToString() {
            Password password = Password.fromPlainPassword(VALID_PLAIN_PASSWORD);

            String toString = password.toString();

            assertFalse(toString.contains(VALID_PLAIN_PASSWORD));
            assertTrue(toString.contains("plain"));
        }

        @Test
        @DisplayName("Should not expose hashed password in toString")
        void shouldNotExposeHashedPassword_inToString() {
            Password password = Password.fromHash(VALID_BCRYPT_HASH);

            String toString = password.toString();

            assertFalse(toString.contains(VALID_BCRYPT_HASH));
            assertTrue(toString.contains("hashed"));
        }

        @Test
        @DisplayName("Should return Password[plain] for plain password")
        void shouldReturnCorrectFormat_forPlainPassword() {
            Password password = Password.fromPlainPassword(VALID_PLAIN_PASSWORD);

            assertEquals("Password[plain]", password.toString());
        }

        @Test
        @DisplayName("Should return Password[hashed] for hashed password")
        void shouldReturnCorrectFormat_forHashedPassword() {
            Password password = Password.fromHash(VALID_BCRYPT_HASH);

            assertEquals("Password[hashed]", password.toString());
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should accept password with exactly 8 characters")
        void shouldAcceptPassword_withExactly8Characters() {
            Password password = Password.fromPlainPassword("Passw0rd!");

            assertNotNull(password);
            assertTrue(password.isPlain());
        }

        @Test
        @DisplayName("Should accept password with exactly 128 characters")
        void shouldAcceptPassword_withExactly128Characters() {
            String maxLengthPassword = "A1!" + "a".repeat(125);
            Password password = Password.fromPlainPassword(maxLengthPassword);

            assertNotNull(password);
            assertEquals(128, password.value().length());
        }

        @Test
        @DisplayName("Should accept password with all valid special characters")
        void shouldAcceptPassword_withAllValidSpecialChars() {
            String passwordWithAllSpecialChars = "P@ssw0rd!#$%^&*()_+-=[]{}|;':\"\\,.<>/?";

            Password password = Password.fromPlainPassword(passwordWithAllSpecialChars);

            assertNotNull(password);
            assertTrue(password.isPlain());
        }

        @Test
        @DisplayName("Should reject password with tab character")
        void shouldRejectPassword_withTabCharacter() {
            assertThrows(IllegalArgumentException.class, () ->
                    Password.fromPlainPassword("Pass\tword123!")
            );
        }

        @Test
        @DisplayName("Should reject password with newline character")
        void shouldRejectPassword_withNewlineCharacter() {
            assertThrows(IllegalArgumentException.class, () ->
                    Password.fromPlainPassword("Pass\nword123!")
            );
        }
    }
}
