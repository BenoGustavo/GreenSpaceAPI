package com.greenspace.api.features.email;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import com.greenspace.api.dto.email.EmailDTO;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Component
public class EmailSender {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.from}")
    private String from;

    public void sendEmail(EmailDTO emailContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(from);
        helper.setTo(emailContent.getUserEmail());
        helper.setSubject(emailContent.getSubject());
        helper.setText(emailContent.getMessage(), true); // Set the second parameter to true to enable HTML

        mailSender.send(message);
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
