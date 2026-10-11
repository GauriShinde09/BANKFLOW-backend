package com.app.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class TransactionResponseDto {

    private Integer id;
    private String type;
    private BigDecimal amount;
    private LocalDateTime date;
    private String counterpartyUsername;
}