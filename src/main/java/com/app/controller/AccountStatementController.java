
package com.app.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.service.AccountStatementService;

@RestController
@RequestMapping("/account-statement")
public class AccountStatementController {

    private final AccountStatementService statementService;

    public AccountStatementController(
            AccountStatementService statementService) {
        this.statementService = statementService;
    }

    @GetMapping(
            value = "/pdf",
            produces = MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<byte[]> downloadStatement(
            Authentication authentication) {

        String username = authentication.getName();

        byte[] pdf =
                statementService.generatePdf(username);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"BANKFLOW-Account-Statement.pdf\"")
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(pdf);
    }
}
