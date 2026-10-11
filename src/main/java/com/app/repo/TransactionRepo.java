package com.app.repo;

import java.awt.print.Pageable;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;

import com.app.entity.Transaction;

public interface TransactionRepo extends JpaRepository<Transaction,Integer>{

	Page<Transaction> findByBankaccountUserUsernameOrderByDateDesc(
	        String username, org.springframework.data.domain.Pageable pageable);
	List<Transaction> findByBankaccountUserUsernameOrderByDateDesc(
	        String username
	);

}
