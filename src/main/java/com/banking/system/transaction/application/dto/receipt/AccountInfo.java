package com.banking.system.transaction.application.dto.receipt;

/**
 * Masked account information for transfer receipts.
 * Contains only necessary info for displaying on vouchers/confirmations.
 */
public record AccountInfo(
        String alias,
        String partialAccountNumber  // e.g., "****1234"
) {
}