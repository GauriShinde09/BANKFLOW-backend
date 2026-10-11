package com.app.service;

import com.app.dto.LoanRepaymentRequestDto;
import com.app.dto.LoanRepaymentResponseDto;

import java.util.List;

public interface LoanRepaymentService {

    LoanRepaymentResponseDto makeRepayment(
            Long loanId,
            LoanRepaymentRequestDto dto,
            String username
    );

    List<LoanRepaymentResponseDto> getRepaymentSchedule(
            Long loanId,
            String username
    );

    void recalculateRepaymentComponents(Long loanId);
}