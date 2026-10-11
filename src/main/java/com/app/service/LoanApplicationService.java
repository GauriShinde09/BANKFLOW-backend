package com.app.service;

import com.app.dto.LoanApplicationRequestDto;
import com.app.dto.LoanApplicationResponseDto;
import com.app.dto.LoanStatusResponseDto;

import java.math.BigDecimal;
import java.util.List;

public interface LoanApplicationService {

    LoanApplicationResponseDto applyForLoan(
            LoanApplicationRequestDto dto,
            String username
    );

    List<LoanApplicationResponseDto> getMyApplications(
            String username
    );

    LoanApplicationResponseDto getApplicationById(Long id);
    LoanApplicationResponseDto rejectLoan(
            Long id,
            String rejectionReason
    );
    LoanApplicationResponseDto approveLoan(
            Long id,
            BigDecimal approvedAmount
    );
    LoanApplicationResponseDto disburseLoan(Long id);

    LoanStatusResponseDto getLoanStatus(
            Long loanId,
            String username
    );
    List<LoanApplicationResponseDto> getLoanHistory(String username);

    List<LoanApplicationResponseDto> getAllApplicationsForAdmin();

}