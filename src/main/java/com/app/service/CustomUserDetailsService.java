package com.app.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.app.entity.Bank_User;
import com.app.repo.Bank_UserRepo;

@Service
public class CustomUserDetailsService implements UserDetailsService {
@Autowired
	Bank_UserRepo repo;
@Autowired
BCryptPasswordEncoder encoder;
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		// TODO Auto-generated method stub
	Optional<Bank_User>user=	repo.findByUsername(username);
	if(user.isPresent()) {
	Bank_User dbUser =	user.get();
	return User.withUsername(dbUser.getUsername()).password((dbUser.getPassword())).roles(dbUser.getRole()).build();
	}
		throw new UsernameNotFoundException("UserName Not Found");
	}

}
