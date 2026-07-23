package com.sigorta.backend.service;

import com.sigorta.backend.entity.InsuranceRecord;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PdfParsingServiceTest {

    private static final String AXA_TEXT = """
            AXA GEN\u0130\u015eLET\u0130LM\u0130\u015e MAKS\u0130MUM KASKO POL\u0130\u00c7ES\u0130
            Tanzim Tarihi
            | : 14/07/2026
            Ba\u015flang\u0131\u00e7 Tarihi:
            |
            : 15/07/2026
            Biti\u015f Tarihi:
            | :
            15/07/2027
            Sigortal\u0131n\u0131n Ad\u0131 Soyad\u0131
            |
            MERTCAN KAYA
            Kimlik No:
            | 12345678901
            Telefon
            | 05551234567
            E-posta
            | mertcan@example.com
            Poli\u00e7e No:
            | : 690935627
            Plaka No
            | 35ABC123
            Net Prim
            | 26.544,67
            \u00d6denecek Prim
            |
            31.000,50
            """;

    private final PdfParsingService pdfParsingService = new PdfParsingService();

    @Test
    void parsesAxaValuesWhenLabelsAndValuesAreOnDifferentLines() {
        InsuranceRecord record = pdfParsingService.parseExtractedText(AXA_TEXT);

        assertEquals(LocalDate.of(2026, 7, 14), record.getIssueDate());
        assertEquals(LocalDate.of(2026, 7, 15), record.getStartDate());
        assertEquals(LocalDate.of(2027, 7, 15), record.getPolicyEndDate());
        assertEquals("690935627", record.getPolicyNumber());
        assertEquals("MERTCAN KAYA", record.getInsured());
        assertEquals("12345678901", record.getTcTaxNo());
        assertEquals("35ABC123", record.getPlateNumber());
        assertEquals("05551234567", record.getPhoneNumber());
        assertEquals("mertcan@example.com", record.getEmail());
        assertEquals(new BigDecimal("26544.67"), record.getNetPremium());
        assertEquals(new BigDecimal("31000.50"), record.getGrossPremium());
        assertTrue(pdfParsingService.canAutoSave(record));
    }

    @Test
    void rejectsEverySupportedMaskCharacterDuringParsing() {
        for (String mask : new String[]{"*", "$", "^", "{", "}", "~"}) {
            InsuranceRecord record = pdfParsingService.parseExtractedText(
                    AXA_TEXT.replace("12345678901", "12345" + mask + "67890")
            );

            assertNull(record.getTcTaxNo(), "Mask should be rejected: " + mask);
            assertEquals(LocalDate.of(2026, 7, 14), record.getIssueDate());
            assertEquals(LocalDate.of(2026, 7, 15), record.getStartDate());
            assertEquals(LocalDate.of(2027, 7, 15), record.getPolicyEndDate());
            assertEquals("MERTCAN KAYA", record.getInsured());
            assertEquals("690935627", record.getPolicyNumber());
            assertEquals(new BigDecimal("26544.67"), record.getNetPremium());
            assertEquals(new BigDecimal("31000.50"), record.getGrossPremium());
            assertFalse(pdfParsingService.canAutoSave(record));
        }
    }

    @Test
    void maskOutsideCapturedValuesDoesNotAffectParsedRecord() {
        InsuranceRecord record = pdfParsingService.parseExtractedText(
                AXA_TEXT + System.lineSeparator() + "A\u00e7\u0131klama: ***"
        );

        assertEquals("12345678901", record.getTcTaxNo());
        assertEquals("35ABC123", record.getPlateNumber());
        assertEquals("690935627", record.getPolicyNumber());
        assertTrue(pdfParsingService.canAutoSave(record));
    }

    @Test
    void maskedPlatePhoneAndEmailBecomeNullAndRequireManualInput() {
        InsuranceRecord record = pdfParsingService.parseExtractedText(
                AXA_TEXT
                        .replace("35ABC123", "35A{C123")
                        .replace("05551234567", "0555^123456")
                        .replace("mertcan@example.com", "mertcan$example.com")
        );

        assertNull(record.getPlateNumber());
        assertNull(record.getPhoneNumber());
        assertNull(record.getEmail());
        assertFalse(pdfParsingService.canAutoSave(record));
    }
}
