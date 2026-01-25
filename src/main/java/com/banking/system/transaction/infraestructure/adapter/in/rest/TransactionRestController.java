package com.banking.system.transaction.infraestructure.adapter.in.rest;

import com.banking.system.common.domain.PageRequest;
import com.banking.system.common.domain.dto.PagedResult;
import com.banking.system.transaction.application.dto.result.TransactionResult;
import com.banking.system.transaction.application.usecase.DepositUseCase;
import com.banking.system.transaction.application.usecase.GetAllTransactionsByAccountUseCase;
import com.banking.system.transaction.application.usecase.WithdrawUseCase;
import com.banking.system.transaction.infraestructure.adapter.in.rest.dto.request.DepositMoneyRequest;
import com.banking.system.transaction.infraestructure.adapter.in.rest.dto.request.WithdrawMoneyRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionRestController {
    private final DepositUseCase depositUseCase;
    private final WithdrawUseCase withdrawUseCase;
    private final GetAllTransactionsByAccountUseCase getAllTransactionsByAccountUseCase;

    @PostMapping("/accounts/{accountId}/deposits")
    public ResponseEntity<Void> createDeposit(@RequestBody @Valid DepositMoneyRequest request,
                                              @PathVariable UUID accountId,
                                              @AuthenticationPrincipal UUID userId) {
        var command = request.toCommand();
        depositUseCase.deposit(command, accountId, userId);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/accounts/{accountId}/withdrawals")
    public ResponseEntity<Void> withdrawMoney(@RequestBody @Valid WithdrawMoneyRequest request,
                                              @PathVariable UUID accountId,
                                              @AuthenticationPrincipal UUID userId) {
        var command = request.toCommand();
        withdrawUseCase.withdraw(command, accountId, userId);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/accounts/{accountId}/transactions")
    public ResponseEntity<PagedResult<TransactionResult>> getAllTransactionsByAccount(
            @PathVariable UUID accountId,
            @AuthenticationPrincipal UUID userId,
            Pageable pageable) {
        var pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
        var result = getAllTransactionsByAccountUseCase.getAllTransactionsByAccountId(accountId, userId, pageRequest);
        return ResponseEntity.ok(result);
    }
}
