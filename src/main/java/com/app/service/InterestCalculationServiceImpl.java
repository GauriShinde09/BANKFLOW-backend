package com.app.service;

import com.app.dto.InterestCalculationResponseDto;
import com.app.exception.BadRequestException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class InterestCalculationServiceImpl
        implements InterestCalculationService {

    @Override
    public InterestCalculationResponseDto calculateInterest(
            BigDecimal amount,
            BigDecimal interestRate,
            Integer tenureMonths) {

        // Validate amount
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException(
                    "Amount must be greater than 0"
            );
        }

        // Validate interest rate
        if (interestRate == null
                || interestRate.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException(
                    "Interest rate must be greater than 0"
            );
        }

        // Validate tenure
        if (tenureMonths == null || tenureMonths <= 0) {
            throw new BadRequestException(
                    "Tenure must be greater than 0"
            );
        }

        BigDecimal years = BigDecimal.valueOf(tenureMonths)
                .divide(
                        BigDecimal.valueOf(12),
                        2,
                        RoundingMode.HALF_UP
                );

        BigDecimal interest = amount
                .multiply(interestRate)
                .multiply(years)
                .divide(
                        BigDecimal.valueOf(100),
                        2,
                        RoundingMode.HALF_UP
                );

        BigDecimal totalAmount = amount.add(interest);

        InterestCalculationResponseDto response =
                new InterestCalculationResponseDto();

        response.setPrincipalAmount(amount);
        response.setInterestRate(interestRate);
        response.setTenureMonths(tenureMonths);
        response.setInterestAmount(interest);
        response.setTotalAmount(totalAmount);

        return response;
    }
}