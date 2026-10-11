package com.app.service;

import com.app.dto.InterestCalculationResponseDto;

import java.math.BigDecimal;

public interface InterestCalculationService {

    InterestCalculationResponseDto calculateInterest(
            BigDecimal amount,
            BigDecimal interestRate,
            Integer tenureMonths
    );
}