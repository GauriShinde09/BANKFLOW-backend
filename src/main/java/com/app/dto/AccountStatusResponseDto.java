package com.app.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class AccountStatusResponseDto {

    private Integer id;
    private String accountNumber;
    private String username;
    private BigDecimal balance;
    private boolean frozen;
    private String message;
}