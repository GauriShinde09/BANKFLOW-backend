package com.app.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Column;
import lombok.Data;

@Entity(name = "transaction")
@Data
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String type;

    private BigDecimal amount;

    private LocalDateTime date;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private BankAccount bankaccount;

    @Column(name = "counterparty_username")
    private String counterpartyUsername;
}