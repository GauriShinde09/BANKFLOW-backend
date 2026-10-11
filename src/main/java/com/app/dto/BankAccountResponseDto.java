package com.app.dto;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class BankAccountResponseDto {

    private Integer id;
    private String accountNumber;
    private BigDecimal balance;
}