package com.app.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.dto.AccountStatusResponseDto;
import com.app.service.BankAccountService;

@RestController
@RequestMapping("/admin/accounts")
public class AdminAccountController {

    private final BankAccountService bankAccountService;

    public AdminAccountController(
            BankAccountService bankAccountService) {
        this.bankAccountService = bankAccountService;
    }

    // View all customer accounts and their status
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<AccountStatusResponseDto> getAllAccounts() {

        return bankAccountService.getAllAccountStatuses();
    }

    // Freeze an account
    @PatchMapping("/{accountId}/freeze")
    @PreAuthorize("hasRole('ADMIN')")
    public AccountStatusResponseDto freezeAccount(
            @PathVariable Integer accountId) {

        return bankAccountService.freezeAccount(accountId);
    }

    // Unfreeze an account
    @PatchMapping("/{accountId}/unfreeze")
    @PreAuthorize("hasRole('ADMIN')")
    public AccountStatusResponseDto unfreezeAccount(
            @PathVariable Integer accountId) {

        return bankAccountService.unfreezeAccount(accountId);
    }
}