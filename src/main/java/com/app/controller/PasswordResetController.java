
package com.app.controller;

import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.app.dto.ForgotPasswordRequestDto;
import com.app.dto.ResetPasswordRequestDto;
import com.app.entity.Bank_User;
import com.app.repo.Bank_UserRepo;
import com.app.service.EmailService;
import com.app.service.PasswordResetTokenService;

import jakarta.validation.Valid;

@RestController
public class PasswordResetController {

    private static final Logger logger =
            LoggerFactory.getLogger(PasswordResetController.class);

    private final Bank_UserRepo userRepo;
    private final PasswordResetTokenService tokenService;
    private final EmailService emailService;

    public PasswordResetController(
            Bank_UserRepo userRepo,
            PasswordResetTokenService tokenService,
            EmailService emailService) {

        this.userRepo = userRepo;
        this.tokenService = tokenService;
        this.emailService = emailService;
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequestDto dto) {

        String email = dto.getEmail()
                .trim()
                .toLowerCase(Locale.ROOT);

        userRepo.findByEmail(email).ifPresent(user -> {
            try {
                String token = tokenService.createResetToken(user);

                emailService.sendPasswordResetEmail(
                        user.getEmail(),
                        token
                );

            } catch (Exception ex) {
                logger.error(
                        "Unable to process password reset email request",
                        ex
                );
            }
        });

        return ResponseEntity.ok(Map.of(
                "message",
                "If an account with that email exists, a password reset link has been sent."
        ));
    }
    
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(
            @Valid @RequestBody ResetPasswordRequestDto dto) {

        tokenService.resetPassword(
                dto.getToken(),
                dto.getNewPassword()
        );

        return ResponseEntity.ok(Map.of(
                "message",
                "Password reset successfully. Please log in with your new password."
        ));
    }
}


