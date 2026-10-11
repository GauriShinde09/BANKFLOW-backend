package com.app.service;

import com.app.dto.EmiCalculationResponseDto;
import com.app.dto.LoanApplicationRequestDto;
import com.app.dto.LoanApplicationResponseDto;
import com.app.dto.LoanStatusResponseDto;
import com.app.entity.Bank_User;
import com.app.entity.LoanApplication;
import com.app.entity.LoanRepayment;
import com.app.entity.LoanType;
import com.app.enums.LoanApplicationStatus;
import com.app.enums.LoanRepaymentStatus;
import com.app.exception.BadRequestException;
import com.app.exception.ResourceNotFoundException;
import com.app.repo.Bank_UserRepo;
import com.app.repo.LoanApplicationRepository;
import com.app.repo.LoanRepaymentRepository;
import com.app.repo.LoanTypeRepository;

import jakarta.transaction.Transactional;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class LoanApplicationServiceImpl implements LoanApplicationService {

    private final LoanApplicationRepository loanApplicationRepository;
    private final LoanTypeRepository loanTypeRepository;
    private final Bank_UserRepo bankUserRepository;
    private final ModelMapper modelMapper;
    private final BankAccountService bankAccountService;
    private final EmiCalculationService emiCalculationService;
    private final LoanRepaymentRepository loanRepaymentRepository;
    
    public LoanApplicationServiceImpl(
            LoanApplicationRepository loanApplicationRepository,
            LoanTypeRepository loanTypeRepository,
            Bank_UserRepo bankUserRepository,
            ModelMapper modelMapper, BankAccountService bankAccountService, EmiCalculationService emiCalculationService, LoanRepaymentRepository loanRepaymentRepository) {

        this.loanApplicationRepository = loanApplicationRepository;
        this.loanTypeRepository = loanTypeRepository;
        this.bankUserRepository = bankUserRepository;
        this.modelMapper = modelMapper;
		this.bankAccountService = bankAccountService;
		this.emiCalculationService = emiCalculationService;
		this.loanRepaymentRepository = loanRepaymentRepository;
    }

    @Override
    public LoanApplicationResponseDto applyForLoan(
            LoanApplicationRequestDto dto,
            String username) {

        // 1. Validate monthly income
        if (dto.getMonthlyIncome() == null
                || dto.getMonthlyIncome().compareTo(BigDecimal.ZERO) <= 0) {

            throw new BadRequestException(
                    "Monthly income must be greater than zero"
            );
        }

        // 2. Find the applicant
        Bank_User applicant = bankUserRepository
                .findByUsername(username)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        // 3. Find the selected loan type
        LoanType loanType = loanTypeRepository
                .findById(dto.getLoanTypeId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Loan type not found"));

        // 4. Check whether the loan type is active
        if (!Boolean.TRUE.equals(loanType.getActive())) {
            throw new BadRequestException(
                    "This loan type is currently inactive"
            );
        }

        // 5. Validate requested loan amount
        if (dto.getRequestedAmount().compareTo(
                loanType.getMinAmount()) < 0) {

            throw new BadRequestException(
                    "Requested amount is below minimum loan amount"
            );
        }

        if (dto.getRequestedAmount().compareTo(
                loanType.getMaxAmount()) > 0) {

            throw new BadRequestException(
                    "Requested amount exceeds maximum loan amount"
            );
        }

        // 6. Validate requested tenure
        if (dto.getRequestedTenureMonths()
                > loanType.getMaxTenureMonths()) {

            throw new BadRequestException(
                    "Requested tenure exceeds maximum allowed tenure"
            );
        }

        // 7. Calculate estimated EMI using the existing service
        EmiCalculationResponseDto emiDetails =
                emiCalculationService.calculateEmi(
                        dto.getRequestedAmount(),
                        loanType.getInterestRate(),
                        dto.getRequestedTenureMonths()
                );

        BigDecimal estimatedEmi = emiDetails.getMonthlyEmi();

        // Demo eligibility rule: EMI must not exceed 40% of income
        BigDecimal maximumAffordableEmi =
                dto.getMonthlyIncome()
                        .multiply(new BigDecimal("0.40"))
                        .setScale(2, RoundingMode.HALF_UP);

        boolean eligible = estimatedEmi.compareTo(
                maximumAffordableEmi) <= 0;

        // 8. Create the application
        LoanApplication application = new LoanApplication();

        application.setApplicant(applicant);
        application.setLoanType(loanType);
        application.setRequestedAmount(dto.getRequestedAmount());
        application.setMonthlyIncome(dto.getMonthlyIncome());
        application.setRequestedTenureMonths(
                dto.getRequestedTenureMonths()
        );
        application.setInterestRate(loanType.getInterestRate());

        // 9. Store eligibility outcome
        if (eligible) {
            application.setStatus(LoanApplicationStatus.PENDING);
        } else {
            application.setStatus(LoanApplicationStatus.REJECTED);

            application.setRejectionReason(
                    "Application does not meet the demo eligibility rule: "
                    + "estimated EMI exceeds 40% of monthly income."
            );
        }

        // Save the application and return its response
        LoanApplication savedApplication =
                loanApplicationRepository.save(application);

        return mapToResponse(savedApplication);
    }
    @Override
    public List<LoanApplicationResponseDto> getMyApplications(
            String username) {

        Bank_User applicant = bankUserRepository
                .findByUsername(username)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        return loanApplicationRepository
                .findByApplicantId(applicant.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public LoanApplicationResponseDto getApplicationById(Long id) {

        LoanApplication application =
                loanApplicationRepository
                        .findById(id)
                        .orElseThrow(() ->
                        new ResourceNotFoundException("Loan application not found")
                                );

        return mapToResponse(application);
    }

    // Entity → Response DTO
    private LoanApplicationResponseDto mapToResponse(
            LoanApplication application) {

        LoanApplicationResponseDto response =
                modelMapper.map(
                        application,
                        LoanApplicationResponseDto.class
                );

        // Nested entity mapping
        response.setApplicantId(
                application.getApplicant().getId()
        );

        response.setLoanTypeId(
                application.getLoanType().getId()
        );

        response.setLoanTypeName(
                application.getLoanType().getName()
        );
        response.setEmiAmount(application.getEmiAmount());

        response.setTotalInterest(application.getTotalInterest());

        response.setTotalRepayment(application.getTotalRepayment());
        return response;
    }
    @Override
    public LoanApplicationResponseDto approveLoan(
            Long id,
            BigDecimal approvedAmount) {

        LoanApplication application =
                loanApplicationRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Loan application not found"
                                ));

        if (application.getStatus()
                != LoanApplicationStatus.PENDING) {

            throw new BadRequestException(
                    "Loan application is already processed"
            );
        }

        if (approvedAmount.compareTo(
                application.getRequestedAmount()) > 0) {

            throw new BadRequestException(
                    "Approved amount cannot be greater than requested amount"
            );
        }

        if (approvedAmount.compareTo(BigDecimal.ZERO) <= 0) {

            throw new BadRequestException(
                    "Approved amount must be greater than 0"
            );
        }

        application.setApprovedAmount(approvedAmount);

        application.setApprovedAmount(approvedAmount);

     // Calculate EMI using the existing service
     EmiCalculationResponseDto emiDetails =
             emiCalculationService.calculateEmi(
                     approvedAmount,
                     application.getInterestRate(),
                     application.getRequestedTenureMonths()
             );

     // Store calculated values in the loan application
     application.setEmiAmount(
             emiDetails.getMonthlyEmi()
     );

     application.setTotalInterest(
             emiDetails.getTotalInterest()
     );

     application.setTotalRepayment(
             emiDetails.getTotalAmount()
     );

     // Update application status
     application.setStatus(
             LoanApplicationStatus.APPROVED
     );
        LoanApplication updatedApplication =
                loanApplicationRepository.save(application);

        return mapToResponse(updatedApplication);
    }
    private void calculateRepaymentDetails(LoanApplication application, BigDecimal approvedAmount,
			BigDecimal interestRate) {
		// TODO Auto-generated method stub
		
	}

	@Override
    public LoanApplicationResponseDto rejectLoan(
            Long id,
            String rejectionReason) {

        LoanApplication application =
                loanApplicationRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Loan application not found"
                                ));

        if (application.getStatus()
                != LoanApplicationStatus.PENDING) {

            throw new BadRequestException(
                    "Loan application is already processed"
            );
        }

        if (rejectionReason == null ||
                rejectionReason.trim().isEmpty()) {

            throw new BadRequestException(
                    "Rejection reason is required"
            );
        }

        application.setStatus(
                LoanApplicationStatus.REJECTED
        );

        application.setRejectionReason(
                rejectionReason
        );

        LoanApplication updatedApplication =
                loanApplicationRepository.save(application);

        return mapToResponse(updatedApplication);
    }
    @Transactional
    @Override
    public LoanApplicationResponseDto disburseLoan(Long id) {

        LoanApplication application =
                loanApplicationRepository.findById(id)
                        .orElseThrow(() ->
                                new  ResourceNotFoundException(
                                        "Loan application not found"
                                ));

        if (application.getStatus()
                != LoanApplicationStatus.APPROVED) {

            throw new BadRequestException(
                    "Only approved loans can be disbursed"
            );
        }

        if (application.getDisbursedAmount() != null) {

            throw new BadRequestException(
                    "Loan is already disbursed"
            );
        }

        BigDecimal amount =
                application.getApprovedAmount();

        bankAccountService.deposit(
                application.getApplicant().getUsername(),
                amount
        );

        application.setDisbursedAmount(amount);

        application.setDisbursedAt(
                LocalDateTime.now()
        );

        application.setStatus(
                LoanApplicationStatus.DISBURSED
        );

     // Generate all scheduled EMI installments
     generateRepaymentSchedule(application);
        LoanApplication updatedApplication =
                loanApplicationRepository.save(application);
        return mapToResponse(updatedApplication);
    }
    
    @Override
    public LoanStatusResponseDto getLoanStatus(
            Long loanId,
            String username) {

        LoanApplication application =
                loanApplicationRepository.findById(loanId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Loan application not found"
                                ));

        if (!application.getApplicant()
                .getUsername()
                .equals(username)) {

            throw new BadRequestException(
                    "You cannot view this loan"
            );
        }

        LoanStatusResponseDto response =
                new LoanStatusResponseDto();

        response.setLoanApplicationId(application.getId());

        response.setStatus(application.getStatus());

        response.setRequestedAmount(
                application.getRequestedAmount()
        );

        response.setApprovedAmount(
                application.getApprovedAmount()
        );

        response.setDisbursedAmount(
                application.getDisbursedAmount()
        );

        return response;
    }
    @Override
    public List<LoanApplicationResponseDto> getLoanHistory(
            String username) {

        Bank_User applicant = bankUserRepository
                .findByUsername(username)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        return loanApplicationRepository
                .findByApplicantId(applicant.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }
    private void generateRepaymentSchedule(
            LoanApplication application) {

        Integer tenure = application.getRequestedTenureMonths();
        BigDecimal emi = application.getEmiAmount();
        BigDecimal totalRepayment = application.getTotalRepayment();
        BigDecimal annualInterestRate = application.getInterestRate();
        BigDecimal principal = application.getDisbursedAmount();

        if (tenure == null || tenure <= 0
                || emi == null || totalRepayment == null
                || annualInterestRate == null
                || principal == null || principal.signum() <= 0
                || application.getDisbursedAt() == null) {

            throw new BadRequestException(
                    "Loan repayment details are incomplete"
            );
        }

        if (annualInterestRate.signum() < 0) {
            throw new BadRequestException(
                    "Interest rate cannot be negative"
            );
        }

        BigDecimal monthlyRate = annualInterestRate.divide(
                BigDecimal.valueOf(1200),
                12,
                RoundingMode.HALF_UP
        );

        BigDecimal remainingPrincipal = principal.setScale(
                2,
                RoundingMode.HALF_UP
        );

        BigDecimal scheduledAmount = BigDecimal.ZERO;

        LocalDate disbursementDate =
                application.getDisbursedAt().toLocalDate();

        List<LoanRepayment> repayments = new ArrayList<>();

        for (int installment = 1; installment <= tenure; installment++) {

            LoanRepayment repayment = new LoanRepayment();

            repayment.setLoanApplication(application);
            repayment.setInstallmentNumber(installment);
            repayment.setDueDate(
                    disbursementDate.plusMonths(installment)
            );

            BigDecimal installmentAmount = emi.setScale(
                    2,
                    RoundingMode.HALF_UP
            );

            // Adjust the final installment for rounding differences.
            if (installment == tenure) {
                installmentAmount = totalRepayment
                        .subtract(scheduledAmount)
                        .setScale(2, RoundingMode.HALF_UP);
            }

            BigDecimal interestComponent;
            BigDecimal principalComponent;

            if (installment == tenure) {
                // Clear the remaining principal in the final installment.
                principalComponent = remainingPrincipal;

                interestComponent = installmentAmount
                        .subtract(principalComponent)
                        .setScale(2, RoundingMode.HALF_UP);

            } else {
                // Interest is calculated on the outstanding principal.
                interestComponent = remainingPrincipal
                        .multiply(monthlyRate)
                        .setScale(2, RoundingMode.HALF_UP);

                principalComponent = installmentAmount
                        .subtract(interestComponent)
                        .setScale(2, RoundingMode.HALF_UP);

                if (principalComponent.compareTo(
                        remainingPrincipal) > 0) {

                    throw new BadRequestException(
                            "Calculated principal exceeds outstanding balance"
                    );
                }
            }

            remainingPrincipal = remainingPrincipal
                    .subtract(principalComponent)
                    .max(BigDecimal.ZERO)
                    .setScale(2, RoundingMode.HALF_UP);

            repayment.setAmount(installmentAmount);
            repayment.setPrincipalComponent(principalComponent);
            repayment.setInterestComponent(interestComponent);
            repayment.setRemainingPrincipal(remainingPrincipal);

            repayment.setStatus(LoanRepaymentStatus.PENDING);

            repayments.add(repayment);
            scheduledAmount = scheduledAmount.add(installmentAmount);
        }

        loanRepaymentRepository.saveAll(repayments);
    }
    @Override
    @jakarta.transaction.Transactional
    public List<LoanApplicationResponseDto> getAllApplicationsForAdmin() {

        return loanApplicationRepository.findAll()
                .stream()
                .sorted(
                    java.util.Comparator.comparing(
                        LoanApplication::getAppliedAt,
                        java.util.Comparator.nullsLast(
                            java.util.Comparator.reverseOrder()
                        )
                    )
                )
                .map(this::mapToResponse)
                .toList();
    }

}
	