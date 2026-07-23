package com.sigorta.backend.controller;

import com.sigorta.backend.dto.MessageTemplateResponse;
import com.sigorta.backend.service.MessageTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/templates")
@RequiredArgsConstructor
public class MessageTemplateController {

    private final MessageTemplateService messageTemplateService;

    @GetMapping
    public ResponseEntity<List<MessageTemplateResponse>> getAllTemplates() {
        return ResponseEntity.ok(messageTemplateService.getAllTemplates());
    }
}
