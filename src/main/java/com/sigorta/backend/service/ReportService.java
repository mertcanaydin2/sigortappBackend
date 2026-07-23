package com.sigorta.backend.service;

import com.sigorta.backend.dto.report.CompanyPolicyCountReport;
import com.sigorta.backend.dto.report.DashboardReportResponse;
import com.sigorta.backend.dto.report.MonthlyProductionReport;
import com.sigorta.backend.repository.InsuranceRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final InsuranceRecordRepository insuranceRecordRepository;

    @Transactional(readOnly = true)
    public DashboardReportResponse getDashboardReport() {
        LocalDate today = LocalDate.now();
        LocalDate firstDayOfMonth = today.withDayOfMonth(1);
        LocalDate firstDayOfNextMonth = firstDayOfMonth.plusMonths(1);
        BigDecimal monthlyTotalPremium = insuranceRecordRepository
                .sumNetPremiumByStartDateRange(firstDayOfMonth, firstDayOfNextMonth);

        List<MonthlyProductionReport> monthlyProduction = insuranceRecordRepository
                .findMonthlyGrossPremiumProduction()
                .stream()
                .map(projection -> new MonthlyProductionReport(
                        projection.getYear(),
                        projection.getMonth(),
                        formatPeriod(projection.getYear(), projection.getMonth()),
                        projection.getTotalGrossPremium() == null
                                ? BigDecimal.ZERO
                                : projection.getTotalGrossPremium()
                ))
                .toList();

        List<CompanyPolicyCountReport> companyDistribution = insuranceRecordRepository
                .countPoliciesByCompany()
                .stream()
                .map(projection -> new CompanyPolicyCountReport(
                        normalizeCompany(projection.getCompany()),
                        projection.getPolicyCount()
                ))
                .toList();

        return new DashboardReportResponse(
                insuranceRecordRepository.countDistinctCustomers(),
                insuranceRecordRepository.countActivePolicies(today),
                insuranceRecordRepository.countUpcomingRenewals(today, today.plusDays(30)),
                monthlyTotalPremium == null ? BigDecimal.ZERO : monthlyTotalPremium,
                monthlyProduction,
                companyDistribution
        );
    }

    private String formatPeriod(Integer year, Integer month) {
        if (year == null || month == null) {
            return "";
        }

        return "%04d-%02d".formatted(year, month);
    }

    private String normalizeCompany(String company) {
        if (company == null || company.isBlank()) {
            return "Bilinmeyen";
        }

        return company.trim();
    }
}
