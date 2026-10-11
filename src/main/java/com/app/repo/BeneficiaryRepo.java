package com.app.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.app.entity.Beneficiary;

public interface BeneficiaryRepo extends JpaRepository<Beneficiary, Integer> {

    List<Beneficiary> findByUserUsername(String username);

    Optional<Beneficiary> findByUserUsernameAndAccountNumber(
            String username,
            String accountNumber
    );
}