package com.app.controller;

import com.app.dto.InterestCalculationResponseDto;
import com.app.service.InterestCalculationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/interest")
public class InterestCalculationController {

    private final InterestCalculationService interestCalculationService;

    public InterestCalculationController(
            InterestCalculationService interestCalculationService) {

        this.interestCalculationService =
                interestCalculationService;
    }

    @GetMapping("/calculate")
    public ResponseEntity<InterestCalculationResponseDto>
    calculateInterest(
            @RequestParam BigDecimal amount,
            @RequestParam BigDecimal interestRate,
            @RequestParam Integer tenureMonths) {

        return ResponseEntity.ok(
                interestCalculationService.calculateInterest(
                        amount,
                        interestRate,
                        tenureMonths
                )
        );
    }
}