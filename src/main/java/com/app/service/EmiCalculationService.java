package com.app.service;

import java.math.BigDecimal;

import com.app.dto.EmiCalculationResponseDto;

public interface EmiCalculationService {
	  EmiCalculationResponseDto calculateEmi(
	            BigDecimal loanAmount,
	            BigDecimal interestRate,
	            Integer tenureMonths
	    );
}
