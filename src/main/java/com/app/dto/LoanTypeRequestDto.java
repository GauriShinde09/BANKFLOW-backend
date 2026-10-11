package com.app.dto;



import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
@Data
public class LoanTypeRequestDto {

    @NotBlank(message = "Loan name is required")
    private String name;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Interest rate is required")
    @DecimalMin(value = "0.0", inclusive = false,
            message = "Interest rate must be greater than 0")
    private BigDecimal interestRate;

    @NotNull(message = "Minimum amount is required")
    @DecimalMin(value = "0.0", inclusive = false,
            message = "Minimum amount must be greater than 0")
    private BigDecimal minAmount;

    @NotNull(message = "Maximum amount is required")
    @DecimalMin(value = "0.0", inclusive = false,
            message = "Maximum amount must be greater than 0")
    private BigDecimal maxAmount;

    @NotNull(message = "Maximum tenure is required")
    @Min(value = 1, message = "Tenure must be at least 1 month")
    private Integer maxTenureMonths;

   
}