package com.sigorta.backend.dto.report;

import java.math.BigDecimal;

public interface MonthlyProductionProjection {

    Integer getYear();

    Integer getMonth();

    BigDecimal getTotalGrossPremium();
}
