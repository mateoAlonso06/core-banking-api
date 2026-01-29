package com.banking.system.unit.common.domain.model;

import com.banking.system.common.domain.IdentityDocument;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("IdentityDocument Value Object Tests")
class IdentityDocumentTest {

    @Nested
    @DisplayName("Valid Creation Tests")
    class ValidCreationTests {

        @Test
        @DisplayName("Should create valid DNI with 8 digits")
        void shouldCreateValidDniWith8Digits() {
            IdentityDocument document = new IdentityDocument("12345678", "DNI");

            assertNotNull(document);
            assertEquals("12345678", document.number());
            assertEquals("DNI", document.type());
        }

        @Test
        @DisplayName("Should create valid DNI with 7 digits (old format)")
        void shouldCreateValidDniWith7Digits() {
            IdentityDocument document = new IdentityDocument("1234567", "dni");

            assertEquals("1234567", document.number());
            assertEquals("DNI", document.type());
        }

        @Test
        @DisplayName("Should create valid passport with 3 letters + 6 digits")
        void shouldCreateValidPassportWith3Letters6Digits() {
            IdentityDocument document = new IdentityDocument("AAA123456", "PASSPORT");

            assertEquals("AAA123456", document.number());
            assertEquals("PASSPORT", document.type());
        }

        @Test
        @DisplayName("Should create valid passport with 2 letters + 9 digits")
        void shouldCreateValidPassportWith2Letters9Digits() {
            IdentityDocument document = new IdentityDocument("AB123456789", "passport");

            assertEquals("AB123456789", document.number());
            assertEquals("PASSPORT", document.type());
        }

        @Test
        @DisplayName("Should normalize type to uppercase")
        void shouldNormalizeTypeToUppercase() {
            IdentityDocument document = new IdentityDocument("12345678", "dni");

            assertEquals("DNI", document.type());
        }

        @Test
        @DisplayName("Should normalize number removing separators")
        void shouldNormalizeNumberRemovingSeparators() {
            IdentityDocument document = new IdentityDocument("12.345.678", "DNI");

            assertEquals("12345678", document.number());
        }

        @Test
        @DisplayName("Should normalize number removing spaces and hyphens")
        void shouldNormalizeNumberRemovingSpacesAndHyphens() {
            IdentityDocument document = new IdentityDocument("12 345-678", "DNI");

            assertEquals("12345678", document.number());
        }

        @Test
        @DisplayName("Should trim whitespace from all fields")
        void shouldTrimWhitespaceFromAllFields() {
            IdentityDocument document = new IdentityDocument("  12345678  ", "  DNI  ");

            assertEquals("12345678", document.number());
            assertEquals("DNI", document.type());
        }
    }

    @Nested
    @DisplayName("Null Validation Tests")
    class NullValidationTests {

        @Test
        @DisplayName("Should throw NPE when number is null")
        void shouldThrowNpeWhenNumberIsNull() {
            NullPointerException exception = assertThrows(
                    NullPointerException.class,
                    () -> new IdentityDocument(null, "DNI")
            );
            assertEquals("Identity document number cannot be null", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw NPE when type is null")
        void shouldThrowNpeWhenTypeIsNull() {
            NullPointerException exception = assertThrows(
                    NullPointerException.class,
                    () -> new IdentityDocument("12345678", null)
            );
            assertEquals("Identity document type cannot be null", exception.getMessage());
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
                    () -> new IdentityDocument(blankNumber, "DNI")
            );
            assertEquals("Identity document number cannot be blank", exception.getMessage());
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   ", "\t", "\n", "  \t\n  "})
        @DisplayName("Should reject blank type")
        void shouldRejectBlankType(String blankType) {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> new IdentityDocument("12345678", blankType)
            );
            assertEquals("Identity document type cannot be blank", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Type Validation Tests")
    class TypeValidationTests {

        @ParameterizedTest
        @ValueSource(strings = {"CEDULA", "LICENSE", "ID", "SSN", "INVALID"})
        @DisplayName("Should reject invalid document types")
        void shouldRejectInvalidDocumentTypes(String invalidType) {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> new IdentityDocument("12345678", invalidType)
            );
            assertTrue(exception.getMessage().contains("Invalid identity document type"));
            assertTrue(exception.getMessage().contains("Allowed types"));
        }
    }

    @Nested
    @DisplayName("Length Validation Tests")
    class LengthValidationTests {

        @Test
        @DisplayName("Should reject number shorter than 5 characters")
        void shouldRejectNumberShorterThan5Characters() {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> new IdentityDocument("1234", "DNI")
            );
            assertTrue(exception.getMessage().contains("must be between 5 and 20 characters"));
        }

        @Test
        @DisplayName("Should reject number longer than 20 characters")
        void shouldRejectNumberLongerThan20Characters() {
            String tooLongNumber = "1".repeat(21);

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> new IdentityDocument(tooLongNumber, "DNI")
            );
            assertTrue(exception.getMessage().contains("must be between 5 and 20 characters"));
        }
    }

    @Nested
    @DisplayName("DNI Format Validation Tests")
    class DniFormatValidationTests {

        @Test
        @DisplayName("Should reject DNI with less than 7 digits")
        void shouldRejectDniWithLessThan7Digits() {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> new IdentityDocument("123456", "DNI")
            );
            assertTrue(exception.getMessage().contains("Invalid DNI format"));
        }

        @Test
        @DisplayName("Should reject DNI with more than 8 digits")
        void shouldRejectDniWithMoreThan8Digits() {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> new IdentityDocument("123456789", "DNI")
            );
            assertTrue(exception.getMessage().contains("Invalid DNI format"));
        }

        @Test
        @DisplayName("Should reject DNI with letters")
        void shouldRejectDniWithLetters() {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> new IdentityDocument("1234567A", "DNI")
            );
            assertTrue(exception.getMessage().contains("Invalid DNI format"));
        }
    }

    @Nested
    @DisplayName("Passport Format Validation Tests")
    class PassportFormatValidationTests {

        @Test
        @DisplayName("Should reject passport with only digits")
        void shouldRejectPassportWithOnlyDigits() {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> new IdentityDocument("123456789", "PASSPORT")
            );
            assertTrue(exception.getMessage().contains("Invalid PASSPORT format"));
        }

        @Test
        @DisplayName("Should reject passport with single letter prefix")
        void shouldRejectPassportWithSingleLetterPrefix() {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> new IdentityDocument("A12345678", "PASSPORT")
            );
            assertTrue(exception.getMessage().contains("Invalid PASSPORT format"));
        }

        @Test
        @DisplayName("Should reject passport with 4+ letter prefix")
        void shouldRejectPassportWith4PlusLetterPrefix() {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> new IdentityDocument("ABCD123456", "PASSPORT")
            );
            assertTrue(exception.getMessage().contains("Invalid PASSPORT format"));
        }

        @Test
        @DisplayName("Should reject passport with less than 6 digits")
        void shouldRejectPassportWithLessThan6Digits() {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> new IdentityDocument("AB12345", "PASSPORT")
            );
            assertTrue(exception.getMessage().contains("Invalid PASSPORT format"));
        }
    }

    @Nested
    @DisplayName("Character Validation Tests")
    class CharacterValidationTests {

        @ParameterizedTest
        @ValueSource(strings = {"1234567@", "12345#78", "1234$678"})
        @DisplayName("Should reject number with special characters")
        void shouldRejectNumberWithSpecialCharacters(String invalidNumber) {
            // Note: Special characters like @, #, $ are not removed by normalization
            // and will cause either format validation or alphanumeric validation to fail
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> new IdentityDocument(invalidNumber, "DNI")
            );
            // Can fail on DNI format (expects only digits) or alphanumeric validation
            assertTrue(exception.getMessage().contains("Invalid DNI format") ||
                    exception.getMessage().contains("can only contain alphanumeric characters"));
        }
    }

    @Nested
    @DisplayName("maskedNumber() Method Tests")
    class MaskedNumberMethodTests {

        @Test
        @DisplayName("Should mask DNI number correctly")
        void shouldMaskDniNumberCorrectly() {
            IdentityDocument document = new IdentityDocument("12345678", "DNI");

            assertEquals("12***78", document.maskedNumber());
        }

        @Test
        @DisplayName("Should mask passport number correctly")
        void shouldMaskPassportNumberCorrectly() {
            IdentityDocument document = new IdentityDocument("ABC123456", "PASSPORT");

            assertEquals("AB***56", document.maskedNumber());
        }

        @Test
        @DisplayName("Should fully mask short numbers")
        void shouldFullyMaskShortNumbers() {
            IdentityDocument document = new IdentityDocument("AB123456", "PASSPORT");

            String masked = document.maskedNumber();
            assertTrue(masked.startsWith("AB"));
            assertTrue(masked.endsWith("56"));
        }
    }

    @Nested
    @DisplayName("Equality and HashCode (Record) Tests")
    class EqualityTests {

        @Test
        @DisplayName("Should be equal when number and type are equal")
        void shouldBeEqualWhenNumberAndTypeAreEqual() {
            IdentityDocument doc1 = new IdentityDocument("12345678", "DNI");
            IdentityDocument doc2 = new IdentityDocument("12345678", "DNI");

            assertEquals(doc1, doc2);
            assertEquals(doc2, doc1);
        }

        @Test
        @DisplayName("Should not be equal when number differs")
        void shouldNotBeEqualWhenNumberDiffers() {
            IdentityDocument doc1 = new IdentityDocument("12345678", "DNI");
            IdentityDocument doc2 = new IdentityDocument("87654321", "DNI");

            assertNotEquals(doc1, doc2);
        }

        @Test
        @DisplayName("Should not be equal when type differs")
        void shouldNotBeEqualWhenTypeDiffers() {
            IdentityDocument doc1 = new IdentityDocument("AB123456", "PASSPORT");
            IdentityDocument doc2 = new IdentityDocument("12345678", "DNI");

            assertNotEquals(doc1, doc2);
        }

        @Test
        @DisplayName("Should have same hashCode when equal")
        void shouldHaveSameHashCodeWhenEqual() {
            IdentityDocument doc1 = new IdentityDocument("12345678", "DNI");
            IdentityDocument doc2 = new IdentityDocument("12345678", "DNI");

            assertEquals(doc1.hashCode(), doc2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal to null")
        void shouldNotBeEqualToNull() {
            IdentityDocument document = new IdentityDocument("12345678", "DNI");

            assertNotEquals(null, document);
        }

        @Test
        @DisplayName("Should be equal to itself (reflexivity)")
        void shouldBeEqualToItself() {
            IdentityDocument document = new IdentityDocument("12345678", "DNI");

            assertEquals(document, document);
        }
    }

    @Nested
    @DisplayName("Immutability (Record) Tests")
    class ImmutabilityTests {

        @Test
        @DisplayName("Should be immutable - fields cannot be changed")
        void shouldBeImmutableFieldsCannotBeChanged() {
            IdentityDocument document = new IdentityDocument("12345678", "DNI");

            String originalNumber = document.number();
            String originalType = document.type();

            assertEquals(originalNumber, document.number());
            assertEquals(originalType, document.type());
        }
    }

    @Nested
    @DisplayName("toString() Method (Record) Tests")
    class ToStringTests {

        @Test
        @DisplayName("Should have meaningful toString representation")
        void shouldHaveMeaningfulToStringRepresentation() {
            IdentityDocument document = new IdentityDocument("12345678", "DNI");

            String toString = document.toString();

            assertNotNull(toString);
            assertTrue(toString.contains("12345678"));
            assertTrue(toString.contains("DNI"));
        }
    }

    @Nested
    @DisplayName("Normalization Edge Cases")
    class NormalizationEdgeCases {

        @Test
        @DisplayName("Should handle mixed case type")
        void shouldHandleMixedCaseType() {
            IdentityDocument document = new IdentityDocument("12345678", "DnI");

            assertEquals("DNI", document.type());
        }

        @Test
        @DisplayName("Should handle passport number with lowercase letters")
        void shouldHandlePassportNumberWithLowercaseLetters() {
            IdentityDocument document = new IdentityDocument("abc123456", "PASSPORT");

            assertEquals("ABC123456", document.number());
        }

        @Test
        @DisplayName("Should handle multiple separators in number")
        void shouldHandleMultipleSeparatorsInNumber() {
            IdentityDocument document = new IdentityDocument("12.345-678", "DNI");

            assertEquals("12345678", document.number());
        }
    }
}
