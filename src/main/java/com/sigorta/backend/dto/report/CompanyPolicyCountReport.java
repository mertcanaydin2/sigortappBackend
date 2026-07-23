package com.sigorta.backend.dto.report;

public record CompanyPolicyCountReport(
        String company,
        Long policyCount
) {
}
