package com.app.dto;



import java.math.BigDecimal;

import lombok.Data;
@Data
public class LoanTypeResponseDto {

    private Long id;
    private String name;
    private String description;
    private BigDecimal interestRate;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private Integer maxTenureMonths;
    private Boolean active;

 
}