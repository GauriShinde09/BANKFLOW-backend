package com.app.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Beneficiary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String beneficiaryName;

    private String accountNumber;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private Bank_User user;
}