package com.app.service;

import com.app.dto.LoanApplicationRequestDto;
import com.app.dto.LoanEligibilityResponseDto;
import com.app.entity.LoanType;
import com.app.exception.BadRequestException;
import com.app.exception.ResourceNotFoundException;
import com.app.repo.LoanTypeRepository;
import org.springframework.stereotype.Service;

@Service
public class LoanEligibilityServiceImpl
        implements LoanEligibilityService {

    private final LoanTypeRepository loanTypeRepository;

    public LoanEligibilityServiceImpl(
            LoanTypeRepository loanTypeRepository) {

        this.loanTypeRepository = loanTypeRepository;
    }

    @Override
    public LoanEligibilityResponseDto checkEligibility(
            LoanApplicationRequestDto dto) {

        LoanEligibilityResponseDto response =
                new LoanEligibilityResponseDto();

        LoanType loanType = loanTypeRepository
                .findById(dto.getLoanTypeId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Loan type not found"
                        ));

        if (!loanType.getActive()) {

            throw new BadRequestException(
                    "This loan type is currently inactive"
            );
        }

        if (dto.getRequestedAmount()
                .compareTo(loanType.getMinAmount()) < 0) {

            throw new BadRequestException(
                    "Requested amount is below minimum loan amount"
            );
        }

        if (dto.getRequestedAmount()
                .compareTo(loanType.getMaxAmount()) > 0) {

            throw new BadRequestException(
                    "Requested amount exceeds maximum loan amount"
            );
        }

        if (dto.getRequestedTenureMonths()
                > loanType.getMaxTenureMonths()) {

            throw new BadRequestException(
                    "Requested tenure exceeds maximum allowed tenure"
            );
        }

        response.setEligible(true);
        response.setMessage(
                "You are eligible for this loan"
        );

        return response;
    }
}