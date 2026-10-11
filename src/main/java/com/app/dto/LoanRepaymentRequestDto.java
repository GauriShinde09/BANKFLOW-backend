package com.app.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class LoanRepaymentRequestDto {

    @NotNull(message = "Installment number is required")
    @Positive(message = "Installment number must be greater than 0")
    private Integer installmentNumber;

    @NotNull(message = "Payment amount is required")
    @DecimalMin(
            value = "0.0",
            inclusive = false,
            message = "Payment amount must be greater than 0"
    )
    private BigDecimal amount;
}