package com.app.dto;

import lombok.Data;

@Data
public class LoginResponseDto {

	
	private String username;
    private String role;
    private String message;
    private String token;
    
    public LoginResponseDto(String username, String role, String message,String token) {
        this.username = username;
        this.role = role;
        this.message = message;
        this.token=token;
    }
}
