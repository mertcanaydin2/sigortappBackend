package com.sigorta.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {

    private Long customerId;

    private String type;

    private List<String> recipients;

    private String subject;

    private String messageText;

    private String message;
}
