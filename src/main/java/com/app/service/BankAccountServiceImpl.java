
package com.app.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.app.dto.AccountStatusResponseDto;
import com.app.dto.BankAccountResponseDto;
import com.app.dto.TransactionResponseDto;
import com.app.entity.BankAccount;
import com.app.entity.Transaction;
import com.app.exception.AccountNotFoundException;
import com.app.exception.InsufficientBalanceException;
import com.app.exception.InvalidAmountException;
import com.app.exception.SameAccountTransferException;
import com.app.repo.BankAccountRepo;
import com.app.repo.BeneficiaryRepo;
import com.app.repo.TransactionRepo;

import jakarta.transaction.Transactional;

@Service
public class BankAccountServiceImpl implements BankAccountService {

    @Autowired
    private BankAccountRepo repo;

    @Autowired
    private TransactionRepo transactionRepo;

    @Autowired
    private BeneficiaryRepo beneficiaryRepo;

    // Get logged-in customer's account
    @Override
    public BankAccountResponseDto getMyAccount(String username) {

        BankAccount account = repo.findByUserUsername(username)
                .orElseThrow(() ->
                        new AccountNotFoundException(
                                "Bank account not found"));

        BankAccountResponseDto dto = new BankAccountResponseDto();

        dto.setId(account.getId());
        dto.setAccountNumber(account.getAccountNumber());
        dto.setBalance(account.getBalance());

        return dto;
    }

    // Deposit money
    @Transactional
    @Override
    public BankAccountResponseDto deposit(
            String username,
            BigDecimal amount) {

        BankAccount account = repo.findByUserUsername(username)
                .orElseThrow(() ->
                        new AccountNotFoundException(
                                "Bank account not found"));

        // Block deposits when account is frozen
        ensureNotFrozen(account);

        if (amount == null
                || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException(
                    "Amount must be greater than 0");
        }

        BigDecimal currentBalance = account.getBalance();

        account.setBalance(currentBalance.add(amount));

        BankAccount savedAccount = repo.save(account);

        Transaction transaction = new Transaction();
        transaction.setType("DEPOSIT");
        transaction.setAmount(amount);
        transaction.setDate(LocalDateTime.now());
        transaction.setBankaccount(savedAccount);

        transactionRepo.save(transaction);

        BankAccountResponseDto dto = new BankAccountResponseDto();
        dto.setId(savedAccount.getId());
        dto.setAccountNumber(savedAccount.getAccountNumber());
        dto.setBalance(savedAccount.getBalance());

        return dto;
    }

    // Withdraw money
    @Transactional
    @Override
    public BankAccountResponseDto withdraw(
            String username,
            BigDecimal amount) {

        BankAccount account = repo.findByUserUsername(username)
                .orElseThrow(() ->
                        new AccountNotFoundException(
                                "Bank account not found"));

        // Block withdrawals when account is frozen
        ensureNotFrozen(account);

        if (amount == null
                || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException(
                    "Amount must be greater than 0");
        }

        BigDecimal currentBalance = account.getBalance();

        if (currentBalance.compareTo(amount) < 0) {
            throw new InsufficientBalanceException(
                    "Insufficient balance");
        }

        account.setBalance(currentBalance.subtract(amount));

        BankAccount savedAccount = repo.save(account);

        Transaction transaction = new Transaction();
        transaction.setType("WITHDRAW");
        transaction.setAmount(amount);
        transaction.setDate(LocalDateTime.now());
        transaction.setBankaccount(savedAccount);

        transactionRepo.save(transaction);

        BankAccountResponseDto dto = new BankAccountResponseDto();
        dto.setId(savedAccount.getId());
        dto.setAccountNumber(savedAccount.getAccountNumber());
        dto.setBalance(savedAccount.getBalance());

        return dto;
    }

    // Transfer money to another customer's account
    @Transactional
    @Override
    public BankAccountResponseDto transfer(
            String senderUsername,
            String receiverUsername,
            BigDecimal amount) {

        if (amount == null
                || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException(
                    "Transfer amount must be greater than 0");
        }

        BankAccount senderAccount =
                repo.findByUserUsername(senderUsername)
                        .orElseThrow(() ->
                                new AccountNotFoundException(
                                        "Sender bank account not found"));

        BankAccount receiverAccount =
                repo.findByUserUsername(receiverUsername)
                        .orElseThrow(() ->
                                new AccountNotFoundException(
                                        "Receiver bank account not found"));

        // Check both accounts BEFORE changing any balance
        ensureNotFrozen(senderAccount);
        ensureNotFrozen(receiverAccount);

        if (senderAccount.getId().equals(receiverAccount.getId())) {
            throw new SameAccountTransferException(
                    "You cannot transfer money to your own account");
        }

        if (senderAccount.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException(
                    "Insufficient balance");
        }

        // Deduct from sender
        senderAccount.setBalance(
                senderAccount.getBalance().subtract(amount));

        // Add to receiver
        receiverAccount.setBalance(
                receiverAccount.getBalance().add(amount));

        BankAccount savedSender = repo.save(senderAccount);
        repo.save(receiverAccount);

        // Sender debit transaction
        Transaction senderTransaction = new Transaction();
        senderTransaction.setType("TRANSFER_DEBIT");
        senderTransaction.setAmount(amount);
        senderTransaction.setDate(LocalDateTime.now());
        senderTransaction.setBankaccount(savedSender);
        senderTransaction.setCounterpartyUsername(receiverUsername);

        transactionRepo.save(senderTransaction);

        // Receiver credit transaction
        Transaction receiverTransaction = new Transaction();
        receiverTransaction.setType("TRANSFER_CREDIT");
        receiverTransaction.setAmount(amount);
        receiverTransaction.setDate(LocalDateTime.now());
        receiverTransaction.setBankaccount(receiverAccount);
        receiverTransaction.setCounterpartyUsername(senderUsername);

        transactionRepo.save(receiverTransaction);

        BankAccountResponseDto dto = new BankAccountResponseDto();
        dto.setId(savedSender.getId());
        dto.setAccountNumber(savedSender.getAccountNumber());
        dto.setBalance(savedSender.getBalance());

        return dto;
    }

    // Transaction history with pagination
    @Override
    public Page<TransactionResponseDto> getMyTransactions(
            String username,
            Pageable pageable) {

        Page<Transaction> transactions =
                transactionRepo
                        .findByBankaccountUserUsernameOrderByDateDesc(
                                username,
                                pageable);

        return transactions.map(transaction -> {

            TransactionResponseDto dto =
                    new TransactionResponseDto();

            dto.setId(transaction.getId());
            dto.setType(transaction.getType());
            dto.setAmount(transaction.getAmount());
            dto.setDate(transaction.getDate());
            dto.setCounterpartyUsername(
                    transaction.getCounterpartyUsername());

            return dto;
        });
    }

    // Transfer money to a saved beneficiary
    @Transactional
    @Override
    public BankAccountResponseDto transferToBeneficiary(
            String senderUsername,
            String receiverAccountNumber,
            BigDecimal amount) {

        if (amount == null
                || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException(
                    "Transfer amount must be greater than 0");
        }

        // Find sender's account
        BankAccount senderAccount =
                repo.findByUserUsername(senderUsername)
                        .orElseThrow(() ->
                                new AccountNotFoundException(
                                        "Sender bank account not found"));

        // Confirm beneficiary belongs to the logged-in sender
        beneficiaryRepo.findByUserUsernameAndAccountNumber(
                        senderUsername,
                        receiverAccountNumber)
                .orElseThrow(() ->
                        new AccountNotFoundException(
                                "Saved beneficiary not found"));

        // Find receiver's account
        BankAccount receiverAccount =
                repo.findByAccountNumber(receiverAccountNumber)
                        .orElseThrow(() ->
                                new AccountNotFoundException(
                                        "Receiver bank account not found"));

        // Check both accounts BEFORE changing any balance
        ensureNotFrozen(senderAccount);
        ensureNotFrozen(receiverAccount);

        if (senderAccount.getId().equals(receiverAccount.getId())) {
            throw new SameAccountTransferException(
                    "You cannot transfer money to your own account");
        }

        if (senderAccount.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException(
                    "Insufficient balance");
        }

        // Deduct amount from sender
        senderAccount.setBalance(
                senderAccount.getBalance().subtract(amount));

        // Add amount to receiver
        receiverAccount.setBalance(
                receiverAccount.getBalance().add(amount));

        // Save updated accounts
        BankAccount savedSender = repo.save(senderAccount);
        repo.save(receiverAccount);

        // Record sender's debit transaction
        Transaction senderTransaction = new Transaction();
        senderTransaction.setType("TRANSFER_DEBIT");
        senderTransaction.setAmount(amount);
        senderTransaction.setDate(LocalDateTime.now());
        senderTransaction.setBankaccount(savedSender);
        senderTransaction.setCounterpartyUsername(
                receiverAccount.getUser().getUsername());

        transactionRepo.save(senderTransaction);

        // Record receiver's credit transaction
        Transaction receiverTransaction = new Transaction();
        receiverTransaction.setType("TRANSFER_CREDIT");
        receiverTransaction.setAmount(amount);
        receiverTransaction.setDate(LocalDateTime.now());
        receiverTransaction.setBankaccount(receiverAccount);
        receiverTransaction.setCounterpartyUsername(senderUsername);

        transactionRepo.save(receiverTransaction);

        BankAccountResponseDto dto = new BankAccountResponseDto();
        dto.setId(savedSender.getId());
        dto.setAccountNumber(savedSender.getAccountNumber());
        dto.setBalance(savedSender.getBalance());

        return dto;
    }

    // Common check used by banking operations
    private void ensureNotFrozen(BankAccount account) {

        if (account.isFrozen()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "This account is frozen. Transactions are not allowed.");
        }
    }
    @Override
    public List<AccountStatusResponseDto> getAllAccountStatuses() {

        return repo.findAll()
                .stream()
                .map(account -> mapAccountStatus(account, null))
                .toList();
    }

    @Transactional
    @Override
    public AccountStatusResponseDto freezeAccount(Integer accountId) {

        BankAccount account = repo.findById(accountId)
                .orElseThrow(() ->
                        new AccountNotFoundException(
                                "Bank account not found"));

        account.setFrozen(true);

        BankAccount savedAccount = repo.save(account);

        return mapAccountStatus(
                savedAccount,
                "Account frozen successfully");
    }

    @Transactional
    @Override
    public AccountStatusResponseDto unfreezeAccount(Integer accountId) {

        BankAccount account = repo.findById(accountId)
                .orElseThrow(() ->
                        new AccountNotFoundException(
                                "Bank account not found"));

        account.setFrozen(false);

        BankAccount savedAccount = repo.save(account);

        return mapAccountStatus(
                savedAccount,
                "Account unfrozen successfully");
    }

    private AccountStatusResponseDto mapAccountStatus(
            BankAccount account,
            String message) {

        AccountStatusResponseDto dto =
                new AccountStatusResponseDto();

        dto.setId(account.getId());
        dto.setAccountNumber(account.getAccountNumber());
        dto.setUsername(account.getUser().getUsername());
        dto.setBalance(account.getBalance());
        dto.setFrozen(account.isFrozen());
        dto.setMessage(message);

        return dto;
    }
}
