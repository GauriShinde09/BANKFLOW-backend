package com.app.entity;

import com.app.enums.LoanRepaymentStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@Table(
    name = "loan_repayments",
    uniqueConstraints = {
        @UniqueConstraint(
            columnNames = {
                "loan_application_id",
                "installment_number"
            }
        )
    }
)
public class LoanRepayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "loan_application_id",
        nullable = false
    )
    private LoanApplication loanApplication;

    @Column(name = "installment_number", nullable = false)
    private Integer installmentNumber;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    // Total EMI payable for this installment
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    // Portion of the EMI that repays the principal
    @Column(name = "principal_component", precision = 15, scale = 2)
    private BigDecimal principalComponent;

    // Portion of the EMI that pays interest
    @Column(name = "interest_component", precision = 15, scale = 2)
    private BigDecimal interestComponent;

    // Remaining principal after this installment
    @Column(name = "remaining_principal", precision = 15, scale = 2)
    private BigDecimal remainingPrincipal;

    // Null until the installment is actually paid
    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoanRepaymentStatus status =
            LoanRepaymentStatus.PENDING;
}