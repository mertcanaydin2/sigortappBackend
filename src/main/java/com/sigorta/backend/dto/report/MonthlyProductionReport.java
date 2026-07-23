package com.sigorta.backend.dto.report;

import java.math.BigDecimal;

public record MonthlyProductionReport(
        Integer year,
        Integer month,
        String period,
        BigDecimal totalGrossPremium
) {
}
