package com.app.service;

import com.app.dto.EmiCalculationResponseDto;
import com.app.exception.BadRequestException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class EmiCalculationServiceImpl
        implements EmiCalculationService {

    @Override
    public EmiCalculationResponseDto calculateEmi(
            BigDecimal loanAmount,
            BigDecimal interestRate,
            Integer tenureMonths) {

        // Validate loan amount
        if (loanAmount == null
                || loanAmount.compareTo(BigDecimal.ZERO) <= 0) {

            throw new BadRequestException(
                    "Loan amount must be greater than 0"
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

        BigDecimal monthlyRate = interestRate
                .divide(
                        BigDecimal.valueOf(12 * 100),
                        10,
                        RoundingMode.HALF_UP
                );

        BigDecimal onePlusRate =
                BigDecimal.ONE.add(monthlyRate);

        BigDecimal power =
                onePlusRate.pow(tenureMonths);

        BigDecimal numerator = loanAmount
                .multiply(monthlyRate)
                .multiply(power);

        BigDecimal denominator =
                power.subtract(BigDecimal.ONE);

        BigDecimal emi = numerator
                .divide(
                        denominator,
                        2,
                        RoundingMode.HALF_UP
                );

        BigDecimal totalAmount = emi
                .multiply(BigDecimal.valueOf(tenureMonths))
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal totalInterest = totalAmount
                .subtract(loanAmount)
                .setScale(2, RoundingMode.HALF_UP);

        EmiCalculationResponseDto response =
                new EmiCalculationResponseDto();

        response.setLoanAmount(loanAmount);
        response.setInterestRate(interestRate);
        response.setTenureMonths(tenureMonths);
        response.setMonthlyEmi(emi);
        response.setTotalAmount(totalAmount);
        response.setTotalInterest(totalInterest);

        return response;
    }
}