package com.sigorta.backend.dto.report;

import java.math.BigDecimal;
import java.util.List;

public record DashboardReportResponse(
        long totalCustomers,
        long totalActivePolicies,
        long upcomingRenewals,
        BigDecimal monthlyTotalPremium,
        List<MonthlyProductionReport> monthlyProduction,
        List<CompanyPolicyCountReport> companyDistribution
) {
}
