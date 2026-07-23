package com.sigorta.backend.dto;

import com.sigorta.backend.entity.InsuranceRecord;

public record InsuranceUploadResponse(
        String status,
        InsuranceRecord data
) {
}
