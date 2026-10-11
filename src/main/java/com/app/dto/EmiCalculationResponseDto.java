package com.app.dto;

import java.math.BigDecimal;

import lombok.Data;
@Data
public class EmiCalculationResponseDto {
	 private BigDecimal loanAmount;

	    private BigDecimal interestRate;

	    private Integer tenureMonths;

	    private BigDecimal monthlyEmi;

	    private BigDecimal totalAmount;

	    private BigDecimal totalInterest;
}
