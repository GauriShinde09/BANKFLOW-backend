package com.app.repo;

import com.app.entity.LoanRepayment;
import com.app.enums.LoanRepaymentStatus;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LoanRepaymentRepository
        extends JpaRepository<LoanRepayment, Long> {

    // Get all installments for a loan in installment order
    List<LoanRepayment>
    findByLoanApplicationIdOrderByInstallmentNumberAsc(
            Long loanApplicationId
    );

    // Get installments with a particular status
    List<LoanRepayment>
    findByLoanApplicationIdAndStatusOrderByInstallmentNumberAsc(
            Long loanApplicationId,
            LoanRepaymentStatus status
    );

    // Get a customer's repayment history
    List<LoanRepayment>
    findByLoanApplication_Applicant_UsernameOrderByDueDateAsc(
            String username
    );

    // Find a specific installment belonging to a loan
    Optional<LoanRepayment>
    findByLoanApplicationIdAndInstallmentNumber(
            Long loanApplicationId,
            Integer installmentNumber
    );

	List<LoanRepayment> findByLoanApplicationId(Long loanId);
}