package com.banking.system.customer.application.usecase;

import com.banking.system.common.domain.PageRequest;
import com.banking.system.common.domain.dto.PagedResult;
import com.banking.system.customer.application.dto.result.CustomerResult;

public interface GetAllCustomerUseCase {
    PagedResult<CustomerResult> getAllCustomers(PageRequest pageRequest);
}
