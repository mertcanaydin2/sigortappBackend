package com.sigorta.backend.service;

import com.sigorta.backend.dto.report.DashboardReportResponse;
import com.sigorta.backend.repository.InsuranceRecordRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private InsuranceRecordRepository insuranceRecordRepository;

    @InjectMocks
    private ReportService reportService;

    @Test
    void shouldReturnReadyDashboardStatsWithExistingReportData() {
        LocalDate today = LocalDate.now();
        LocalDate firstDayOfMonth = today.withDayOfMonth(1);
        LocalDate firstDayOfNextMonth = firstDayOfMonth.plusMonths(1);

        when(insuranceRecordRepository.countDistinctCustomers()).thenReturn(42L);
        when(insuranceRecordRepository.countActivePolicies(today)).thenReturn(31L);
        when(insuranceRecordRepository.countUpcomingRenewals(today, today.plusDays(30))).thenReturn(7L);
        when(insuranceRecordRepository.sumNetPremiumByStartDateRange(firstDayOfMonth, firstDayOfNextMonth))
                .thenReturn(new BigDecimal("125000.50"));
        when(insuranceRecordRepository.findMonthlyGrossPremiumProduction()).thenReturn(List.of());
        when(insuranceRecordRepository.countPoliciesByCompany()).thenReturn(List.of());

        DashboardReportResponse result = reportService.getDashboardReport();

        assertThat(result.totalCustomers()).isEqualTo(42L);
        assertThat(result.totalActivePolicies()).isEqualTo(31L);
        assertThat(result.upcomingRenewals()).isEqualTo(7L);
        assertThat(result.monthlyTotalPremium()).isEqualByComparingTo("125000.50");
        assertThat(result.monthlyProduction()).isEmpty();
        assertThat(result.companyDistribution()).isEmpty();
    }

    @Test
    void shouldUseZeroWhenMonthlyPremiumSumIsNull() {
        when(insuranceRecordRepository.findMonthlyGrossPremiumProduction()).thenReturn(List.of());
        when(insuranceRecordRepository.countPoliciesByCompany()).thenReturn(List.of());

        DashboardReportResponse result = reportService.getDashboardReport();

        assertThat(result.monthlyTotalPremium()).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
