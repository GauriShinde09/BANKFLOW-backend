
package com.app.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String senderEmail;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendPasswordResetEmail(
            String recipientEmail,
            String token) {

        String resetLink = frontendUrl.replaceAll("/+$", "")
                + "/reset-password?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(senderEmail);
        message.setTo(recipientEmail);
        message.setSubject("BANKFLOW - Password Reset");
        message.setText(
                "Hello,\n\n"
                + "We received a request to reset your BANKFLOW password.\n\n"
                + "Use the link below to reset your password:\n"
                + resetLink + "\n\n"
                + "This link expires in 15 minutes and can only be used once.\n\n"
                + "If you did not request a password reset, ignore this email.\n\n"
                + "BANKFLOW Support"
        );

        mailSender.send(message);
    }
}
