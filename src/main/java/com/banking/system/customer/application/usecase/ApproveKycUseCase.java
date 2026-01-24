package com.banking.system.customer.application.usecase;

import java.util.UUID;

public interface ApproveKycUseCase {
    void approveKyc(UUID customerId);
}
