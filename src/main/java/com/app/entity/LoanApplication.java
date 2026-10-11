package com.app.entity;

import com.app.enums.LoanApplicationStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "loan_applications")
public class LoanApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "applicant_id", nullable = false)
    private Bank_User applicant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "loan_type_id", nullable = false)
    private LoanType loanType;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal requestedAmount;

    // Monthly income provided by the applicant
    @Column(name = "monthly_income", precision = 15, scale = 2)
    private BigDecimal monthlyIncome;

    @Column(nullable = false)
    private Integer requestedTenureMonths;

    @Column(precision = 15, scale = 2)
    private BigDecimal approvedAmount;

    @Column(precision = 5, scale = 2)
    private BigDecimal interestRate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoanApplicationStatus status =
            LoanApplicationStatus.PENDING;

    @Column(nullable = false, updatable = false)
    private LocalDateTime appliedAt;

    private LocalDateTime reviewedAt;

    private String rejectionReason;

    @Column(precision = 15, scale = 2)
    private BigDecimal disbursedAmount;

    @Column(precision = 15, scale = 2)
    private BigDecimal emiAmount;

    @Column(precision = 15, scale = 2)
    private BigDecimal totalInterest;

    @Column(precision = 15, scale = 2)
    private BigDecimal totalRepayment;

    private LocalDateTime disbursedAt;

    @PrePersist
    protected void onCreate() {
        appliedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        if (status != LoanApplicationStatus.PENDING
                && reviewedAt == null) {
            reviewedAt = LocalDateTime.now();
        }
    }
}