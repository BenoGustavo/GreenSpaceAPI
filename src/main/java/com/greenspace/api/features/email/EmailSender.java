package com.greenspace.api.features.email;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import com.greenspace.api.dto.email.EmailDTO;

@Component
public class EmailSender {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.from}")
    private String from;

    public void sendEmail(EmailDTO emailContent) {
        SimpleMailMessage email = new SimpleMailMessage();
        email.setFrom(from);
        email.setTo(emailContent.getUserEmail());
        email.setSubject(emailContent.getSubject());
        email.setText(emailContent.getMessage());
        mailSender.send(email);
    }

    // public void sendVerificationEmail(UserModel user, String token) {

    // String recipientAddress = user.getEmailAddress();
    // String subject = "Account Verification";
    // String confirmationUrl = host + "/api/auth/verify-email?token=" + token;
    // String message = "Clique no link para verificar sua conta:\n" +
    // confirmationUrl;

    // SimpleMailMessage email = new SimpleMailMessage();
    // email.setFrom(from);
    // email.setTo(recipientAddress);
    // email.setSubject(subject);
    // email.setText(message);
    // mailSender.send(email);
    // }
}
