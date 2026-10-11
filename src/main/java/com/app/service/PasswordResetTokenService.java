
package com.app.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.app.entity.Bank_User;
import com.app.entity.PasswordResetToken;
import com.app.repo.Bank_UserRepo;
import com.app.repo.PasswordResetTokenRepo;

@Service
public class PasswordResetTokenService {

    private static final SecureRandom SECURE_RANDOM =
            new SecureRandom();

    private final PasswordResetTokenRepo tokenRepo;
    private final Bank_UserRepo userRepo;
    private final BCryptPasswordEncoder passwordEncoder;

    public PasswordResetTokenService(
            PasswordResetTokenRepo tokenRepo,
            Bank_UserRepo userRepo,
            BCryptPasswordEncoder passwordEncoder) {

        this.tokenRepo = tokenRepo;
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public String createResetToken(Bank_User user) {

        tokenRepo.deleteByUser_Id(user.getId());

        byte[] randomBytes = new byte[32];
        SECURE_RANDOM.nextBytes(randomBytes);

        String rawToken = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);

        PasswordResetToken resetToken =
                new PasswordResetToken();

        resetToken.setTokenHash(hashToken(rawToken));
        resetToken.setUser(user);
        resetToken.setExpiryDate(
                LocalDateTime.now().plusMinutes(15));
        resetToken.setUsed(false);

        tokenRepo.save(resetToken);

        return rawToken;
    }

    @Transactional
    public void resetPassword(
            String rawToken,
            String newPassword) {

        if (rawToken == null || rawToken.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid or expired reset token");
        }

        PasswordResetToken resetToken =
                tokenRepo.findByTokenHash(hashToken(rawToken))
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "Invalid or expired reset token"));

        if (resetToken.isUsed()
                || !resetToken.getExpiryDate()
                        .isAfter(LocalDateTime.now())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid or expired reset token");
        }

        if (newPassword == null
                || newPassword.length() < 8
                || newPassword.length() > 72) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Password must be between 8 and 72 characters");
        }

        Bank_User user = resetToken.getUser();

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepo.save(user);

        resetToken.setUsed(true);
        tokenRepo.save(resetToken);
    }

    @Transactional(readOnly = true)
    public PasswordResetToken validateToken(String rawToken) {

        if (rawToken == null || rawToken.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid or expired reset token");
        }

        PasswordResetToken resetToken =
                tokenRepo.findByTokenHash(hashToken(rawToken))
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "Invalid or expired reset token"));

        if (resetToken.isUsed()
                || !resetToken.getExpiryDate()
                        .isAfter(LocalDateTime.now())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid or expired reset token");
        }

        return resetToken;
    }

    private String hashToken(String rawToken) {

        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    rawToken.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(hash);

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(
                    "SHA-256 is not available", e);
        }
    }
}
