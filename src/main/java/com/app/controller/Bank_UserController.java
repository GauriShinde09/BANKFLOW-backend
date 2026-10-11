package com.app.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.app.dto.BankAccountResponseDto;
import com.app.dto.DepositeDto;
import com.app.dto.LoginRequestDto;
import com.app.dto.LoginResponseDto;
import com.app.dto.TransactionResponseDto;
import com.app.dto.TransferRequestDto;
import com.app.dto.TransferToBeneficiaryRequestDto;
import com.app.dto.WithdrawRequestDto;
import com.app.entity.BankAccount;
import com.app.entity.Bank_User;
import com.app.entity.Transaction;
import com.app.service.BankAccountService;
import com.app.service.Bank_UserService;
import com.app.service.JWTUtil;

import jakarta.validation.Valid;

@RestController
public class Bank_UserController {
	
	@Autowired
	Bank_UserService service;
	@Autowired
	BankAccountService accservice;
	@Autowired
	 JWTUtil jwtUtil;
	@PostMapping("/register")
	public Bank_User register(@RequestBody Bank_User user) {
		return service.register(user);
	}
	
	@PostMapping("/login")
	public LoginResponseDto register(@RequestBody LoginRequestDto dto) {
	Bank_User user =service.login(dto);
	

    String token = jwtUtil.generateToken(user);
		
		return new LoginResponseDto(user.getUsername(),user.getRole(),  "Login successful", token);
	}
	
	@GetMapping("/test")
	public String test() {
	    return "JWT working!";
	}
	@GetMapping("/getAll")
	@PreAuthorize("hasRole('ADMIN')")
	public List<Bank_User> getAllUser() {
		return service.getAllUser();
	}
	
	
	@GetMapping("/my-account")
	@PreAuthorize("hasRole('CUSTOMER')")
	public BankAccountResponseDto myAccount(Authentication authentication) {

	    String username = authentication.getName();

	    return accservice.getMyAccount(username);
	}
	@PostMapping("/deposite")
	@PreAuthorize("hasRole('CUSTOMER')")
	public BankAccountResponseDto deposite(@RequestBody DepositeDto dto,Authentication authentication) {
		
		  String username = authentication.getName();
		  
		return accservice.deposit(username, dto.getAmount());
		
	}
	@PostMapping("/Withdrawal")
	@PreAuthorize("hasRole('CUSTOMER')")
	public BankAccountResponseDto withdrawal(@RequestBody WithdrawRequestDto dto,Authentication authentication) {
		
		  String username = authentication.getName();
		  
		return accservice.withdraw(username, dto.getAmount());
		
	}
	@GetMapping("/transactions")
	@PreAuthorize("hasRole('CUSTOMER')")
	public Page<TransactionResponseDto> getMyTransactions(
	        Authentication authentication,
	        Pageable pageable) {

	    String username = authentication.getName();

	    return accservice.getMyTransactions(username, pageable);
	}

	@PostMapping("/transfer")
	@PreAuthorize("hasRole('CUSTOMER')")
	public BankAccountResponseDto transfer(
	        @RequestBody @Valid  TransferRequestDto dto,
	        Authentication authentication) {

	    String senderUsername = authentication.getName();

	    return accservice.transfer(
	            senderUsername,
	            dto.getReceiverUsername(),
	            dto.getAmount()
	    );
	}
	
	@PostMapping("/transfer-to-beneficiary")
	@PreAuthorize("hasRole('CUSTOMER')")
	public BankAccountResponseDto transferToBeneficiary(
	        @RequestBody @Valid TransferToBeneficiaryRequestDto dto,
	        Authentication authentication) {

	    String senderUsername = authentication.getName();

	    return accservice.transferToBeneficiary(
	            senderUsername,
	            dto.getReceiverAccountNumber(),
	            dto.getAmount()
	    );
	}
}
