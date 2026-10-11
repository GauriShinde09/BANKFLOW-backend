package com.app.dto;

import com.app.enums.LoanApplicationStatus;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class LoanStatusResponseDto {

    private Long loanApplicationId;

    private LoanApplicationStatus status;

    private BigDecimal requestedAmount;

    private BigDecimal approvedAmount;

    private BigDecimal disbursedAmount;
}