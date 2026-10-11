package com.app.controller;

import com.app.dto.LoanTypeRequestDto;
import com.app.dto.LoanTypeResponseDto;
import com.app.service.LoanTypeService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/loan-types")
public class LoanTypeController {

    private final LoanTypeService loanTypeService;

    public LoanTypeController(
            LoanTypeService loanTypeService) {

        this.loanTypeService = loanTypeService;
    }

    // Admin: create a loan product
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LoanTypeResponseDto> createLoanType(
            @Valid @RequestBody LoanTypeRequestDto dto) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(loanTypeService.createLoanType(dto));
    }

    // Retrieve all loan products
    @GetMapping
    public ResponseEntity<List<LoanTypeResponseDto>> getAllLoanTypes() {

        return ResponseEntity.ok(
                loanTypeService.getAllLoanTypes()
        );
    }

    // Retrieve one loan product
    @GetMapping("/{id}")
    public ResponseEntity<LoanTypeResponseDto> getLoanTypeById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                loanTypeService.getLoanTypeById(id)
        );
    }

    // Admin: edit a loan product
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LoanTypeResponseDto> updateLoanType(
            @PathVariable Long id,
            @Valid @RequestBody LoanTypeRequestDto dto) {

        return ResponseEntity.ok(
                loanTypeService.updateLoanType(id, dto)
        );
    }

    // Admin: activate or deactivate a loan product
    @PatchMapping("/{id}/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LoanTypeResponseDto> setLoanTypeActive(
            @PathVariable Long id,
            @RequestParam boolean active) {

        return ResponseEntity.ok(
                loanTypeService.setLoanTypeActive(id, active)
        );
    }
}