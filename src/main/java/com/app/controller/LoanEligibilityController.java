package com.app.controller;

import com.app.dto.LoanApplicationRequestDto;
import com.app.dto.LoanEligibilityResponseDto;
import com.app.service.LoanEligibilityService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/loan-eligibility")
public class LoanEligibilityController {

    private final LoanEligibilityService loanEligibilityService;

    public LoanEligibilityController(
            LoanEligibilityService loanEligibilityService) {

        this.loanEligibilityService =
                loanEligibilityService;
    }

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<LoanEligibilityResponseDto>
    checkEligibility(
            @Valid @RequestBody LoanApplicationRequestDto dto) {

        return ResponseEntity.ok(
                loanEligibilityService.checkEligibility(dto)
        );
    }
}