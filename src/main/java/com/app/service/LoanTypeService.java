package com.app.service;

import com.app.dto.LoanTypeRequestDto;
import com.app.dto.LoanTypeResponseDto;

import java.util.List;

public interface LoanTypeService {

    LoanTypeResponseDto createLoanType(LoanTypeRequestDto dto);

    List<LoanTypeResponseDto> getAllLoanTypes();

    LoanTypeResponseDto getLoanTypeById(Long id);

    LoanTypeResponseDto updateLoanType(
            Long id,
            LoanTypeRequestDto dto
    );

    LoanTypeResponseDto setLoanTypeActive(
            Long id,
            boolean active
    );
}