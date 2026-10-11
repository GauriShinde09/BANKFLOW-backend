package com.app.controller;

import com.app.dto.LoanApplicationRequestDto;
import com.app.dto.LoanApplicationResponseDto;
import com.app.dto.LoanStatusResponseDto;
import com.app.service.LoanApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/loan-applications")
public class LoanApplicationController {

    private final LoanApplicationService loanApplicationService;

    public LoanApplicationController(
            LoanApplicationService loanApplicationService) {

        this.loanApplicationService = loanApplicationService;
    }

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<LoanApplicationResponseDto> applyForLoan(
            @Valid @RequestBody LoanApplicationRequestDto dto,
            Authentication authentication) {

        String username = authentication.getName();

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        loanApplicationService
                                .applyForLoan(dto, username)
                );
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<LoanApplicationResponseDto>>
    getMyApplications(Authentication authentication) {

        String username = authentication.getName();

        return ResponseEntity.ok(
                loanApplicationService
                        .getMyApplications(username)
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    public ResponseEntity<LoanApplicationResponseDto>
    getApplicationById(@PathVariable Long id) {

        return ResponseEntity.ok(
                loanApplicationService
                        .getApplicationById(id)
        );
    }
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LoanApplicationResponseDto> approveLoan(
            @PathVariable Long id,
            @RequestParam BigDecimal approvedAmount) {

        return ResponseEntity.ok(
                loanApplicationService.approveLoan(
                        id,
                        approvedAmount
                )
        );
    }
    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LoanApplicationResponseDto> rejectLoan(
            @PathVariable Long id,
            @RequestParam String rejectionReason) {

        return ResponseEntity.ok(
                loanApplicationService.rejectLoan(
                        id,
                        rejectionReason
                )
        );
    }
    @PutMapping("/{id}/disburse")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LoanApplicationResponseDto> disburseLoan(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                loanApplicationService.disburseLoan(id)
        );
    }
    @GetMapping("/{id}/status")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<LoanStatusResponseDto> getLoanStatus(
            @PathVariable Long id,
            Authentication authentication) {

        return ResponseEntity.ok(
                loanApplicationService.getLoanStatus(
                        id,
                        authentication.getName()
                )
        );
    }
    @GetMapping("/history")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<LoanApplicationResponseDto>> getLoanHistory(
            Authentication authentication) {

        return ResponseEntity.ok(
                loanApplicationService.getLoanHistory(
                        authentication.getName()
                )
        );
        
    }
    
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<LoanApplicationResponseDto>>
    getAllApplicationsForAdmin() {

        return ResponseEntity.ok(
                loanApplicationService.getAllApplicationsForAdmin()
        );
    }
}