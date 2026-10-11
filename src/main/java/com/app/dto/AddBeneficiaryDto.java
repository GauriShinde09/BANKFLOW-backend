package com.app.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddBeneficiaryDto {

    @NotBlank(message = "Beneficiary name is required")
    private String beneficiaryName;

    @NotBlank(message = "Account number is required")
    private String accountNumber;
}