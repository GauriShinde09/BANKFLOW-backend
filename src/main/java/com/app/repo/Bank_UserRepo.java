package com.app.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.app.entity.Bank_User;

public interface Bank_UserRepo extends JpaRepository<Bank_User,Integer>{
	
	  Optional<Bank_User> findByUsername(String username);
	  Optional<Bank_User> findByEmail(String email);
}
