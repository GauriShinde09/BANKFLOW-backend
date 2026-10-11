package com.app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.app.entity.AuthenticateRequest;
import com.app.service.CustomUserDetailsService;
import com.app.service.JWTUtil;

@RestController
public class AuthController {
	@Autowired
	private AuthenticationManager authManager;

	@Autowired
	private CustomUserDetailsService userDetailsService;

	@Autowired
	private JWTUtil jwtUtil;

	@PostMapping("/authenticate")
	public String authenticate(@RequestBody AuthenticateRequest request) throws Exception {

		try {
			authManager.authenticate(
					new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

		} catch (Exception e) {
			throw new Exception("Invalid Username or Password");
		}

		UserDetails user = userDetailsService.loadUserByUsername(request.getUsername());

		String token = jwtUtil.generateToken(user);

		return token;

	}

}
