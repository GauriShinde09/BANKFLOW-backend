package com.app.dto;

import lombok.Data;

@Data
public class LoanEligibilityResponseDto {

    private boolean eligible;

    private String message;
}