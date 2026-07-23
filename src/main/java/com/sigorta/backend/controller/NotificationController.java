package com.sigorta.backend.controller;

import com.sigorta.backend.dto.NotificationRequest;
import com.sigorta.backend.entity.InsuranceRecord;
import com.sigorta.backend.service.InsuranceRecordService;
import com.sigorta.backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final InsuranceRecordService insuranceRecordService;

    @PostMapping(
            value = "/send",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Map<String, String>> sendNotification(
            @RequestBody NotificationRequest request
    ) {
        String notificationType = resolveNotificationType(request);
        List<String> recipients = resolveRecipients(request, notificationType);
        String messageText = resolveMessageText(request);
        validateResolvedRequest(recipients, messageText);

        switch (notificationType) {
            case "SMS" -> notificationService.sendSms(recipients, messageText);
            case "EMAIL" -> {
                if (request.getSubject() == null || request.getSubject().isBlank()) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "EMAIL bildirimleri i\u00e7in subject alan\u0131 zorunludur"
                    );
                }
                notificationService.sendEmail(
                        recipients,
                        request.getSubject().trim(),
                        messageText
                );
            }
            default -> throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "type alan\u0131 SMS veya EMAIL olmal\u0131d\u0131r"
            );
        }

        return ResponseEntity.ok(Map.of(
                "message",
                "Bildirim iste\u011fi ba\u015far\u0131yla i\u015flendi"
        ));
    }

    private String resolveNotificationType(NotificationRequest request) {
        if (request == null || request.getType() == null || request.getType().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "type alan\u0131 zorunludur");
        }

        String notificationType = request.getType().trim().toUpperCase(Locale.ROOT);
        if (!notificationType.equals("SMS") && !notificationType.equals("EMAIL")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "type alan\u0131 SMS veya EMAIL olmal\u0131d\u0131r"
            );
        }
        return notificationType;
    }

    private List<String> resolveRecipients(NotificationRequest request, String notificationType) {
        if (request.getRecipients() != null && !request.getRecipients().isEmpty()) {
            return request.getRecipients().stream()
                    .filter(recipient -> recipient != null && !recipient.isBlank())
                    .map(String::trim)
                    .distinct()
                    .toList();
        }

        if (request.getCustomerId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "recipients alan\u0131 zorunludur");
        }

        InsuranceRecord customer = insuranceRecordService.getInsuranceRecordById(request.getCustomerId());
        String recipient = notificationType.equals("SMS")
                ? customer.getPhoneNumber()
                : customer.getEmail();

        if (recipient == null || recipient.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    notificationType.equals("SMS")
                            ? "M\u00fc\u015fterinin telefon numaras\u0131 bulunmuyor"
                            : "M\u00fc\u015fterinin e-posta adresi bulunmuyor"
            );
        }
        return List.of(recipient.trim());
    }

    private String resolveMessageText(NotificationRequest request) {
        if (request.getMessageText() != null && !request.getMessageText().isBlank()) {
            return request.getMessageText().trim();
        }
        return request.getMessage() == null ? "" : request.getMessage().trim();
    }

    private void validateResolvedRequest(List<String> recipients, String messageText) {
        if (recipients.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "recipients alan\u0131 zorunludur");
        }
        if (messageText.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "messageText alan\u0131 zorunludur");
        }
    }
}
