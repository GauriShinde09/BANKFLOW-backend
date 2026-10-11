package com.app.controller;

import com.app.dto.EmiCalculationResponseDto;
import com.app.service.EmiCalculationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/emi")
public class EmiCalculationController {

    private final EmiCalculationService emiCalculationService;

    public EmiCalculationController(
            EmiCalculationService emiCalculationService) {

        this.emiCalculationService =
                emiCalculationService;
    }

    @GetMapping("/calculate")
    public ResponseEntity<EmiCalculationResponseDto> calculateEmi(
            @RequestParam BigDecimal loanAmount,
            @RequestParam BigDecimal interestRate,
            @RequestParam Integer tenureMonths) {

        return ResponseEntity.ok(
                emiCalculationService.calculateEmi(
                        loanAmount,
                        interestRate,
                        tenureMonths
                )
        );
    }
}