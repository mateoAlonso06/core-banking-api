package com.banking.system.unit.common.model.vo;

import com.banking.system.common.domain.MoneyCurrency;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Currency;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MoneyCurrency Value Object Tests")
class MoneyCurrencyTest {

    @Nested
    @DisplayName("Valid Creation Tests")
    class ValidCreationTests {

        @Test
        @DisplayName("Should create valid USD currency from code")
        void shouldCreateValidUsdCurrencyFromCode() {
            MoneyCurrency currency = MoneyCurrency.ofCode("USD");

            assertNotNull(currency);
            assertEquals("USD", currency.code());
        }

        @Test
        @DisplayName("Should create valid ARS currency from code")
        void shouldCreateValidArsCurrencyFromCode() {
            MoneyCurrency currency = MoneyCurrency.ofCode("ARS");

            assertNotNull(currency);
            assertEquals("ARS", currency.code());
        }

        @Test
        @DisplayName("Should create currency from Currency instance when supported")
        void shouldCreateCurrencyFromCurrencyInstanceWhenSupported() {
            Currency javaCurrency = Currency.getInstance("USD");
            MoneyCurrency currency = MoneyCurrency.of(javaCurrency);

            assertEquals("USD", currency.code());
            assertEquals(javaCurrency, currency.value());
        }

        @Test
        @DisplayName("Should normalize code to uppercase")
        void shouldNormalizeCodeToUppercase() {
            MoneyCurrency currency = MoneyCurrency.ofCode("usd");

            assertEquals("USD", currency.code());
        }

        @Test
        @DisplayName("Should trim whitespace from code")
        void shouldTrimWhitespaceFromCode() {
            MoneyCurrency currency = MoneyCurrency.ofCode("  USD  ");

            assertEquals("USD", currency.code());
        }

        @Test
        @DisplayName("Should handle mixed case code")
        void shouldHandleMixedCaseCode() {
            MoneyCurrency currency = MoneyCurrency.ofCode("uSd");

            assertEquals("USD", currency.code());
        }
    }

    @Nested
    @DisplayName("Invalid Code Validation Tests")
    class InvalidCodeValidationTests {

        @ParameterizedTest
        @ValueSource(strings = {"AAA", "ZZZ", "QQQ", "123", "INVALID"})
        @DisplayName("Should throw IAE for syntactically invalid currency codes")
        void shouldThrowIaeForSyntacticallyInvalidCurrencyCodes(String invalidCode) {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> MoneyCurrency.ofCode(invalidCode)
            );
        }

        @ParameterizedTest
        @ValueSource(strings = {"EUR", "BRL", "CLP", "MXN"})
        @DisplayName("Should throw IAE for valid ISO codes that are not supported by application")
        void shouldThrowIaeForValidIsoCodesNotSupportedByApplication(String isoButNotSupported) {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> MoneyCurrency.ofCode(isoButNotSupported)
            );
            assertTrue(exception.getMessage().startsWith("Currency not supported:"));
        }

        @Test
        @DisplayName("Should throw IAE for empty code")
        void shouldThrowIaeForEmptyCode() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> MoneyCurrency.ofCode("")
            );
        }

        @Test
        @DisplayName("Should throw IAE for blank code")
        void shouldThrowIaeForBlankCode() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> MoneyCurrency.ofCode("   ")
            );
        }

        @Test
        @DisplayName("Should throw IAE for code with special characters")
        void shouldThrowIaeForCodeWithSpecialCharacters() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> MoneyCurrency.ofCode("US$")
            );
        }
    }

    @Nested
    @DisplayName("code() Method Tests")
    class CodeMethodTests {

        @Test
        @DisplayName("Should return correct code for USD")
        void shouldReturnCorrectCodeForUsd() {
            MoneyCurrency currency = MoneyCurrency.ofCode("USD");

            assertEquals("USD", currency.code());
        }

        @Test
        @DisplayName("Should return correct code for ARS")
        void shouldReturnCorrectCodeForArs() {
            MoneyCurrency currency = MoneyCurrency.ofCode("ARS");

            assertEquals("ARS", currency.code());
        }
    }

    @Nested
    @DisplayName("asJavaCurrency() Method Tests")
    class AsJavaCurrencyMethodTests {

        @Test
        @DisplayName("Should return Java Currency instance")
        void shouldReturnJavaCurrencyInstance() {
            MoneyCurrency currency = MoneyCurrency.ofCode("USD");

            Currency javaCurrency = currency.asJavaCurrency();

            assertNotNull(javaCurrency);
            assertEquals("USD", javaCurrency.getCurrencyCode());
        }

        @Test
        @DisplayName("Should return same instance as value()")
        void shouldReturnSameInstanceAsValue() {
            MoneyCurrency currency = MoneyCurrency.ofCode("USD");

            assertSame(currency.value(), currency.asJavaCurrency());
        }
    }

    @Nested
    @DisplayName("Equality and HashCode (Record) Tests")
    class EqualityTests {

        @Test
        @DisplayName("Should be equal when currencies are equal")
        void shouldBeEqualWhenCurrenciesAreEqual() {
            MoneyCurrency currency1 = MoneyCurrency.ofCode("USD");
            MoneyCurrency currency2 = MoneyCurrency.ofCode("USD");

            assertEquals(currency1, currency2);
            assertEquals(currency2, currency1);
        }

        @Test
        @DisplayName("Should be equal when created from same code with different case")
        void shouldBeEqualWhenCreatedFromSameCodeWithDifferentCase() {
            MoneyCurrency currency1 = MoneyCurrency.ofCode("USD");
            MoneyCurrency currency2 = MoneyCurrency.ofCode("usd");

            assertEquals(currency1, currency2);
        }

        @Test
        @DisplayName("Should not be equal when currencies differ")
        void shouldNotBeEqualWhenCurrenciesDiffer() {
            MoneyCurrency usd = MoneyCurrency.ofCode("USD");
            MoneyCurrency ars = MoneyCurrency.ofCode("ARS");

            assertNotEquals(usd, ars);
        }

        @Test
        @DisplayName("Should be equal when created from Currency instance")
        void shouldBeEqualWhenCreatedFromCurrencyInstance() {
            MoneyCurrency currency1 = MoneyCurrency.ofCode("USD");
            MoneyCurrency currency2 = MoneyCurrency.of(Currency.getInstance("USD"));

            assertEquals(currency1, currency2);
        }
    }

    @Nested
    @DisplayName("Various ISO-4217 Currencies")
    class VariousCurrenciesTests {

        @ParameterizedTest
        @ValueSource(strings = {"USD", "ARS"})
        @DisplayName("Should accept only configured supported currency codes")
        void shouldAcceptOnlyConfiguredSupportedCurrencyCodes(String code) {
            MoneyCurrency currency = MoneyCurrency.ofCode(code);

            assertNotNull(currency);
            assertEquals(code, currency.code());
        }
    }
}
