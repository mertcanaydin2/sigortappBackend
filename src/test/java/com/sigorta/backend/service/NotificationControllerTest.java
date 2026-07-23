package com.sigorta.backend.service;

import com.sigorta.backend.controller.NotificationController;
import com.sigorta.backend.dto.NotificationRequest;
import com.sigorta.backend.entity.InsuranceRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private InsuranceRecordService insuranceRecordService;

    @InjectMocks
    private NotificationController notificationController;

    @Test
    void shouldResolveSmsRecipientFromCustomerIdForMobileRequest() {
        InsuranceRecord customer = InsuranceRecord.builder()
                .id(42L)
                .phoneNumber("05551112233")
                .build();
        NotificationRequest request = new NotificationRequest();
        request.setCustomerId(42L);
        request.setType("SMS");
        request.setMessage("Poliçeniz yakında sona eriyor.");
        when(insuranceRecordService.getInsuranceRecordById(42L)).thenReturn(customer);

        notificationController.sendNotification(request);

        verify(notificationService).sendSms(
                List.of("05551112233"),
                "Poliçeniz yakında sona eriyor."
        );
    }

    @Test
    void shouldKeepLegacyRecipientPayloadForWebClient() {
        NotificationRequest request = new NotificationRequest();
        request.setType("EMAIL");
        request.setRecipients(List.of("customer@example.com"));
        request.setSubject("Yenileme Hatırlatması");
        request.setMessageText("Poliçeniz yakında sona eriyor.");

        notificationController.sendNotification(request);

        verify(notificationService).sendEmail(
                List.of("customer@example.com"),
                "Yenileme Hatırlatması",
                "Poliçeniz yakında sona eriyor."
        );
    }
}
