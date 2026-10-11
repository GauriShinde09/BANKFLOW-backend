package com.app.service;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
@Component
public class JWTFilter extends OncePerRequestFilter {

	private final JWTUtil jwtService;
	private final CustomUserDetailsService userDetailsService;

	public JWTFilter(JWTUtil jwtService, CustomUserDetailsService userDetailsService) {
		this.jwtService = jwtService;
		this.userDetailsService = userDetailsService;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		final String authHeader = request.getHeader("Authorization");

		// 1️⃣ No token → continue
		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			filterChain.doFilter(request, response);
			return;
		}

		// 2️⃣ Extract token
		String token = authHeader.substring(7);
		if (!jwtService.validateToken(token)) {
		    System.out.println("FILTER: TOKEN REJECTED");
		    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		    return;
		}

		System.out.println("FILTER: TOKEN ACCEPTED");

		String username = jwtService.extractUsername(token);

		System.out.println("JWT USERNAME: " + username);

		// 4️⃣ Extract username
		String username1 = jwtService.extractUsername(token);

		// 5️⃣ Set authentication only if not already set
		if (username1 != null && SecurityContextHolder.getContext().getAuthentication() == null) {

			UserDetails userDetails = userDetailsService.loadUserByUsername(username1);

			UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails, null,
					userDetails.getAuthorities());

			authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

			
			SecurityContextHolder.getContext().setAuthentication(authToken);

			System.out.println("AUTHENTICATED USER: "
			        + SecurityContextHolder.getContext().getAuthentication().getName());

			System.out.println("AUTHORITIES: "
			        + SecurityContextHolder.getContext().getAuthentication().getAuthorities());
		}

// 6️⃣ Continue filter chain
		filterChain.doFilter(request, response);
	}
}