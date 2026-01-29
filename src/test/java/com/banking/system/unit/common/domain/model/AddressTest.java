package com.banking.system.unit.common.domain.model;

import com.banking.system.common.domain.Address;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Address Value Object Tests")
class AddressTest {

    @Nested
    @DisplayName("Valid Creation Tests")
    class ValidCreationTests {

        @Test
        @DisplayName("Should create valid address with simple data")
        void shouldCreateValidAddressWithSimpleData() {
            Address address = new Address("Av. Corrientes 1234", "Buenos Aires", "AR");

            assertNotNull(address);
            assertEquals("Av. Corrientes 1234", address.address());
            assertEquals("Buenos Aires", address.city());
            assertEquals("AR", address.country());
        }

        @Test
        @DisplayName("Should normalize country code to uppercase")
        void shouldNormalizeCountryCodeToUppercase() {
            Address address = new Address("123 Main Street", "New York", "us");

            assertEquals("US", address.country());
        }

        @Test
        @DisplayName("Should trim whitespace from all fields")
        void shouldTrimWhitespaceFromAllFields() {
            Address address = new Address("  123 Main Street  ", "  New York  ", "  US  ");

            assertEquals("123 Main Street", address.address());
            assertEquals("New York", address.city());
            assertEquals("US", address.country());
        }

        @Test
        @DisplayName("Should create address with minimum valid lengths")
        void shouldCreateAddressWithMinimumValidLengths() {
            Address address = new Address("12345", "NY", "AR");

            assertEquals(5, address.address().length());
            assertEquals(2, address.city().length());
        }

        @Test
        @DisplayName("Should create address with maximum valid lengths")
        void shouldCreateAddressWithMaximumValidLengths() {
            String maxAddress = "a".repeat(200);
            String maxCity = "b".repeat(100);

            Address address = new Address(maxAddress, maxCity, "AR");

            assertEquals(200, address.address().length());
            assertEquals(100, address.city().length());
        }

        @Test
        @DisplayName("Should accept various valid ISO country codes")
        void shouldAcceptVariousValidIsoCountryCodes() {
            assertDoesNotThrow(() -> new Address("Address 12345", "City Name", "AR"));
            assertDoesNotThrow(() -> new Address("Address 12345", "City Name", "US"));
            assertDoesNotThrow(() -> new Address("Address 12345", "City Name", "BR"));
            assertDoesNotThrow(() -> new Address("Address 12345", "City Name", "MX"));
            assertDoesNotThrow(() -> new Address("Address 12345", "City Name", "ES"));
        }
    }

    @Nested
    @DisplayName("Null Validation Tests")
    class NullValidationTests {

        @Test
        @DisplayName("Should throw NPE when address is null")
        void shouldThrowNpeWhenAddressIsNull() {
            NullPointerException exception = assertThrows(
                    NullPointerException.class,
                    () -> new Address(null, "Buenos Aires", "AR")
            );
            assertEquals("Address cannot be null", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw NPE when city is null")
        void shouldThrowNpeWhenCityIsNull() {
            NullPointerException exception = assertThrows(
                    NullPointerException.class,
                    () -> new Address("Av. Corrientes 1234", null, "AR")
            );
            assertEquals("City cannot be null", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw NPE when country is null")
        void shouldThrowNpeWhenCountryIsNull() {
            NullPointerException exception = assertThrows(
                    NullPointerException.class,
                    () -> new Address("Av. Corrientes 1234", "Buenos Aires", null)
            );
            assertEquals("Country code cannot be null", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Blank Validation Tests")
    class BlankValidationTests {

        @ParameterizedTest
        @ValueSource(strings = {"", "   ", "\t", "\n", "  \t\n  "})
        @DisplayName("Should reject blank address")
        void shouldRejectBlankAddress(String blankAddress) {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> new Address(blankAddress, "Buenos Aires", "AR")
            );
            assertEquals("Address cannot be blank", exception.getMessage());
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   ", "\t", "\n", "  \t\n  "})
        @DisplayName("Should reject blank city")
        void shouldRejectBlankCity(String blankCity) {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> new Address("Av. Corrientes 1234", blankCity, "AR")
            );
            assertEquals("City cannot be blank", exception.getMessage());
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   ", "\t", "\n", "  \t\n  "})
        @DisplayName("Should reject blank country")
        void shouldRejectBlankCountry(String blankCountry) {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> new Address("Av. Corrientes 1234", "Buenos Aires", blankCountry)
            );
            assertEquals("Country code cannot be blank", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Country Code Format Validation Tests")
    class CountryCodeFormatTests {

        @ParameterizedTest
        @ValueSource(strings = {"A", "ARG", "ARGE", "ARGENTINA"})
        @DisplayName("Should reject country code not exactly 2 characters")
        void shouldRejectCountryCodeNotExactly2Characters(String invalidCountry) {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> new Address("Av. Corrientes 1234", "Buenos Aires", invalidCountry)
            );
            assertTrue(exception.getMessage().contains("Country code must be exactly 2 characters"));
        }

        @ParameterizedTest
        @ValueSource(strings = {"12", "A1", "1A", "@#"})
        @DisplayName("Should reject country code with non-alphabetic characters")
        void shouldRejectCountryCodeWithNonAlphabeticCharacters(String invalidCountry) {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> new Address("Av. Corrientes 1234", "Buenos Aires", invalidCountry)
            );
            assertTrue(exception.getMessage().contains("Country code must contain only letters"));
        }

        @ParameterizedTest
        @ValueSource(strings = {"XX", "ZZ", "QQ", "AA"})
        @DisplayName("Should reject invalid ISO country codes")
        void shouldRejectInvalidIsoCountryCodes(String invalidCountry) {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> new Address("Av. Corrientes 1234", "Buenos Aires", invalidCountry)
            );
            assertTrue(exception.getMessage().contains("Invalid country code"));
        }
    }

    @Nested
    @DisplayName("Length Validation Tests")
    class LengthValidationTests {

        @Test
        @DisplayName("Should reject address shorter than 5 characters")
        void shouldRejectAddressShorterThan5Characters() {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> new Address("1234", "Buenos Aires", "AR")
            );
            assertTrue(exception.getMessage().contains("Address must be between 5 and 200 characters"));
        }

        @Test
        @DisplayName("Should reject address longer than 200 characters")
        void shouldRejectAddressLongerThan200Characters() {
            String tooLongAddress = "a".repeat(201);

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> new Address(tooLongAddress, "Buenos Aires", "AR")
            );
            assertTrue(exception.getMessage().contains("Address must be between 5 and 200 characters"));
        }

        @Test
        @DisplayName("Should reject city shorter than 2 characters")
        void shouldRejectCityShorterThan2Characters() {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> new Address("Av. Corrientes 1234", "B", "AR")
            );
            assertTrue(exception.getMessage().contains("City must be between 2 and 100 characters"));
        }

        @Test
        @DisplayName("Should reject city longer than 100 characters")
        void shouldRejectCityLongerThan100Characters() {
            String tooLongCity = "b".repeat(101);

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> new Address("Av. Corrientes 1234", tooLongCity, "AR")
            );
            assertTrue(exception.getMessage().contains("City must be between 2 and 100 characters"));
        }
    }

    @Nested
    @DisplayName("countryName() Method Tests")
    class CountryNameMethodTests {

        @Test
        @DisplayName("Should return full country name for AR")
        void shouldReturnFullCountryNameForAR() {
            Address address = new Address("Av. Corrientes 1234", "Buenos Aires", "AR");

            assertEquals("Argentina", address.countryName());
        }

        @Test
        @DisplayName("Should return full country name for US")
        void shouldReturnFullCountryNameForUS() {
            Address address = new Address("123 Main Street", "New York", "US");

            assertEquals("United States", address.countryName());
        }

        @Test
        @DisplayName("Should return full country name for BR")
        void shouldReturnFullCountryNameForBR() {
            Address address = new Address("Rua Principal 100", "São Paulo", "BR");

            assertEquals("Brazil", address.countryName());
        }
    }

    @Nested
    @DisplayName("fullAddress() Method Tests")
    class FullAddressMethodTests {

        @Test
        @DisplayName("Should return formatted full address")
        void shouldReturnFormattedFullAddress() {
            Address address = new Address("Av. Corrientes 1234", "Buenos Aires", "AR");

            String fullAddress = address.fullAddress();

            assertEquals("Av. Corrientes 1234, Buenos Aires, Argentina (AR)", fullAddress);
        }

        @Test
        @DisplayName("Should include all components in full address")
        void shouldIncludeAllComponentsInFullAddress() {
            Address address = new Address("123 Main Street", "New York", "US");

            String fullAddress = address.fullAddress();

            assertTrue(fullAddress.contains("123 Main Street"));
            assertTrue(fullAddress.contains("New York"));
            assertTrue(fullAddress.contains("United States"));
            assertTrue(fullAddress.contains("(US)"));
        }
    }

    @Nested
    @DisplayName("Equality and HashCode (Record) Tests")
    class EqualityTests {

        @Test
        @DisplayName("Should be equal when all fields are equal")
        void shouldBeEqualWhenAllFieldsAreEqual() {
            Address address1 = new Address("Av. Corrientes 1234", "Buenos Aires", "AR");
            Address address2 = new Address("Av. Corrientes 1234", "Buenos Aires", "AR");

            assertEquals(address1, address2);
            assertEquals(address2, address1);
        }

        @Test
        @DisplayName("Should not be equal when address differs")
        void shouldNotBeEqualWhenAddressDiffers() {
            Address address1 = new Address("Av. Corrientes 1234", "Buenos Aires", "AR");
            Address address2 = new Address("Av. Santa Fe 5678", "Buenos Aires", "AR");

            assertNotEquals(address1, address2);
        }

        @Test
        @DisplayName("Should not be equal when city differs")
        void shouldNotBeEqualWhenCityDiffers() {
            Address address1 = new Address("Av. Corrientes 1234", "Buenos Aires", "AR");
            Address address2 = new Address("Av. Corrientes 1234", "Córdoba", "AR");

            assertNotEquals(address1, address2);
        }

        @Test
        @DisplayName("Should not be equal when country differs")
        void shouldNotBeEqualWhenCountryDiffers() {
            Address address1 = new Address("Av. Corrientes 1234", "Buenos Aires", "AR");
            Address address2 = new Address("Av. Corrientes 1234", "Buenos Aires", "BR");

            assertNotEquals(address1, address2);
        }

        @Test
        @DisplayName("Should have same hashCode when equal")
        void shouldHaveSameHashCodeWhenEqual() {
            Address address1 = new Address("Av. Corrientes 1234", "Buenos Aires", "AR");
            Address address2 = new Address("Av. Corrientes 1234", "Buenos Aires", "AR");

            assertEquals(address1.hashCode(), address2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal to null")
        void shouldNotBeEqualToNull() {
            Address address = new Address("Av. Corrientes 1234", "Buenos Aires", "AR");

            assertNotEquals(null, address);
        }

        @Test
        @DisplayName("Should be equal to itself (reflexivity)")
        void shouldBeEqualToItself() {
            Address address = new Address("Av. Corrientes 1234", "Buenos Aires", "AR");

            assertEquals(address, address);
        }
    }

    @Nested
    @DisplayName("Immutability (Record) Tests")
    class ImmutabilityTests {

        @Test
        @DisplayName("Should be immutable - fields cannot be changed")
        void shouldBeImmutableFieldsCannotBeChanged() {
            Address address = new Address("Av. Corrientes 1234", "Buenos Aires", "AR");

            String originalAddress = address.address();
            String originalCity = address.city();
            String originalCountry = address.country();

            assertEquals(originalAddress, address.address());
            assertEquals(originalCity, address.city());
            assertEquals(originalCountry, address.country());
        }
    }

    @Nested
    @DisplayName("toString() Method (Record) Tests")
    class ToStringTests {

        @Test
        @DisplayName("Should have meaningful toString representation")
        void shouldHaveMeaningfulToStringRepresentation() {
            Address address = new Address("Av. Corrientes 1234", "Buenos Aires", "AR");

            String toString = address.toString();

            assertNotNull(toString);
            assertTrue(toString.contains("Av. Corrientes 1234"));
            assertTrue(toString.contains("Buenos Aires"));
            assertTrue(toString.contains("AR"));
        }
    }
}
