package com.sigorta.backend.service;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class NotificationServiceTest {

    @Test
    void emailDeliveryFailureIsHandledWithoutInterruptingTheRequest() {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        doThrow(new MailSendException("SMTP connection failed"))
                .when(mailSender)
                .send(any(SimpleMailMessage.class));
        NotificationService notificationService = createNotificationService(mailSender);

        assertDoesNotThrow(() -> notificationService.sendEmail(
                List.of("customer@example.com"),
                "Policy reminder",
                "Your policy will expire soon."
        ));
    }

    @Test
    void sendsOneEmailForEachRecipient() {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        NotificationService notificationService = createNotificationService(mailSender);

        notificationService.sendEmail(
                List.of("first@example.com", "second@example.com"),
                "Policy reminder",
                "Your policy will expire soon."
        );

        ArgumentCaptor<SimpleMailMessage> mailCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(2)).send(mailCaptor.capture());

        List<SimpleMailMessage> sentMessages = mailCaptor.getAllValues();
        assertArrayEquals(new String[]{"first@example.com"}, sentMessages.get(0).getTo());
        assertArrayEquals(new String[]{"second@example.com"}, sentMessages.get(1).getTo());
        assertEquals("sender@example.com", sentMessages.get(0).getFrom());
        assertEquals("Policy reminder", sentMessages.get(0).getSubject());
        assertEquals("Your policy will expire soon.", sentMessages.get(0).getText());
    }

    private NotificationService createNotificationService(JavaMailSender mailSender) {
        NotificationService notificationService = new NotificationService(mailSender);
        ReflectionTestUtils.setField(notificationService, "senderEmail", "sender@example.com");
        return notificationService;
    }
}
