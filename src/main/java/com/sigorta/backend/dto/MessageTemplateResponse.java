package com.sigorta.backend.dto;

public record MessageTemplateResponse(
        long id,
        String category,
        String name,
        String type,
        String content,
        String subject
) {
}
