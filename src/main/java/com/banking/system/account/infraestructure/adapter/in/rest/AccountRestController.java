package com.banking.system.account.infraestructure.adapter.in.rest;

import com.banking.system.account.application.dto.command.CreateAccountCommand;
import com.banking.system.account.application.dto.result.AccountResult;
import com.banking.system.account.application.usecase.CreateAccountUseCase;
import com.banking.system.account.application.usecase.FindAccountByIdUseCase;
import com.banking.system.account.application.usecase.FindAllAccountUseCase;
import com.banking.system.account.infraestructure.adapter.in.rest.dto.request.CreateAccountRequest;
import com.banking.system.common.domain.PageRequest;
import com.banking.system.common.domain.dto.PagedResult;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/accounts")
public class AccountRestController {

    private final CreateAccountUseCase createAccountUseCase;
    private final FindAccountByIdUseCase findAccountByIdUseCase;
    private final FindAllAccountUseCase findAllAccountUseCase;

    @PreAuthorize("hasAuthority('ACCOUNT_CREATE')")
    @PostMapping
    public ResponseEntity<AccountResult> createAccount(
            @AuthenticationPrincipal UUID userId,
            @RequestBody @Valid CreateAccountRequest request) {


        var command = new CreateAccountCommand(
                userId,
                request.accountType(),
                request.currency()
        );

        var result = createAccountUseCase.createAccount(command);

        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/api/v1/accounts/{id}")
                .buildAndExpand(result.id())
                .toUri();

        return ResponseEntity.created(location).body(result);
    }

    @PreAuthorize("hasAnyAuthority('ACCOUNT_VIEW_OWN', 'ACCOUNT_VIEW_ALL')")
    @GetMapping
    public ResponseEntity<PagedResult<AccountResult>> getAllAccounts(Pageable pageable) {
        var pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
        var results = findAllAccountUseCase.findAll(pageRequest);

        return ResponseEntity.ok().body(results);
    }

    @PreAuthorize("hasAnyAuthority('ACCOUNT_VIEW_OWN', 'ACCOUNT_VIEW')")
    @GetMapping("/{id}")
    public ResponseEntity<AccountResult> getAccountById(@PathVariable @NotNull UUID id) {
        var result = findAccountByIdUseCase.findById(id);

        return ResponseEntity.ok().body(result);
    }
}
