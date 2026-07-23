package com.sigorta.backend.controller;

import com.sigorta.backend.dto.report.DashboardReportResponse;
import com.sigorta.backend.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/api/reports/dashboard")
    public ResponseEntity<DashboardReportResponse> getDashboardReport() {
        return ResponseEntity.ok(reportService.getDashboardReport());
    }
}
