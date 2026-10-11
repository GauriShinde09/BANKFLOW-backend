package com.app.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.app.dto.AddBeneficiaryDto;
import com.app.dto.BeneficiaryResponseDto;
import com.app.service.BeneficiaryService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/beneficiary")
public class BeneficiaryController {

    @Autowired
    private BeneficiaryService beneficiaryService;

    @PostMapping("/add")
    @PreAuthorize("hasRole('CUSTOMER')")
    public BeneficiaryResponseDto addBeneficiary(
            @RequestBody @Valid AddBeneficiaryDto dto,
            Authentication authentication) {

        String username = authentication.getName();

        return beneficiaryService.addBeneficiary(
                username,
                dto
        );
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('CUSTOMER')")
    public List<BeneficiaryResponseDto> getMyBeneficiaries(
            Authentication authentication) {

        String username = authentication.getName();

        return beneficiaryService.getMyBeneficiaries(username);
    }
}