package com.app.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class LoanApplicationRequestDto {

    @NotNull(message = "Loan type is required")
    private Long loanTypeId;

    @NotNull(message = "Loan amount is required")
    @DecimalMin(
            value = "0.0",
            inclusive = false,
            message = "Loan amount must be greater than 0"
    )
    private BigDecimal requestedAmount;

    @NotNull(message = "Loan tenure is required")
    @Min(
            value = 1,
            message = "Loan tenure must be at least 1 month"
    )
    private Integer requestedTenureMonths;

    @NotNull(message = "Monthly income is required")
    @DecimalMin(
            value = "0.0",
            inclusive = false,
            message = "Monthly income must be greater than 0"
    )
    private BigDecimal monthlyIncome;
}