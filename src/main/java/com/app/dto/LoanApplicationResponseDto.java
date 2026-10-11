package com.app.dto;

import com.app.enums.LoanApplicationStatus;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
public class LoanApplicationResponseDto {

    private Long id;

    private Integer applicantId;

    private Long loanTypeId;

    private String loanTypeName;

    private BigDecimal requestedAmount;

    private Integer requestedTenureMonths;

    private BigDecimal approvedAmount;

    private BigDecimal interestRate;

    private LoanApplicationStatus status;

    private LocalDateTime appliedAt;

    private LocalDateTime reviewedAt;

    private String rejectionReason;
    private BigDecimal disbursedAmount;

    private LocalDateTime disbursedAt;
    private BigDecimal emiAmount;
    private BigDecimal totalInterest;
    private BigDecimal totalRepayment;
}