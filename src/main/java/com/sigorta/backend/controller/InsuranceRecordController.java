package com.sigorta.backend.controller;

import com.sigorta.backend.dto.InsuranceUploadResponse;
import com.sigorta.backend.entity.InsuranceRecord;
import com.sigorta.backend.service.InsuranceRecordService;
import com.sigorta.backend.service.PdfParsingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class InsuranceRecordController {

    private static final String AUTO_SAVED = "AUTO_SAVED";
    private static final String NEEDS_MANUAL_INPUT = "NEEDS_MANUAL_INPUT";

    private final InsuranceRecordService insuranceRecordService;
    private final PdfParsingService pdfParsingService;

    @GetMapping("/api/insurance-records")
    public ResponseEntity<List<InsuranceRecord>> getAllInsuranceRecords() {
        return ResponseEntity.ok(insuranceRecordService.getAllInsuranceRecords());
    }

    @GetMapping("/api/insurance-records/active")
    public ResponseEntity<List<InsuranceRecord>> getActiveInsuranceRecords() {
        return ResponseEntity.ok(insuranceRecordService.getActiveInsuranceRecords());
    }

    @GetMapping("/api/insurance-records/sorted-by-insured")
    public ResponseEntity<List<InsuranceRecord>> getInsuranceRecordsSortedByInsured() {
        return ResponseEntity.ok(insuranceRecordService.getInsuranceRecordsSortedByInsured());
    }

    @GetMapping("/api/insurance-records/sorted-by-policy-end-date")
    public ResponseEntity<List<InsuranceRecord>> getInsuranceRecordsSortedByPolicyEndDate() {
        return ResponseEntity.ok(insuranceRecordService.getInsuranceRecordsSortedByPolicyEndDate());
    }

    @GetMapping("/api/insurance-records/expiring-within-7-days")
    public ResponseEntity<List<InsuranceRecord>> getInsuranceRecordsExpiringWithinSevenDays() {
        return ResponseEntity.ok(insuranceRecordService.getInsuranceRecordsExpiringWithinSevenDays());
    }

    @GetMapping("/api/insurance/expired")
    public ResponseEntity<List<InsuranceRecord>> getExpiredInsuranceRecords() {
        return ResponseEntity.ok(insuranceRecordService.getExpiredInsuranceRecords());
    }

    @GetMapping("/api/insurance/upcoming-renewals")
    public ResponseEntity<List<InsuranceRecord>> getUpcomingRenewals() {
        return ResponseEntity.ok(insuranceRecordService.getUpcomingRenewals());
    }

    @GetMapping("/api/insurance/check-tc/{tcTaxNo}")
    public ResponseEntity<Boolean> checkTcTaxNo(@PathVariable String tcTaxNo) {
        return ResponseEntity.ok(insuranceRecordService.existsByTcTaxNo(tcTaxNo));
    }

    @GetMapping("/api/insurance/opportunities/{tcTaxNo}")
    public ResponseEntity<List<String>> getCrossSellOpportunities(@PathVariable String tcTaxNo) {
        return ResponseEntity.ok(insuranceRecordService.getCrossSellOpportunities(tcTaxNo));
    }

    @GetMapping("/api/insurance-records/{id}")
    public ResponseEntity<InsuranceRecord> getInsuranceRecordById(@PathVariable Long id) {
        return ResponseEntity.ok(insuranceRecordService.getInsuranceRecordById(id));
    }

    @PostMapping("/api/insurance-records")
    public ResponseEntity<InsuranceRecord> createInsuranceRecord(@RequestBody InsuranceRecord insuranceRecord) {
        InsuranceRecord createdRecord = insuranceRecordService.createInsuranceRecord(insuranceRecord);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRecord);
    }

    @PostMapping("/api/insurance")
    public ResponseEntity<InsuranceRecord> createInsurance(@RequestBody InsuranceRecord insuranceRecord) {
        InsuranceRecord createdRecord = insuranceRecordService.createInsuranceRecord(insuranceRecord);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRecord);
    }

    @PostMapping(value = "/api/insurance/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<InsuranceUploadResponse> uploadInsurancePdf(@RequestParam("file") MultipartFile file) {
        try {
            InsuranceRecord parsedRecord = pdfParsingService.parsePdf(file);

            if (pdfParsingService.canAutoSave(parsedRecord)) {
                InsuranceRecord savedRecord = insuranceRecordService.createInsuranceRecord(parsedRecord);
                return ResponseEntity.status(HttpStatus.CREATED)
                        .body(new InsuranceUploadResponse(AUTO_SAVED, savedRecord));
            }

            InsuranceRecord manualRecord = pdfParsingService.sanitizeForManualInput(parsedRecord);
            return ResponseEntity.ok(new InsuranceUploadResponse(NEEDS_MANUAL_INPUT, manualRecord));
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "PDF file could not be read.", exception);
        }
    }

    @PutMapping("/api/insurance-records/{id}")
    public ResponseEntity<InsuranceRecord> updateInsuranceRecord(
            @PathVariable Long id,
            @RequestBody InsuranceRecord insuranceRecord
    ) {
        return ResponseEntity.ok(insuranceRecordService.updateInsuranceRecord(id, insuranceRecord));
    }

    @PatchMapping("/api/insurance/{id}/contacted")
    public ResponseEntity<InsuranceRecord> updateContactedStatus(
            @PathVariable Long id,
            @RequestParam(defaultValue = "true") boolean contacted
    ) {
        return ResponseEntity.ok(insuranceRecordService.updateContactedStatus(id, contacted));
    }

    @DeleteMapping("/api/insurance-records/{id}")
    public ResponseEntity<Void> deleteInsuranceRecord(@PathVariable Long id) {
        insuranceRecordService.deleteInsuranceRecord(id);
        return ResponseEntity.noContent().build();
    }
}
