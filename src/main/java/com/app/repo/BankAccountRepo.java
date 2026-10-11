package com.app.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.app.entity.BankAccount;
import com.app.entity.Beneficiary;

public interface BankAccountRepo extends JpaRepository<BankAccount,Integer>{

	
	Optional<BankAccount>findByUserUsername(String username);


    Optional<BankAccount> findByAccountNumber(String accountNumber);
 

}
