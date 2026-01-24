package com.banking.system.customer.application.usecase;

import java.util.UUID;

public interface RejectKycUseCase {
    void rejectKyc(UUID customerId);
}
