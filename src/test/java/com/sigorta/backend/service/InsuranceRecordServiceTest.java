package com.sigorta.backend.service;

import com.sigorta.backend.entity.InsuranceRecord;
import com.sigorta.backend.repository.InsuranceRecordRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InsuranceRecordServiceTest {

    @Mock
    private InsuranceRecordRepository insuranceRecordRepository;

    @InjectMocks
    private InsuranceRecordService insuranceRecordService;

    @Test
    void returnsUpcomingRenewalsFromFutureOnlyRepositoryQuery() {
        List<InsuranceRecord> upcomingRenewals = List.of(
                InsuranceRecord.builder().id(1L).policyEndDate(LocalDate.now().plusDays(1)).build(),
                InsuranceRecord.builder().id(2L).policyEndDate(LocalDate.now().plusDays(5)).build()
        );
        when(insuranceRecordRepository.findUpcomingRenewals()).thenReturn(upcomingRenewals);

        List<InsuranceRecord> result = insuranceRecordService.getUpcomingRenewals();

        assertSame(upcomingRenewals, result);
        verify(insuranceRecordRepository).findUpcomingRenewals();
    }

    @Test
    void updatesContactedStatusToTrueAndFalse() {
        InsuranceRecord insuranceRecord = InsuranceRecord.builder()
                .id(42L)
                .isContacted(false)
                .build();
        when(insuranceRecordRepository.findById(42L)).thenReturn(Optional.of(insuranceRecord));
        when(insuranceRecordRepository.save(insuranceRecord)).thenReturn(insuranceRecord);

        InsuranceRecord contactedRecord = insuranceRecordService.updateContactedStatus(42L, true);
        assertTrue(contactedRecord.getIsContacted());

        InsuranceRecord resetRecord = insuranceRecordService.updateContactedStatus(42L, false);
        assertFalse(resetRecord.getIsContacted());
        verify(insuranceRecordRepository, times(2)).save(insuranceRecord);
    }

    @Test
    void alwaysStartsNewPolicyAsNotContacted() {
        InsuranceRecord insuranceRecord = InsuranceRecord.builder()
                .policyNumber("POL-100")
                .isContacted(true)
                .build();
        when(insuranceRecordRepository.save(insuranceRecord)).thenReturn(insuranceRecord);

        InsuranceRecord savedRecord = insuranceRecordService.createInsuranceRecord(insuranceRecord);

        assertFalse(savedRecord.getIsContacted());
    }

    @Test
    void resetsContactedStatusWhenPolicyEndDateAdvancesToNewTerm() {
        InsuranceRecord existingRecord = InsuranceRecord.builder()
                .id(7L)
                .policyEndDate(LocalDate.of(2026, 9, 30))
                .isContacted(true)
                .build();
        InsuranceRecord renewedRecord = InsuranceRecord.builder()
                .policyEndDate(LocalDate.of(2027, 9, 30))
                .isContacted(true)
                .build();
        when(insuranceRecordRepository.findById(7L)).thenReturn(Optional.of(existingRecord));
        when(insuranceRecordRepository.save(existingRecord)).thenReturn(existingRecord);

        InsuranceRecord result = insuranceRecordService.updateInsuranceRecord(7L, renewedRecord);

        assertEquals(LocalDate.of(2027, 9, 30), result.getPolicyEndDate());
        assertFalse(result.getIsContacted());
    }

    @Test
    void preservesContactedStatusWhenPolicyTermDoesNotChange() {
        LocalDate policyEndDate = LocalDate.of(2026, 9, 30);
        InsuranceRecord existingRecord = InsuranceRecord.builder()
                .id(8L)
                .policyEndDate(policyEndDate)
                .isContacted(true)
                .build();
        InsuranceRecord editedRecord = InsuranceRecord.builder()
                .policyEndDate(policyEndDate)
                .insured("Updated customer name")
                .isContacted(null)
                .build();
        when(insuranceRecordRepository.findById(8L)).thenReturn(Optional.of(existingRecord));
        when(insuranceRecordRepository.save(existingRecord)).thenReturn(existingRecord);

        InsuranceRecord result = insuranceRecordService.updateInsuranceRecord(8L, editedRecord);

        assertTrue(result.getIsContacted());
    }
}
