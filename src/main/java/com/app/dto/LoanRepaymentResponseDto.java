package com.app.dto;

import com.app.enums.LoanRepaymentStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class LoanRepaymentResponseDto {

    private Long id;
    private Long loanApplicationId;
    private Integer installmentNumber;

    private LocalDate dueDate;
    private BigDecimal amount;

    private BigDecimal principalComponent;
    private BigDecimal interestComponent;
    private BigDecimal remainingPrincipal;

    private LocalDateTime paymentDate;
    private LoanRepaymentStatus status;
}