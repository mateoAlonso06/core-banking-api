package com.banking.system.account.application.dto.result;

public record AccountPublicResult(
        String alias,
        String ownerName,
        String ownerDocumentNumber,
        String currency,
        String accountType
) {
}
