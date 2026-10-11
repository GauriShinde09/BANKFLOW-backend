package com.app.service;

import com.app.dto.LoanRepaymentRequestDto;
import com.app.dto.LoanRepaymentResponseDto;
import com.app.entity.LoanApplication;
import com.app.entity.LoanRepayment;
import com.app.enums.LoanApplicationStatus;
import com.app.enums.LoanRepaymentStatus;
import com.app.exception.BadRequestException;
import com.app.exception.ResourceNotFoundException;
import com.app.repo.LoanApplicationRepository;
import com.app.repo.LoanRepaymentRepository;

import jakarta.transaction.Transactional;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class LoanRepaymentServiceImpl implements LoanRepaymentService {

    private final LoanApplicationRepository loanApplicationRepository;
    private final LoanRepaymentRepository loanRepaymentRepository;
    private final BankAccountService bankAccountService;
    private final ModelMapper modelMapper;

    public LoanRepaymentServiceImpl(
            LoanApplicationRepository loanApplicationRepository,
            LoanRepaymentRepository loanRepaymentRepository,
            BankAccountService bankAccountService,
            ModelMapper modelMapper) {

        this.loanApplicationRepository = loanApplicationRepository;
        this.loanRepaymentRepository = loanRepaymentRepository;
        this.bankAccountService = bankAccountService;
        this.modelMapper = modelMapper;
    }
    @Transactional
    @Override
    public LoanRepaymentResponseDto makeRepayment(
            Long loanId,
            LoanRepaymentRequestDto dto,
            String username) {

        LoanApplication loanApplication =
                loanApplicationRepository.findById(loanId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Loan application not found"
                                ));

        // Only disbursed loans can be repaid
        if (loanApplication.getStatus()
                != LoanApplicationStatus.DISBURSED) {

            throw new BadRequestException(
                    "Only disbursed loans can be repaid"
            );
        }

        // Verify that the loan belongs to this customer
        if (!loanApplication.getApplicant()
                .getUsername().equals(username)) {

            throw new BadRequestException(
                    "You cannot repay this loan"
            );
        }

        // Validate installment number
        if (dto.getInstallmentNumber() == null
                || dto.getInstallmentNumber() <= 0) {

            throw new BadRequestException(
                    "Valid installment number is required"
            );
        }

        // Find the existing scheduled EMI
        LoanRepayment repayment =
                loanRepaymentRepository
                        .findByLoanApplicationIdAndInstallmentNumber(
                                loanId,
                                dto.getInstallmentNumber()
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Scheduled installment not found"
                                ));

        // Prevent duplicate payment
        if (repayment.getStatus()
                != LoanRepaymentStatus.PENDING) {

            throw new BadRequestException(
                    "This installment has already been processed"
            );
        }

        // The payment must match this installment's scheduled EMI
        if (dto.getAmount() == null
                || dto.getAmount().compareTo(
                        repayment.getAmount()) != 0) {

            throw new BadRequestException(
                    "Payment amount must match the scheduled EMI: "
                            + repayment.getAmount()
            );
        }

        // Debit the customer's account using the scheduled amount
        bankAccountService.withdraw(
                username,
                repayment.getAmount()
        );

        // Update the existing installment
        repayment.setStatus(LoanRepaymentStatus.PAID);
        repayment.setPaymentDate(LocalDateTime.now());

        LoanRepayment savedRepayment =
                loanRepaymentRepository.save(repayment);

        // Check whether all scheduled installments are paid
        List<LoanRepayment> allRepayments =
                loanRepaymentRepository
                        .findByLoanApplicationIdOrderByInstallmentNumberAsc(
                                loanId
                        );

        boolean allPaid = !allRepayments.isEmpty()
                && allRepayments.stream().allMatch(
                        item -> item.getStatus()
                                == LoanRepaymentStatus.PAID
                );

        if (allPaid) {
            loanApplication.setStatus(
                    LoanApplicationStatus.COMPLETED
            );

            loanApplicationRepository.save(loanApplication);
        }

        // Return the updated repayment
        LoanRepaymentResponseDto response =
                modelMapper.map(
                        savedRepayment,
                        LoanRepaymentResponseDto.class
                );

        response.setLoanApplicationId(
                savedRepayment.getLoanApplication().getId()
        );

        return response;
    }
    @Override
    public List<LoanRepaymentResponseDto> getRepaymentSchedule(
            Long loanId,
            String username) {

        LoanApplication loanApplication =
                loanApplicationRepository.findById(loanId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Loan application not found"
                                ));

        // Customers can view only their own loan schedule
        if (!loanApplication.getApplicant()
                .getUsername().equals(username)) {

            throw new BadRequestException(
                    "You cannot view this loan schedule"
            );
        }

        List<LoanRepayment> repayments =
                loanRepaymentRepository
                        .findByLoanApplicationIdOrderByInstallmentNumberAsc(
                                loanId
                        );

        return repayments.stream().map(repayment -> {

            LoanRepaymentResponseDto response =
                    modelMapper.map(
                            repayment,
                            LoanRepaymentResponseDto.class
                    );

            response.setLoanApplicationId(
                    repayment.getLoanApplication().getId()
            );

            response.setInstallmentNumber(
                    repayment.getInstallmentNumber()
            );

            response.setDueDate(
                    repayment.getDueDate()
            );

            response.setStatus(
                    repayment.getStatus()
            );

            return response;

        }).toList();
    }
    @Transactional
    @Override
    public void recalculateRepaymentComponents(Long loanId) {

        LoanApplication loan =
                loanApplicationRepository.findById(loanId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Loan application not found"
                                ));

        BigDecimal principal = loan.getDisbursedAmount();
        BigDecimal annualRate = loan.getInterestRate();

        if (principal == null || principal.signum() <= 0
                || annualRate == null || annualRate.signum() < 0) {

            throw new BadRequestException(
                    "Valid disbursed amount and interest rate are required"
            );
        }

        List<LoanRepayment> installments =
                loanRepaymentRepository
                        .findByLoanApplicationIdOrderByInstallmentNumberAsc(
                                loanId
                        );

        if (installments.isEmpty()) {
            throw new BadRequestException(
                    "No repayment schedule exists for this loan"
            );
        }

        BigDecimal monthlyRate = annualRate.divide(
                BigDecimal.valueOf(1200),
                12,
                RoundingMode.HALF_UP
        );

        BigDecimal remainingPrincipal = principal.setScale(
                2,
                RoundingMode.HALF_UP
        );

        for (int i = 0; i < installments.size(); i++) {

            LoanRepayment installment = installments.get(i);

            BigDecimal amount = installment.getAmount();
            BigDecimal principalComponent;
            BigDecimal interestComponent;

            boolean lastInstallment = i == installments.size() - 1;

            if (lastInstallment) {
                // Clear any remaining principal in the final installment.
                principalComponent = remainingPrincipal;

                interestComponent = amount
                        .subtract(principalComponent)
                        .setScale(2, RoundingMode.HALF_UP);

                if (interestComponent.signum() < 0) {
                    throw new BadRequestException(
                            "Installment schedule does not match the outstanding principal"
                    );
                }
            } else {
                // Calculate interest on the opening principal balance.
                interestComponent = remainingPrincipal
                        .multiply(monthlyRate)
                        .setScale(2, RoundingMode.HALF_UP);

                principalComponent = amount
                        .subtract(interestComponent)
                        .setScale(2, RoundingMode.HALF_UP);

                if (principalComponent.signum() <= 0
                        || principalComponent.compareTo(
                                remainingPrincipal) > 0) {

                    throw new BadRequestException(
                            "Invalid principal calculation for installment "
                                    + installment.getInstallmentNumber()
                    );
                }
            }

            remainingPrincipal = remainingPrincipal
                    .subtract(principalComponent)
                    .max(BigDecimal.ZERO)
                    .setScale(2, RoundingMode.HALF_UP);

            installment.setPrincipalComponent(principalComponent);
            installment.setInterestComponent(interestComponent);
            installment.setRemainingPrincipal(remainingPrincipal);

            // Do not modify amount, status, paymentDate or dueDate.
        }

        loanRepaymentRepository.saveAll(installments);
    }
}