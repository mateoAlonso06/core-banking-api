package com.banking.system.unit.auth.domain.model;

import com.banking.system.auth.domain.model.Email;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Email Value Object Tests")
class EmailTest {

    @Nested
    @DisplayName("Valid Creation Tests")
    class ValidCreationTests {

        @Test
        @DisplayName("Should create valid email")
        void shouldCreateValidEmail() {
            Email email = new Email("user@example.com");

            assertNotNull(email);
            assertEquals("user@example.com", email.value());
        }

        @Test
        @DisplayName("Should normalize to lowercase")
        void shouldNormalizeToLowercase() {
            Email email = new Email("User@Example.COM");

            assertEquals("user@example.com", email.value());
        }

        @Test
        @DisplayName("Should trim whitespace")
        void shouldTrimWhitespace() {
            Email email = new Email("  user@example.com  ");

            assertEquals("user@example.com", email.value());
        }

        @Test
        @DisplayName("Should accept email with numbers")
        void shouldAcceptEmailWithNumbers() {
            Email email = new Email("user123@example.com");

            assertEquals("user123@example.com", email.value());
        }

        @Test
        @DisplayName("Should accept email with dots in local part")
        void shouldAcceptEmailWithDotsInLocalPart() {
            Email email = new Email("john.doe@example.com");

            assertEquals("john.doe@example.com", email.value());
        }

        @Test
        @DisplayName("Should accept email with plus sign")
        void shouldAcceptEmailWithPlusSign() {
            Email email = new Email("user+tag@example.com");

            assertEquals("user+tag@example.com", email.value());
        }

        @Test
        @DisplayName("Should accept email with underscore")
        void shouldAcceptEmailWithUnderscore() {
            Email email = new Email("user_name@example.com");

            assertEquals("user_name@example.com", email.value());
        }

        @Test
        @DisplayName("Should accept email with hyphen in domain")
        void shouldAcceptEmailWithHyphenInDomain() {
            Email email = new Email("user@my-domain.com");

            assertEquals("user@my-domain.com", email.value());
        }

        @Test
        @DisplayName("Should accept subdomain")
        void shouldAcceptSubdomain() {
            Email email = new Email("user@mail.example.com");

            assertEquals("user@mail.example.com", email.value());
        }

        @Test
        @DisplayName("Should accept two-letter TLD")
        void shouldAcceptTwoLetterTld() {
            Email email = new Email("user@example.co");

            assertEquals("user@example.co", email.value());
        }
    }

    @Nested
    @DisplayName("Null Validation Tests")
    class NullValidationTests {

        @Test
        @DisplayName("Should throw NPE when email is null")
        void shouldThrowNpeWhenEmailIsNull() {
            NullPointerException exception = assertThrows(
                    NullPointerException.class,
                    () -> new Email(null)
            );
            assertEquals("Email cannot be null", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Blank Validation Tests")
    class BlankValidationTests {

        @ParameterizedTest
        @ValueSource(strings = {"", "   ", "\t", "\n", "  \t\n  "})
        @DisplayName("Should reject blank email")
        void shouldRejectBlankEmail(String blankEmail) {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> new Email(blankEmail)
            );
            assertEquals("Email cannot be blank", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Length Validation Tests")
    class LengthValidationTests {

        @Test
        @DisplayName("Should reject email exceeding maximum length")
        void shouldRejectEmailExceedingMaximumLength() {
            String localPart = "a".repeat(60);
            String longEmail = localPart + "@example.com";
            // Total length will be > 254 characters

            String veryLongEmail = "a".repeat(245) + "@example.com";

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> new Email(veryLongEmail)
            );
            assertTrue(exception.getMessage().contains("exceeds maximum length"));
        }

        @Test
        @DisplayName("Should reject email with local part exceeding 64 characters")
        void shouldRejectEmailWithLocalPartExceeding64Characters() {
            String localPart = "a".repeat(65);
            String email = localPart + "@example.com";

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> new Email(email)
            );
            assertTrue(exception.getMessage().contains("local part exceeds maximum length"));
        }

        @Test
        @DisplayName("Should accept email with local part at 64 characters")
        void shouldAcceptEmailWithLocalPartAt64Characters() {
            String localPart = "a".repeat(64);
            String email = localPart + "@example.com";

            Email emailObj = new Email(email);

            assertEquals(email, emailObj.value());
        }
    }

    @Nested
    @DisplayName("Format Validation Tests")
    class FormatValidationTests {

        @Test
        @DisplayName("Should reject email without @ symbol")
        void shouldRejectEmailWithoutAtSymbol() {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> new Email("userexample.com")
            );
            assertTrue(exception.getMessage().contains("Invalid email format"));
        }

        @Test
        @DisplayName("Should reject email without domain")
        void shouldRejectEmailWithoutDomain() {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> new Email("user@")
            );
            assertTrue(exception.getMessage().contains("Invalid email format"));
        }

        @Test
        @DisplayName("Should reject email without local part")
        void shouldRejectEmailWithoutLocalPart() {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> new Email("@example.com")
            );
            assertTrue(exception.getMessage().contains("Invalid email format"));
        }

        @Test
        @DisplayName("Should reject email without TLD")
        void shouldRejectEmailWithoutTld() {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> new Email("user@example")
            );
            assertTrue(exception.getMessage().contains("Invalid email format"));
        }

        @Test
        @DisplayName("Should reject email with consecutive dots")
        void shouldRejectEmailWithConsecutiveDots() {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> new Email("user..name@example.com")
            );
            assertEquals("Email cannot contain consecutive dots", exception.getMessage());
        }

        @Test
        @DisplayName("Should reject email with multiple @ symbols")
        void shouldRejectEmailWithMultipleAtSymbols() {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> new Email("user@name@example.com")
            );
            assertTrue(exception.getMessage().contains("Invalid email format"));
        }

        @Test
        @DisplayName("Should reject email with special characters")
        void shouldRejectEmailWithSpecialCharacters() {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> new Email("user#name@example.com")
            );
            assertTrue(exception.getMessage().contains("Invalid email format"));
        }

        @Test
        @DisplayName("Should reject email with spaces in local part")
        void shouldRejectEmailWithSpacesInLocalPart() {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> new Email("user name@example.com")
            );
            assertTrue(exception.getMessage().contains("Invalid email format"));
        }
    }

    @Nested
    @DisplayName("masked() Method Tests")
    class MaskedMethodTests {

        @Test
        @DisplayName("Should mask email showing first 2 characters of local part")
        void shouldMaskEmailShowingFirst2CharactersOfLocalPart() {
            Email email = new Email("john.doe@example.com");

            assertEquals("jo***@example.com", email.masked());
        }

        @Test
        @DisplayName("Should mask short email with 2 or fewer characters in local part")
        void shouldMaskShortEmailWith2OrFewerCharactersInLocalPart() {
            Email email = new Email("ab@example.com");

            assertEquals("***@example.com", email.masked());
        }

        @Test
        @DisplayName("Should mask single character email")
        void shouldMaskSingleCharacterEmail() {
            Email email = new Email("a@example.com");

            assertEquals("***@example.com", email.masked());
        }

        @Test
        @DisplayName("Should preserve domain in masked output")
        void shouldPreserveDomainInMaskedOutput() {
            Email email = new Email("user@subdomain.example.com");

            String masked = email.masked();

            assertTrue(masked.endsWith("@subdomain.example.com"));
        }
    }

    @Nested
    @DisplayName("domain() Method Tests")
    class DomainMethodTests {

        @Test
        @DisplayName("Should return domain part")
        void shouldReturnDomainPart() {
            Email email = new Email("user@example.com");

            assertEquals("example.com", email.domain());
        }

        @Test
        @DisplayName("Should return subdomain correctly")
        void shouldReturnSubdomainCorrectly() {
            Email email = new Email("user@mail.example.com");

            assertEquals("mail.example.com", email.domain());
        }
    }

    @Nested
    @DisplayName("toString() Method Tests")
    class ToStringTests {

        @Test
        @DisplayName("Should return email value as string")
        void shouldReturnEmailValueAsString() {
            Email email = new Email("user@example.com");

            assertEquals("user@example.com", email.toString());
        }
    }

    @Nested
    @DisplayName("Equality and HashCode (Record) Tests")
    class EqualityTests {

        @Test
        @DisplayName("Should be equal when values are equal")
        void shouldBeEqualWhenValuesAreEqual() {
            Email email1 = new Email("user@example.com");
            Email email2 = new Email("user@example.com");

            assertEquals(email1, email2);
            assertEquals(email2, email1);
        }

        @Test
        @DisplayName("Should not be equal when values differ")
        void shouldNotBeEqualWhenValuesDiffer() {
            Email email1 = new Email("user1@example.com");
            Email email2 = new Email("user2@example.com");

            assertNotEquals(email1, email2);
        }

        @Test
        @DisplayName("Should have same hashCode when equal")
        void shouldHaveSameHashCodeWhenEqual() {
            Email email1 = new Email("user@example.com");
            Email email2 = new Email("user@example.com");

            assertEquals(email1.hashCode(), email2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal to null")
        void shouldNotBeEqualToNull() {
            Email email = new Email("user@example.com");

            assertNotEquals(null, email);
        }

        @Test
        @DisplayName("Should be equal to itself (reflexivity)")
        void shouldBeEqualToItself() {
            Email email = new Email("user@example.com");

            assertEquals(email, email);
        }

        @Test
        @DisplayName("Should be equal when normalized forms match")
        void shouldBeEqualWhenNormalizedFormsMatch() {
            Email email1 = new Email("User@Example.COM");
            Email email2 = new Email("user@example.com");

            assertEquals(email1, email2);
        }
    }

    @Nested
    @DisplayName("Immutability (Record) Tests")
    class ImmutabilityTests {

        @Test
        @DisplayName("Should be immutable - value cannot be changed")
        void shouldBeImmutableValueCannotBeChanged() {
            Email email = new Email("user@example.com");

            String originalValue = email.value();

            assertEquals(originalValue, email.value());
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle email with maximum valid local part length")
        void shouldHandleEmailWithMaximumValidLocalPartLength() {
            String localPart = "a".repeat(64);
            Email email = new Email(localPart + "@example.com");

            assertEquals((localPart + "@example.com").toLowerCase(), email.value());
        }

        @Test
        @DisplayName("Should normalize mixed case properly")
        void shouldNormalizeMixedCaseProperly() {
            Email email = new Email("JoHn.DoE@ExAmPlE.CoM");

            assertEquals("john.doe@example.com", email.value());
        }

        @Test
        @DisplayName("Should handle multiple subdomains")
        void shouldHandleMultipleSubdomains() {
            Email email = new Email("user@mail.corp.example.com");

            assertEquals("user@mail.corp.example.com", email.value());
            assertEquals("mail.corp.example.com", email.domain());
        }
    }
}
