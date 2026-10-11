
package com.app.repo;

import com.app.entity.LoanApplication;
import com.app.enums.LoanApplicationStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LoanApplicationRepository
        extends JpaRepository<LoanApplication, Long> {

    // Existing methods
    List<LoanApplication> findByApplicantId(Integer applicantId);

    List<LoanApplication> findByStatus(
            LoanApplicationStatus status
    );

    // Customer: fetch their own applications with pagination
    Page<LoanApplication>
    findByApplicant_UsernameOrderByAppliedAtDesc(
            String username,
            Pageable pageable
    );

    // Admin: fetch applications by status with pagination
    Page<LoanApplication>
    findByStatusOrderByAppliedAtDesc(
            LoanApplicationStatus status,
            Pageable pageable
    );

    // Admin: fetch all applications, newest first
    Page<LoanApplication> findAllByOrderByAppliedAtDesc(
            Pageable pageable
    );

    // Customer: find an application belonging to that customer
    Optional<LoanApplication> findByIdAndApplicant_Username(
            Long id,
            String username
    );
}
