package com.sigorta.backend.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String senderEmail;

    public void sendSms(List<String> phoneNumbers, String message) {
        for (String phoneNumber : recipientsOrEmpty(phoneNumbers)) {
            if (phoneNumber == null || phoneNumber.isBlank()) {
                continue;
            }

            log.info("{} adresine SMS g\u00f6nderildi: {}", phoneNumber.trim(), message);
        }
    }

    public void sendEmail(List<String> emails, String subject, String messageText) {
        for (String email : recipientsOrEmpty(emails)) {
            if (email == null || email.isBlank()) {
                continue;
            }

            String recipient = email.trim();

            try {
                SimpleMailMessage mailMessage = new SimpleMailMessage();
                mailMessage.setFrom(senderEmail);
                mailMessage.setTo(recipient);
                mailMessage.setSubject(subject);
                mailMessage.setText(messageText);
                mailSender.send(mailMessage);
                log.info("Email ba\u015far\u0131yla g\u00f6nderildi: {}", recipient);
            } catch (Exception exception) {
                log.error(
                        "Email g\u00f6nderim hatas\u0131 {}: {}",
                        recipient,
                        exception.getMessage(),
                        exception
                );
            }
        }
    }

    private List<String> recipientsOrEmpty(List<String> recipients) {
        return recipients == null ? Collections.emptyList() : recipients;
    }
}
