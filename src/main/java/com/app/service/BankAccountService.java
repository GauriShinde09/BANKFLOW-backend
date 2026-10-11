package com.app.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.app.dto.AccountStatusResponseDto;
import com.app.dto.AddBeneficiaryDto;
import com.app.dto.BankAccountResponseDto;
import com.app.dto.TransactionResponseDto;
import com.app.entity.BankAccount;
import com.app.entity.Beneficiary;
import com.app.entity.Transaction;

public interface BankAccountService {

	

	BankAccountResponseDto getMyAccount(String username);
	BankAccountResponseDto deposit(String username,BigDecimal amount);
	BankAccountResponseDto withdraw(String username, BigDecimal amount);
	Page<TransactionResponseDto> getMyTransactions(
	        String username,
	        Pageable pageable
	);
	BankAccountResponseDto transfer(
	        String senderUsername,
	        String receiverUsername,
	        BigDecimal amount
	);

	BankAccountResponseDto transferToBeneficiary(
	        String senderUsername,
	        String receiverAccountNumber,
	        BigDecimal amount
	);
	List<AccountStatusResponseDto> getAllAccountStatuses();

	AccountStatusResponseDto freezeAccount(Integer accountId);

	AccountStatusResponseDto unfreezeAccount(Integer accountId);
}
