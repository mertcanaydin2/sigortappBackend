package com.sigorta.backend.service;

import com.sigorta.backend.dto.MessageTemplateResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MessageTemplateServiceTest {

    private final MessageTemplateService messageTemplateService = new MessageTemplateService();

    @Test
    void shouldReturnSmsAndEmailTemplatesGroupedByCategory() {
        List<MessageTemplateResponse> templates = messageTemplateService.getAllTemplates();

        assertThat(templates).hasSize(8);
        assertThat(templates).extracting(MessageTemplateResponse::type)
                .contains("SMS", "EMAIL");
        assertThat(templates).extracting(MessageTemplateResponse::category)
                .contains("Poliçe", "Müşteri", "Kampanya");
        assertThat(templates).allSatisfy(template -> {
            assertThat(template.id()).isPositive();
            assertThat(template.name()).isNotBlank();
            assertThat(template.content()).isNotBlank();
        });
    }
}
