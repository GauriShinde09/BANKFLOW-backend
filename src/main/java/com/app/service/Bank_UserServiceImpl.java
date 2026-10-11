package com.app.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.app.dto.LoginRequestDto;
import com.app.entity.BankAccount;
import com.app.entity.Bank_User;
import com.app.repo.BankAccountRepo;
import com.app.repo.Bank_UserRepo;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
@Service
public class Bank_UserServiceImpl implements Bank_UserService {

 private final BCryptPasswordEncoder bcryptpasswordencoder;
 private final Bank_UserRepo repo;
 private final BankAccountRepo bankAccountRepo;
 
 private final JWTUtil jwtUtil;
 Bank_UserServiceImpl( BCryptPasswordEncoder bcryptpasswordencoder, Bank_UserRepo repo, BankAccountRepo bankAccountRepo, JWTUtil jwtUtil){

	this.bcryptpasswordencoder = bcryptpasswordencoder;
	this.repo = repo;
	this.bankAccountRepo = bankAccountRepo;
	this.jwtUtil = jwtUtil;
	 
 }

 @Override
 public Bank_User register(Bank_User user) {

     if (user.getUsername() == null
             || user.getUsername().isBlank()) {
         throw new RuntimeException("Username is required");
     }

     if (user.getEmail() == null
             || user.getEmail().isBlank()) {
         throw new RuntimeException("Email is required");
     }

     if (user.getPassword() == null
             || user.getPassword().isBlank()) {
         throw new RuntimeException("Password is required");
     }

     String email = user.getEmail().trim().toLowerCase();
     user.setEmail(email);
     user.setUsername(user.getUsername().trim());

     if (repo.findByUsername(user.getUsername()).isPresent()) {
         throw new RuntimeException("Username already exists");
     }

     if (repo.findByEmail(email).isPresent()) {
         throw new RuntimeException("Email already registered");
     }

     user.setPassword(
             bcryptpasswordencoder.encode(user.getPassword())
     );

  // Public registration must never create an admin account.
     user.setRole("CUSTOMER");
     Bank_User savedUser = repo.save(user);

     if ("CUSTOMER".equals(savedUser.getRole())) {

         BankAccount account = new BankAccount();
         account.setAccountNumber("ACC" + UUID.randomUUID());
         account.setBalance(BigDecimal.ZERO);
         account.setUser(savedUser);

         bankAccountRepo.save(account);
     }

     return savedUser;
 }

@Override
public Bank_User login(LoginRequestDto dto) {

    Bank_User user = repo.findByUsername(dto.getUsername().trim())
            .orElseThrow(() ->
                    new ResponseStatusException(
                            HttpStatus.UNAUTHORIZED,
                            "Incorrect username or password. Please try again."
                    ));

    if (!bcryptpasswordencoder.matches(
            dto.getPassword(),
            user.getPassword())) {

        throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Incorrect username or password. Please try again."
        );
    }

    return user;
}


 @Override
 public List<Bank_User> getAllUser() {
	// TODO Auto-generated method stub
	return repo.findAll();
 }


 
   

}
