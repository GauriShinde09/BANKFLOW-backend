package com.app.controller;

import com.app.dto.LoanRepaymentRequestDto;
import com.app.dto.LoanRepaymentResponseDto;
import com.app.service.LoanRepaymentService;

import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/loan-repayments")
public class LoanRepaymentController {

    private final LoanRepaymentService loanRepaymentService;

    public LoanRepaymentController(
            LoanRepaymentService loanRepaymentService) {

        this.loanRepaymentService = loanRepaymentService;
    }

    @PostMapping("/{loanId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<LoanRepaymentResponseDto> makeRepayment(
            @PathVariable Long loanId,
            @Valid @RequestBody LoanRepaymentRequestDto dto,
            Authentication authentication) {

        return ResponseEntity.ok(
                loanRepaymentService.makeRepayment(
                        loanId,
                        dto,
                        authentication.getName()
                )
        );
    }
    @GetMapping("/{loanId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<LoanRepaymentResponseDto>>
    getRepaymentSchedule(
            @PathVariable Long loanId,
            Authentication authentication) {

        return ResponseEntity.ok(
                loanRepaymentService.getRepaymentSchedule(
                        loanId,
                        authentication.getName()
                )
        );
    }
    @PostMapping("/{loanId}/recalculate-components")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>>
    recalculateRepaymentComponents(
            @PathVariable Long loanId) {

        loanRepaymentService.recalculateRepaymentComponents(loanId);

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Repayment components recalculated successfully"
                )
        );
    }
    
}