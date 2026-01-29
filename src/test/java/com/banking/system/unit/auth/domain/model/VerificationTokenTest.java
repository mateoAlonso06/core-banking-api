package com.banking.system.unit.auth.domain.model;

import com.banking.system.auth.domain.model.VerificationToken;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("VerificationToken Domain Entity Tests")
class VerificationTokenTest {

    @Nested
    @DisplayName("Factory Method: createNew")
    class CreateNewTests {

        @Test
        @DisplayName("Should create new verification token with null id")
        void shouldCreateNewToken_withNullId() {
            UUID userId = UUID.randomUUID();

            VerificationToken token = VerificationToken.createNew(userId);

            assertNotNull(token);
            assertNull(token.getId());
        }

        @Test
        @DisplayName("Should create new verification token with provided userId")
        void shouldCreateNewToken_withProvidedUserId() {
            UUID userId = UUID.randomUUID();

            VerificationToken token = VerificationToken.createNew(userId);

            assertEquals(userId, token.getUserId());
        }

        @Test
        @DisplayName("Should create new verification token with random token string")
        void shouldCreateNewToken_withRandomTokenString() {
            UUID userId = UUID.randomUUID();

            VerificationToken token = VerificationToken.createNew(userId);

            assertNotNull(token.getToken());
            assertFalse(token.getToken().isEmpty());
        }

        @Test
        @DisplayName("Should create new verification token with expiration 15 minutes in future")
        void shouldCreateNewToken_withExpiration15MinutesInFuture() {
            UUID userId = UUID.randomUUID();
            LocalDateTime beforeCreation = LocalDateTime.now();

            VerificationToken token = VerificationToken.createNew(userId);

            LocalDateTime afterCreation = LocalDateTime.now().plusMinutes(15);

            assertTrue(token.getExpiresAt().isAfter(beforeCreation.plusMinutes(14)));
            assertTrue(token.getExpiresAt().isBefore(afterCreation.plusMinutes(1)));
        }

        @Test
        @DisplayName("Should create new verification token with used=false")
        void shouldCreateNewToken_withUsedFalse() {
            UUID userId = UUID.randomUUID();

            VerificationToken token = VerificationToken.createNew(userId);

            assertFalse(token.isUsed());
        }

        @Test
        @DisplayName("Should create different tokens for subsequent calls")
        void shouldCreateDifferentTokens_forSubsequentCalls() {
            UUID userId = UUID.randomUUID();

            VerificationToken token1 = VerificationToken.createNew(userId);
            VerificationToken token2 = VerificationToken.createNew(userId);

            assertNotEquals(token1.getToken(), token2.getToken());
        }

        @Test
        @DisplayName("Should throw exception when userId is null")
        void shouldThrowException_whenUserIdIsNull() {
            assertThrows(NullPointerException.class, () ->
                    VerificationToken.createNew(null)
            );
        }
    }

    @Nested
    @DisplayName("Factory Method: reconstitute")
    class ReconstituteTests {

        @Test
        @DisplayName("Should reconstitute token with all provided parameters")
        void shouldReconstituteToken_withAllParameters() {
            UUID id = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            String token = UUID.randomUUID().toString();
            LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(10);
            boolean used = false;

            VerificationToken verificationToken = VerificationToken.reconstitute(
                    id, userId, token, expiresAt, used
            );

            assertNotNull(verificationToken);
            assertEquals(id, verificationToken.getId());
            assertEquals(userId, verificationToken.getUserId());
            assertEquals(token, verificationToken.getToken());
            assertEquals(expiresAt, verificationToken.getExpiresAt());
            assertEquals(used, verificationToken.isUsed());
        }

        @Test
        @DisplayName("Should reconstitute token with used=true")
        void shouldReconstituteToken_withUsedTrue() {
            VerificationToken token = VerificationToken.reconstitute(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    "test-token",
                    LocalDateTime.now().plusMinutes(10),
                    true
            );

            assertTrue(token.isUsed());
        }

        @Test
        @DisplayName("Should throw exception when userId is null")
        void shouldThrowException_whenUserIdIsNull() {
            assertThrows(NullPointerException.class, () ->
                    VerificationToken.reconstitute(
                            UUID.randomUUID(),
                            null,
                            "token",
                            LocalDateTime.now(),
                            false
                    )
            );
        }

        @Test
        @DisplayName("Should throw exception when token is null")
        void shouldThrowException_whenTokenIsNull() {
            assertThrows(NullPointerException.class, () ->
                    VerificationToken.reconstitute(
                            UUID.randomUUID(),
                            UUID.randomUUID(),
                            null,
                            LocalDateTime.now(),
                            false
                    )
            );
        }

        @Test
        @DisplayName("Should throw exception when expiresAt is null")
        void shouldThrowException_whenExpiresAtIsNull() {
            assertThrows(NullPointerException.class, () ->
                    VerificationToken.reconstitute(
                            UUID.randomUUID(),
                            UUID.randomUUID(),
                            "token",
                            null,
                            false
                    )
            );
        }
    }

    @Nested
    @DisplayName("Business Method: isExpired")
    class IsExpiredTests {

        @Test
        @DisplayName("Should return false when token is not expired")
        void shouldReturnFalse_whenTokenIsNotExpired() {
            VerificationToken token = VerificationToken.reconstitute(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    "test-token",
                    LocalDateTime.now().plusMinutes(10),
                    false
            );

            assertFalse(token.isExpired());
        }

        @Test
        @DisplayName("Should return true when token is expired")
        void shouldReturnTrue_whenTokenIsExpired() {
            VerificationToken token = VerificationToken.reconstitute(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    "test-token",
                    LocalDateTime.now().minusMinutes(1),
                    false
            );

            assertTrue(token.isExpired());
        }

        @Test
        @DisplayName("Should return true when token expiration is exactly now")
        void shouldReturnTrue_whenTokenExpirationIsExactlyNow() throws InterruptedException {
            LocalDateTime expiresAt = LocalDateTime.now();
            Thread.sleep(10); // Ensure time has passed

            VerificationToken token = VerificationToken.reconstitute(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    "test-token",
                    expiresAt,
                    false
            );

            assertTrue(token.isExpired());
        }

        @Test
        @DisplayName("Should return false when token expires in future")
        void shouldReturnFalse_whenTokenExpiresInFuture() {
            VerificationToken token = VerificationToken.reconstitute(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    "test-token",
                    LocalDateTime.now().plusDays(1),
                    false
            );

            assertFalse(token.isExpired());
        }

        @Test
        @DisplayName("Should return true when token expired long ago")
        void shouldReturnTrue_whenTokenExpiredLongAgo() {
            VerificationToken token = VerificationToken.reconstitute(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    "test-token",
                    LocalDateTime.now().minusDays(30),
                    false
            );

            assertTrue(token.isExpired());
        }
    }

    @Nested
    @DisplayName("Business Method: markUsed")
    class MarkUsedTests {

        @Test
        @DisplayName("Should mark token as used when valid and not expired")
        void shouldMarkTokenAsUsed_whenValidAndNotExpired() {
            VerificationToken token = VerificationToken.reconstitute(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    "test-token",
                    LocalDateTime.now().plusMinutes(10),
                    false
            );

            token.markUsed();

            assertTrue(token.isUsed());
        }

        @Test
        @DisplayName("Should throw exception when token is already used")
        void shouldThrowException_whenTokenIsAlreadyUsed() {
            VerificationToken token = VerificationToken.reconstitute(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    "test-token",
                    LocalDateTime.now().plusMinutes(10),
                    true
            );

            IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                    token.markUsed()
            );

            assertTrue(exception.getMessage().contains("already been used"));
        }

        @Test
        @DisplayName("Should throw exception when token is expired")
        void shouldThrowException_whenTokenIsExpired() {
            VerificationToken token = VerificationToken.reconstitute(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    "test-token",
                    LocalDateTime.now().minusMinutes(1),
                    false
            );

            IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                    token.markUsed()
            );

            assertTrue(exception.getMessage().contains("expired"));
        }

        @Test
        @DisplayName("Should throw exception when attempting to mark expired token as used")
        void shouldThrowException_whenAttemptingToMarkExpiredTokenAsUsed() {
            VerificationToken token = VerificationToken.reconstitute(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    "test-token",
                    LocalDateTime.now().minusDays(1),
                    false
            );

            assertThrows(IllegalStateException.class, token::markUsed);
            assertFalse(token.isUsed());
        }

        @Test
        @DisplayName("Should not allow marking token as used twice")
        void shouldNotAllowMarkingTokenAsUsedTwice() {
            VerificationToken token = VerificationToken.reconstitute(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    "test-token",
                    LocalDateTime.now().plusMinutes(10),
                    false
            );

            token.markUsed();

            assertThrows(IllegalStateException.class, token::markUsed);
        }
    }

    @Nested
    @DisplayName("Token Lifecycle Scenarios")
    class TokenLifecycleTests {

        @Test
        @DisplayName("Should complete normal token lifecycle: create -> verify -> mark used")
        void shouldCompleteNormalTokenLifecycle() {
            UUID userId = UUID.randomUUID();

            // Create new token
            VerificationToken token = VerificationToken.createNew(userId);
            assertFalse(token.isUsed());
            assertFalse(token.isExpired());

            // Simulate reconstitution from database (with id)
            VerificationToken reconstitutedToken = VerificationToken.reconstitute(
                    UUID.randomUUID(),
                    token.getUserId(),
                    token.getToken(),
                    token.getExpiresAt(),
                    token.isUsed()
            );

            // Mark as used
            reconstitutedToken.markUsed();
            assertTrue(reconstitutedToken.isUsed());
        }

        @Test
        @DisplayName("Should reject expired token in lifecycle")
        void shouldRejectExpiredToken_inLifecycle() {
            VerificationToken token = VerificationToken.reconstitute(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    "expired-token",
                    LocalDateTime.now().minusHours(1),
                    false
            );

            assertTrue(token.isExpired());
            assertThrows(IllegalStateException.class, token::markUsed);
        }

        @Test
        @DisplayName("Should prevent reuse of already used token")
        void shouldPreventReuseOfAlreadyUsedToken() {
            VerificationToken token = VerificationToken.reconstitute(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    "used-token",
                    LocalDateTime.now().plusMinutes(10),
                    false
            );

            token.markUsed();
            assertTrue(token.isUsed());

            assertThrows(IllegalStateException.class, token::markUsed);
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle token expiring exactly at 15 minutes")
        void shouldHandleToken_expiringExactlyAt15Minutes() {
            UUID userId = UUID.randomUUID();
            VerificationToken token = VerificationToken.createNew(userId);

            LocalDateTime expectedExpiration = LocalDateTime.now().plusMinutes(15);

            assertTrue(token.getExpiresAt().isAfter(expectedExpiration.minusSeconds(5)));
            assertTrue(token.getExpiresAt().isBefore(expectedExpiration.plusSeconds(5)));
        }

        @Test
        @DisplayName("Should handle reconstitution with past expiration date")
        void shouldHandleReconstitution_withPastExpirationDate() {
            VerificationToken token = VerificationToken.reconstitute(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    "old-token",
                    LocalDateTime.now().minusYears(1),
                    false
            );

            assertTrue(token.isExpired());
        }

        @Test
        @DisplayName("Should handle reconstitution with far future expiration")
        void shouldHandleReconstitution_withFarFutureExpiration() {
            VerificationToken token = VerificationToken.reconstitute(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    "future-token",
                    LocalDateTime.now().plusYears(10),
                    false
            );

            assertFalse(token.isExpired());
        }
    }
}
