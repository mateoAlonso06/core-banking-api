package com.banking.system.unit.common.domain.model;

import com.banking.system.common.domain.PersonName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PersonName Value Object Tests")
class PersonNameTest {

    @Nested
    class ValidCreationTests {

        @Test
        @DisplayName("Should create valid person name with simple names")
        void shouldCreateValidPersonNameWithSimpleNames() {
            PersonName personName = new PersonName("Juan", "Perez");

            assertNotNull(personName);
            assertEquals("Juan", personName.firstName());
            assertEquals("Perez", personName.lastName());
        }

        @Test
        @DisplayName("Should create valid person name with accented characters")
        void setShouldCreateValidPersonNameWithAccentedCharacters() {
            PersonName personName = new PersonName("José", "García");

            assertEquals("José", personName.firstName());
            assertEquals("García", personName.lastName());
        }

        @Test
        @DisplayName("Should create valid person name with special Latin characters")
        void shouldCreateValidPersonNameWithSpecialLatinCharacters() {
            PersonName personName = new PersonName("Anaïs", "López");

            assertEquals("Anaïs", personName.firstName());
            assertEquals("López", personName.lastName());
        }

        @Test
        @DisplayName("Should create valid person name with international characters")
        void shouldCreateValidPersonNameWithInternationalCharacters() {
            // Given/When
            PersonName cyrillic = new PersonName("Владимир", "Иванов");
            PersonName chinese = new PersonName("明", "王");
            PersonName arabic = new PersonName("محمد", "علي");

            // Then
            assertNotNull(cyrillic);
            assertNotNull(chinese);
            assertNotNull(arabic);
        }

        @Test
        @DisplayName("Should create person name with maximum allowed length")
        void shouldCreatePersonNameWithMaximumAllowedLength() {
            // Given - 100 caracteres exactos
            String maxLengthName = "a".repeat(100);

            // When
            PersonName personName = new PersonName(maxLengthName, maxLengthName);

            // Then
            assertEquals(100, personName.firstName().length());
            assertEquals(100, personName.lastName().length());
        }

        @Test
        @DisplayName("Should create person name with single character names")
        void shouldCreatePersonNameWithSingleCharacterNames() {
            // When
            PersonName personName = new PersonName("A", "B");

            // Then
            assertEquals("A", personName.firstName());
            assertEquals("B", personName.lastName());
        }
    }

    @Nested
    @DisplayName("Null validations")
    class NullValidationTests {

        @Test
        @DisplayName("Should throw NPE when firstName is null")
        void shouldThrowNpeWhenFirstNameIsNull() {
            // When/Then
            NullPointerException exception = assertThrows(
                    NullPointerException.class,
                    () -> new PersonName(null, "Pérez")
            );
            assertNotNull(exception);
        }

        @Test
        @DisplayName("Should throw NPE when last name is null")
        void shouldThrowNpeWhenLastNameIsNull() {
            // When/Then
            NullPointerException exception = assertThrows(
                    NullPointerException.class,
                    () -> new PersonName("Juan", null)
            );
            assertNotNull(exception);
        }

        @Test
        @DisplayName("Should throw NPE when both names are null")
        void shouldThrowNpeWhenBothNamesAreNull() {
            // When/Then
            NullPointerException exception = assertThrows(
                    NullPointerException.class,
                    () -> new PersonName(null, null)
            );
            assertNotNull(exception);
        }
    }

    @Nested
    @DisplayName("Blank Validations")
    class BlankValidationTests {
        @ParameterizedTest
        @ValueSource(strings = {"", "   ", "\t", "\n", "  \t\n  "})
        @DisplayName("Should reject blank firstName")
        void shouldRejectBlankFirstName(String blankFirstName) {
            // When/Then
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> new PersonName(blankFirstName, "Pérez")
            );
            assertEquals("First name cannot be blank.", exception.getMessage());
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   ", "\t", "\n", "  \t\n  "})
        @DisplayName("Should reject blank lastName")
        void shouldRejectBlankLastName(String blankLastName) {
            // When/Then
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> new PersonName("Juan", blankLastName)
            );
            assertEquals("Last name cannot be blank.", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Length Validations")
    class LengthValidationTests {

        @Test
        @DisplayName("Should reject firstName longer than 100 characters")
        void shouldRejectFirstNameLongerThan100Characters() {
            // Given - 101 caracteres
            String tooLongFirstName = "a".repeat(101);

            // When/Then
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> new PersonName(tooLongFirstName, "Pérez")
            );
            assertEquals("First name and last name must be less than 101 characters.", exception.getMessage());
        }

        @Test
        @DisplayName("Should reject lastName longer than 100 characters")
        void shouldRejectLastNameLongerThan100Characters() {
            // Given - 101 caracteres
            String tooLongLastName = "b".repeat(101);

            // When/Then
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> new PersonName("Juan", tooLongLastName)
            );
            assertEquals("First name and last name must be less than 101 characters.", exception.getMessage());
        }

        @Test
        @DisplayName("Should reject both names longer than 100 characters")
        void shouldRejectBothNamesLongerThan100Characters() {
            // Given
            String tooLongName = "x".repeat(101);

            // When/Then
            assertThrows(
                    IllegalArgumentException.class,
                    () -> new PersonName(tooLongName, tooLongName)
            );
        }
    }

    @Nested
    @DisplayName("Character Validations")
    class CharacterValidationTests {

        @ParameterizedTest
        @ValueSource(strings = {"Juan123", "José1", "Test9"})
        @DisplayName("Should reject firstName with numbers")
        void shouldRejectFirstNameWithNumbers(String invalidFirstName) {
            // When/Then
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> new PersonName(invalidFirstName, "Pérez")
            );
            assertEquals("First name contains invalid characters.", exception.getMessage());
        }

        @ParameterizedTest
        @ValueSource(strings = {"Pérez123", "García1", "Test9"})
        @DisplayName("Should reject lastName with numbers")
        void shouldRejectLastNameWithNumbers(String invalidLastName) {
            // When/Then
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> new PersonName("Juan", invalidLastName)
            );
            assertEquals("Last name contains invalid characters.", exception.getMessage());
        }

        @ParameterizedTest
        @ValueSource(strings = {"Juan-Carlos", "María José", "O'Brien", "Jean.Paul"})
        @DisplayName("Should reject firstName with special characters")
        void shouldRejectFirstNameWithSpecialCharacters(String invalidFirstName) {
            // When/Then
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> new PersonName(invalidFirstName, "Pérez")
            );
            assertEquals("First name contains invalid characters.", exception.getMessage());
        }

        @ParameterizedTest
        @ValueSource(strings = {"Pérez-García", "O'Connor", "De La Cruz", "Smith.Jr"})
        @DisplayName("Should reject lastName with special characters")
        void shouldRejectLastNameWithSpecialCharacters(String invalidLastName) {
            // When/Then
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> new PersonName("Juan", invalidLastName)
            );
            assertEquals("Last name contains invalid characters.", exception.getMessage());
        }

        @ParameterizedTest
        @ValueSource(strings = {"Juan@", "Test!", "Name#", "Test$", "Name%"})
        @DisplayName("Should reject firstName with symbols")
        void shouldRejectFirstNameWithSymbols(String invalidFirstName) {
            // When/Then
            assertThrows(
                    IllegalArgumentException.class,
                    () -> new PersonName(invalidFirstName, "Pérez")
            );
        }

        @ParameterizedTest
        @ValueSource(strings = {"Pérez@", "Test!", "Name#", "Test$", "Name%"})
        @DisplayName("Should reject lastName with symbols")
        void shouldRejectLastNameWithSymbols(String invalidLastName) {
            // When/Then
            assertThrows(
                    IllegalArgumentException.class,
                    () -> new PersonName("Juan", invalidLastName)
            );
        }
    }

    @Nested
    @DisplayName("fullName() Method")
    class FullNameMethodTests {

        @Test
        @DisplayName("Should return full name with space separator")
        void shouldReturnFullNameWithSpaceSeparator() {
            // Given
            PersonName personName = new PersonName("Juan", "Pérez");

            // When
            String fullName = personName.fullName();

            // Then
            assertEquals("Juan Pérez", fullName);
        }

        @Test
        @DisplayName("Should concatenate first and last name correctly")
        void shouldConcatenateFirstAndLastNameCorrectly() {
            // Given
            PersonName personName = new PersonName("María", "García");

            // When
            String fullName = personName.fullName();

            // Then
            assertTrue(fullName.startsWith("María"));
            assertTrue(fullName.endsWith("García"));
            assertTrue(fullName.contains(" "));
        }

        @Test
        @DisplayName("Should return full name with single character names")
        void shouldReturnFullNameWithSingleCharacterNames() {
            // Given
            PersonName personName = new PersonName("A", "B");

            // When
            String fullName = personName.fullName();

            // Then
            assertEquals("A B", fullName);
        }

        @Test
        @DisplayName("Should return full name with international characters")
        void shouldReturnFullNameWithInternationalCharacters() {
            // Given
            PersonName personName = new PersonName("François", "Müller");

            // When
            String fullName = personName.fullName();

            // Then
            assertEquals("François Müller", fullName);
        }
    }

    @Nested
    @DisplayName("Equality and HashCode (Record)")
    class EqualityTests {

        @Test
        @DisplayName("Should be equal when firstName and lastName are equal")
        void shouldBeEqualWhenNamesAreEqual() {
            // Given
            PersonName name1 = new PersonName("Juan", "Pérez");
            PersonName name2 = new PersonName("Juan", "Pérez");

            // Then
            assertEquals(name1, name2);
            assertEquals(name2, name1); // Simetría
        }

        @Test
        @DisplayName("Should not be equal when firstName differs")
        void shouldNotBeEqualWhenFirstNameDiffers() {
            // Given
            PersonName name1 = new PersonName("Juan", "Pérez");
            PersonName name2 = new PersonName("Pedro", "Pérez");

            // Then
            assertNotEquals(name1, name2);
        }

        @Test
        @DisplayName("Should not be equal when lastName differs")
        void shouldNotBeEqualWhenLastNameDiffers() {
            // Given
            PersonName name1 = new PersonName("Juan", "Pérez");
            PersonName name2 = new PersonName("Juan", "García");

            // Then
            assertNotEquals(name1, name2);
        }

        @Test
        @DisplayName("Should have same hashCode when equal")
        void shouldHaveSameHashCodeWhenEqual() {
            // Given
            PersonName name1 = new PersonName("Juan", "Pérez");
            PersonName name2 = new PersonName("Juan", "Pérez");

            // Then
            assertEquals(name1.hashCode(), name2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal to null")
        void shouldNotBeEqualToNull() {
            // Given
            PersonName name = new PersonName("Juan", "Pérez");

            // Then
            assertNotEquals(null, name);
        }

        @Test
        @DisplayName("Should not be equal to different class")
        void shouldNotBeEqualToDifferentClass() {
            // Given
            PersonName name = new PersonName("Juan", "Pérez");
            String notAPersonName = "Juan Pérez";

            // Then
            assertNotEquals(name, notAPersonName);
        }

        @Test
        @DisplayName("Should be equal to itself (reflexivity)")
        void shouldBeEqualToItself() {
            // Given
            PersonName name = new PersonName("Juan", "Pérez");

            // Then
            assertEquals(name, name);
        }
    }

    @Nested
    @DisplayName("Immutability (Record)")
    class ImmutabilityTests {

        @Test
        @DisplayName("Should be immutable - firstName cannot be changed")
        void shouldBeImmutableFirstName() {
            // Given
            PersonName personName = new PersonName("Juan", "Pérez");
            String originalFirstName = personName.firstName();

            // Then - No existen setters, verificado por compilación
            assertEquals(originalFirstName, personName.firstName());
            assertEquals("Juan", personName.firstName());
        }

        @Test
        @DisplayName("Should be immutable - lastName cannot be changed")
        void shouldBeImmutableLastName() {
            // Given
            PersonName personName = new PersonName("Juan", "Pérez");
            String originalLastName = personName.lastName();

            // Then - No existen setters, verificado por compilación
            assertEquals(originalLastName, personName.lastName());
            assertEquals("Pérez", personName.lastName());
        }
    }

    @Nested
    @DisplayName("toString() Method (Record)")
    class ToStringTests {

        @Test
        @DisplayName("Should have meaningful toString representation")
        void shouldHaveMeaningfulToStringRepresentation() {
            // Given
            PersonName personName = new PersonName("Juan", "Pérez");

            // When
            String toString = personName.toString();

            // Then
            assertNotNull(toString);
            assertTrue(toString.contains("Juan"));
            assertTrue(toString.contains("Pérez"));
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle names with all uppercase letters")
        void shouldHandleNamesWithAllUppercaseLetters() {
            // When
            PersonName personName = new PersonName("JUAN", "PÉREZ");

            // Then
            assertEquals("JUAN", personName.firstName());
            assertEquals("PÉREZ", personName.lastName());
        }

        @Test
        @DisplayName("Should handle names with all lowercase letters")
        void shouldHandleNamesWithAllLowercaseLetters() {
            // When
            PersonName personName = new PersonName("juan", "pérez");

            // Then
            assertEquals("juan", personName.firstName());
            assertEquals("pérez", personName.lastName());
        }

        @Test
        @DisplayName("Should handle names with mixed case")
        void shouldHandleNamesWithMixedCase() {
            // When
            PersonName personName = new PersonName("JuAn", "PéReZ");


            // Then
            assertEquals("JuAn", personName.firstName());
            assertEquals("PéReZ", personName.lastName());
        }
    }
}