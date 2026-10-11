package com.app.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class InterestCalculationResponseDto {

    private BigDecimal principalAmount;
    private BigDecimal interestRate;
    private Integer tenureMonths;
    private BigDecimal interestAmount;
    private BigDecimal totalAmount;
}