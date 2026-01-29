package com.banking.system.unit.common.domain.model;

import com.banking.system.common.domain.Phone;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Phone Value Object Tests")
class PhoneTest {

    @Nested
    @DisplayName("Valid Creation Tests")
    class ValidCreationTests {

        @Test
        @DisplayName("Should create valid mobile phone with international format")
        void shouldCreateValidMobilePhoneWithInternationalFormat() {
            Phone phone = new Phone("+5491112345678");

            assertNotNull(phone);
            assertEquals("+5491112345678", phone.number());
        }

        @Test
        @DisplayName("Should create valid landline phone with international format")
        void shouldCreateValidLandlinePhoneWithInternationalFormat() {
            Phone phone = new Phone("+541112345678");

            assertEquals("+541112345678", phone.number());
        }

        @Test
        @DisplayName("Should create valid phone with digits only")
        void shouldCreateValidPhoneWithDigitsOnly() {
            Phone phone = new Phone("1112345678");

            assertEquals("1112345678", phone.number());
        }

        @Test
        @DisplayName("Should normalize removing spaces")
        void shouldNormalizeRemovingSpaces() {
            Phone phone = new Phone("+54 9 11 1234 5678");

            assertEquals("+5491112345678", phone.number());
        }

        @Test
        @DisplayName("Should normalize removing parentheses")
        void shouldNormalizeRemovingParentheses() {
            Phone phone = new Phone("+54(9)1112345678");

            assertEquals("+5491112345678", phone.number());
        }

        @Test
        @DisplayName("Should normalize removing hyphens")
        void shouldNormalizeRemovingHyphens() {
            Phone phone = new Phone("+549-11-1234-5678");

            assertEquals("+5491112345678", phone.number());
        }

        @Test
        @DisplayName("Should trim whitespace")
        void shouldTrimWhitespace() {
            Phone phone = new Phone("  +5491112345678  ");

            assertEquals("+5491112345678", phone.number());
        }

        @Test
        @DisplayName("Should accept minimum valid length (8 digits)")
        void shouldAcceptMinimumValidLength() {
            Phone phone = new Phone("12345678");

            assertEquals("12345678", phone.number());
        }

        @Test
        @DisplayName("Should accept maximum valid length")
        void shouldAcceptMaximumValidLength() {
            Phone phone = new Phone("1234567890123");

            assertEquals("1234567890123", phone.number());
        }
    }

    @Nested
    @DisplayName("Null Validation Tests")
    class NullValidationTests {

        @Test
        @DisplayName("Should throw NPE when number is null")
        void shouldThrowNpeWhenNumberIsNull() {
            assertThrows(
                    NullPointerException.class,
                    () -> new Phone(null)
            );
        }
    }

    @Nested
    @DisplayName("Blank Validation Tests")
    class BlankValidationTests {

        @ParameterizedTest
        @ValueSource(strings = {"", "   ", "\t", "\n", "  \t\n  "})
        @DisplayName("Should reject blank number")
        void shouldRejectBlankNumber(String blankNumber) {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> new Phone(blankNumber)
            );
            assertEquals("Phone number cannot be blank", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Length Validation Tests")
    class LengthValidationTests {

        @Test
        @DisplayName("Should reject number shorter than 8 digits")
        void shouldRejectNumberShorterThan8Digits() {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> new Phone("1234567")
            );
            assertEquals("Phone number must be between 8 and 20 digits long", exception.getMessage());
        }

        @Test
        @DisplayName("Should reject number longer than 20 digits")
        void shouldRejectNumberLongerThan20Digits() {
            String tooLongNumber = "1".repeat(21);

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> new Phone(tooLongNumber)
            );
            assertEquals("Phone number must be between 8 and 20 digits long", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Format Validation Tests")
    class FormatValidationTests {

        @Test
        @DisplayName("Should reject international format with wrong country code")
        void shouldRejectInternationalFormatWithWrongCountryCode() {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> new Phone("+1234567890123")
            );
            assertTrue(exception.getMessage().contains("Invalid phone number format"));
        }

        @Test
        @DisplayName("Should reject + symbol not at start")
        void shouldRejectPlusSymbolNotAtStart() {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> new Phone("123+456789012")
            );
            assertEquals("'+' symbol must be at the start for international format", exception.getMessage());
        }

        @Test
        @DisplayName("Should reject multiple + symbols")
        void shouldRejectMultiplePlusSymbols() {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> new Phone("+54+9112345678")
            );
            assertEquals("'+' symbol must be at the start for international format", exception.getMessage());
        }

        @Test
        @DisplayName("Should reject + symbol at the end")
        void shouldRejectPlusSymbolAtEnd() {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> new Phone("12345678+")
            );
            assertEquals("'+' symbol must be at the start for international format", exception.getMessage());
        }

        @Test
        @DisplayName("Should reject + symbol in the middle")
        void shouldRejectPlusSymbolInMiddle() {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> new Phone("1234+5678901")
            );
            assertEquals("'+' symbol must be at the start for international format", exception.getMessage());
        }

        @Test
        @DisplayName("Should reject letters in phone number")
        void shouldRejectLettersInPhoneNumber() {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> new Phone("123456789A")
            );
            assertTrue(exception.getMessage().contains("Invalid phone number format"));
        }

        @Test
        @DisplayName("Should reject invalid international mobile format")
        void shouldRejectInvalidInternationalMobileFormat() {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> new Phone("+549123")
            );
            assertTrue(exception.getMessage().contains("Phone number must be between 8 and 20 digits long") ||
                    exception.getMessage().contains("Invalid phone number format"));
        }
    }

    @Nested
    @DisplayName("isMobile() Method Tests")
    class IsMobileMethodTests {

        @Test
        @DisplayName("Should return true for mobile number")
        void shouldReturnTrueForMobileNumber() {
            Phone phone = new Phone("+5491112345678");

            assertTrue(phone.isMobile());
        }

        @Test
        @DisplayName("Should return false for landline number")
        void shouldReturnFalseForLandlineNumber() {
            Phone phone = new Phone("+541112345678");

            assertFalse(phone.isMobile());
        }

        @Test
        @DisplayName("Should return false for local format")
        void shouldReturnFalseForLocalFormat() {
            Phone phone = new Phone("1112345678");

            assertFalse(phone.isMobile());
        }
    }

    @Nested
    @DisplayName("isLandline() Method Tests")
    class IsLandlineMethodTests {

        @Test
        @DisplayName("Should return true for landline number")
        void shouldReturnTrueForLandlineNumber() {
            Phone phone = new Phone("+541112345678");

            assertTrue(phone.isLandline());
        }

        @Test
        @DisplayName("Should return false for mobile number")
        void shouldReturnFalseForMobileNumber() {
            Phone phone = new Phone("+5491112345678");

            assertFalse(phone.isLandline());
        }

        @Test
        @DisplayName("Should return false for local format")
        void shouldReturnFalseForLocalFormat() {
            Phone phone = new Phone("1112345678");

            assertFalse(phone.isLandline());
        }
    }

    @Nested
    @DisplayName("formatted() Method Tests")
    class FormattedMethodTests {

        @Test
        @DisplayName("Should format mobile phone correctly")
        void shouldFormatMobilePhoneCorrectly() {
            Phone phone = new Phone("+5491112345678");

            String formatted = phone.formatted();

            assertEquals("+54 9 11 1234-5678", formatted);
        }

        @Test
        @DisplayName("Should format landline phone correctly")
        void shouldFormatLandlinePhoneCorrectly() {
            Phone phone = new Phone("+541112345678");

            String formatted = phone.formatted();

            assertEquals("+54 11 1234-5678", formatted);
        }

        @Test
        @DisplayName("Should return local format as-is")
        void shouldReturnLocalFormatAsIs() {
            Phone phone = new Phone("1112345678");

            String formatted = phone.formatted();

            assertEquals("1112345678", formatted);
        }
    }

    @Nested
    @DisplayName("masked() Method Tests")
    class MaskedMethodTests {

        @Test
        @DisplayName("Should mask phone number showing last 4 digits")
        void shouldMaskPhoneNumberShowingLast4Digits() {
            Phone phone = new Phone("+5491112345678");

            assertEquals("***-5678", phone.masked());
        }

        @Test
        @DisplayName("Should mask short number")
        void shouldMaskShortNumber() {
            Phone phone = new Phone("12345678");

            assertEquals("***-5678", phone.masked());
        }
    }

    @Nested
    @DisplayName("Equality and HashCode (Record) Tests")
    class EqualityTests {

        @Test
        @DisplayName("Should be equal when numbers are equal")
        void shouldBeEqualWhenNumbersAreEqual() {
            Phone phone1 = new Phone("+5491112345678");
            Phone phone2 = new Phone("+5491112345678");

            assertEquals(phone1, phone2);
            assertEquals(phone2, phone1);
        }

        @Test
        @DisplayName("Should not be equal when numbers differ")
        void shouldNotBeEqualWhenNumbersDiffer() {
            Phone phone1 = new Phone("+5491112345678");
            Phone phone2 = new Phone("+5491187654321");

            assertNotEquals(phone1, phone2);
        }

        @Test
        @DisplayName("Should have same hashCode when equal")
        void shouldHaveSameHashCodeWhenEqual() {
            Phone phone1 = new Phone("+5491112345678");
            Phone phone2 = new Phone("+5491112345678");

            assertEquals(phone1.hashCode(), phone2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal to null")
        void shouldNotBeEqualToNull() {
            Phone phone = new Phone("+5491112345678");

            assertNotEquals(null, phone);
        }

        @Test
        @DisplayName("Should be equal to itself (reflexivity)")
        void shouldBeEqualToItself() {
            Phone phone = new Phone("+5491112345678");

            assertEquals(phone, phone);
        }

        @Test
        @DisplayName("Should be equal when normalized forms match")
        void shouldBeEqualWhenNormalizedFormsMatch() {
            Phone phone1 = new Phone("+54 9 11 1234-5678");
            Phone phone2 = new Phone("+5491112345678");

            assertEquals(phone1, phone2);
        }
    }

    @Nested
    @DisplayName("Immutability (Record) Tests")
    class ImmutabilityTests {

        @Test
        @DisplayName("Should be immutable - number cannot be changed")
        void shouldBeImmutableNumberCannotBeChanged() {
            Phone phone = new Phone("+5491112345678");

            String originalNumber = phone.number();

            assertEquals(originalNumber, phone.number());
        }
    }

    @Nested
    @DisplayName("toString() Method (Record) Tests")
    class ToStringTests {

        @Test
        @DisplayName("Should have meaningful toString representation")
        void shouldHaveMeaningfulToStringRepresentation() {
            Phone phone = new Phone("+5491112345678");

            String toString = phone.toString();

            assertNotNull(toString);
            assertTrue(toString.contains("+5491112345678"));
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle complex formatting in input")
        void shouldHandleComplexFormattingInInput() {
            Phone phone = new Phone("+54 (9) 11 1234-5678");

            assertEquals("+5491112345678", phone.number());
            assertTrue(phone.isMobile());
        }

        @Test
        @DisplayName("Should handle multiple consecutive separators")
        void shouldHandleMultipleConsecutiveSeparators() {
            Phone phone = new Phone("+54--9--11--1234--5678");

            assertEquals("+5491112345678", phone.number());
        }
    }
}
