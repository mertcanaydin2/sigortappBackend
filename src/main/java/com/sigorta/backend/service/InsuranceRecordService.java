package com.sigorta.backend.service;

import com.sigorta.backend.entity.InsuranceRecord;
import com.sigorta.backend.exception.DuplicatePolicyNumberException;
import com.sigorta.backend.exception.ResourceNotFoundException;
import com.sigorta.backend.repository.InsuranceRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class InsuranceRecordService {

    private final InsuranceRecordRepository insuranceRecordRepository;

    @Transactional(readOnly = true)
    public List<InsuranceRecord> getAllInsuranceRecords() {
        return insuranceRecordRepository.findAll();
    }

    @Transactional(readOnly = true)
    public InsuranceRecord getInsuranceRecordById(Long id) {
        return insuranceRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Insurance record not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<String> getCrossSellOpportunities(String tcTaxNo) {
        if (tcTaxNo == null || tcTaxNo.isBlank()) {
            return List.of();
        }

        List<InsuranceRecord> customerRecords = insuranceRecordRepository.findByTcTaxNo(tcTaxNo.trim());
        boolean hasTrafficInsurance = hasPolicyType(customerRecords, "TRAFIK");
        boolean hasCasco = hasPolicyType(customerRecords, "KASKO");
        boolean hasDask = hasPolicyType(customerRecords, "DASK");
        boolean hasHomeInsurance = hasPolicyType(customerRecords, "KONUT");

        List<String> opportunities = new ArrayList<>();

        if (hasTrafficInsurance && !hasCasco) {
            opportunities.add("Müşterinin Trafik Sigortası var, Kasko teklifi için satış fırsatı oluştu.");
        }

        if (hasDask && !hasHomeInsurance) {
            opportunities.add("Müşterinin DASK poliçesi var, Konut Sigortası teklifi için satış fırsatı oluştu.");
        }

        return opportunities;
    }

    @Transactional(readOnly = true)
    public List<InsuranceRecord> getActiveInsuranceRecords() {
        return insuranceRecordRepository.findActiveRecords(LocalDate.now());
    }

    @Transactional(readOnly = true)
    public List<InsuranceRecord> getInsuranceRecordsSortedByInsured() {
        return insuranceRecordRepository.findAllByOrderByInsuredAsc();
    }

    @Transactional(readOnly = true)
    public List<InsuranceRecord> getInsuranceRecordsSortedByPolicyEndDate() {
        return insuranceRecordRepository.findApproachingExpirations(LocalDate.now());
    }

    @Transactional(readOnly = true)
    public List<InsuranceRecord> getInsuranceRecordsExpiringWithinSevenDays() {
        LocalDate today = LocalDate.now();
        return insuranceRecordRepository.findByPolicyEndDateBetween(today, today.plusDays(7));
    }

    @Transactional(readOnly = true)
    public List<InsuranceRecord> getExpiredInsuranceRecords() {
        return insuranceRecordRepository.findExpiredPolicies();
    }

    @Transactional(readOnly = true)
    public boolean existsByTcTaxNo(String tcTaxNo) {
        return tcTaxNo != null
                && !tcTaxNo.isBlank()
                && insuranceRecordRepository.existsByTcTaxNo(tcTaxNo.trim());
    }

    public InsuranceRecord createInsuranceRecord(InsuranceRecord insuranceRecord) {
        validateUniquePolicyNumber(insuranceRecord);
        insuranceRecord.setId(null);
        return insuranceRecordRepository.save(insuranceRecord);
    }

    public InsuranceRecord updateInsuranceRecord(Long id, InsuranceRecord updatedRecord) {
        InsuranceRecord existingRecord = getInsuranceRecordById(id);

        existingRecord.setIssueDate(updatedRecord.getIssueDate());
        existingRecord.setStartDate(updatedRecord.getStartDate());
        existingRecord.setPolicyEndDate(updatedRecord.getPolicyEndDate());
        existingRecord.setPolicyType(updatedRecord.getPolicyType());
        existingRecord.setCompany(updatedRecord.getCompany());
        existingRecord.setPlateNumber(updatedRecord.getPlateNumber());
        existingRecord.setDocumentSerial(updatedRecord.getDocumentSerial());
        existingRecord.setPolicyNumber(updatedRecord.getPolicyNumber());
        existingRecord.setTcTaxNo(updatedRecord.getTcTaxNo());
        existingRecord.setInsured(updatedRecord.getInsured());
        existingRecord.setNetPremium(updatedRecord.getNetPremium());
        existingRecord.setGrossPremium(updatedRecord.getGrossPremium());
        existingRecord.setPaymentMethod(updatedRecord.getPaymentMethod());
        existingRecord.setPhoneNumber(updatedRecord.getPhoneNumber());
        existingRecord.setEmail(updatedRecord.getEmail());

        return insuranceRecordRepository.save(existingRecord);
    }

    public void deleteInsuranceRecord(Long id) {
        InsuranceRecord existingRecord = getInsuranceRecordById(id);
        insuranceRecordRepository.delete(existingRecord);
    }

    private boolean hasPolicyType(List<InsuranceRecord> records, String keyword) {
        return records.stream()
                .map(InsuranceRecord::getPolicyType)
                .filter(Objects::nonNull)
                .map(this::normalizePolicyType)
                .anyMatch(policyType -> policyType.contains(keyword));
    }

    private String normalizePolicyType(String policyType) {
        return Normalizer.normalize(policyType, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("\\s+", " ")
                .trim()
                .toUpperCase(Locale.ROOT);
    }

    private void validateUniquePolicyNumber(InsuranceRecord insuranceRecord) {
        if (insuranceRecord == null
                || insuranceRecord.getPolicyNumber() == null
                || insuranceRecord.getPolicyNumber().isBlank()) {
            return;
        }

        if (insuranceRecordRepository.existsByPolicyNumber(insuranceRecord.getPolicyNumber().trim())) {
            throw new DuplicatePolicyNumberException("Bu poliçe numarası sistemde zaten kayıtlı");
        }
    }
}
