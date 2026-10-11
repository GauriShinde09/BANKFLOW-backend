package com.app.service;

import com.app.dto.LoanApplicationRequestDto;
import com.app.dto.LoanEligibilityResponseDto;

public interface LoanEligibilityService {

    LoanEligibilityResponseDto checkEligibility(
            LoanApplicationRequestDto dto
    );
}