package com.sigorta.backend.service;

import com.sigorta.backend.entity.InsuranceRecord;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PdfParsingService {

    private static final String ISSUE_DATE_LABEL = "D[\u00fc\u00dcUu]zenleme\\s*Tarihi|Tanzim\\s*Tarihi";
    private static final String START_DATE_LABEL = "Poli[\u00e7\u00c7cC]e\\s*Ba[\u015f\u015eSs]lang[\u0131\u0130Ii][\u00e7\u00c7cC]\\s*Tarihi|Ba[\u015f\u015eSs]lang[\u0131\u0130Ii][\u00e7\u00c7cC]\\s*Tarihi|Vade\\s*Ba[\u015f\u015eSs]lang[\u0131\u0130Ii]c[\u0131\u0130Ii]";
    private static final String END_DATE_LABEL = "Poli[\u00e7\u00c7cC]e\\s*Biti[\u015f\u015eSs]\\s*Tarihi|Biti[\u015f\u015eSs]\\s*Tarihi|Vade\\s*Biti[\u015f\u015eSs]i|Vade\\s*Sonu|Poli[\u00e7\u00c7cC]e\\s*Sonlanma\\s*Tarihi";
    private static final String POLICY_TYPE_LABEL = "Poli[\u00e7\u00c7cC]e\\s*T[\u00fc\u00dcUu]r[\u00fc\u00dcUu]|Bran[\u015f\u015eSs]|[\u00dc\u00fcUu]r[\u00fc\u00dcUu]n";
    private static final String COMPANY_LABEL = "Firma|Sigorta\\s*[\u015e\u015fSs]irketi|[\u015e\u015fSs]irket";
    private static final String PLATE_NUMBER_LABEL = "Plaka|Ara[\u00e7\u00c7cC]\\s*Plaka|Plaka\\s*No";
    private static final String DOCUMENT_SERIAL_LABEL = "Belge\\s*Seri|Belge\\s*Seri\\s*No|Seri\\s*No";
    private static final String POLICY_NUMBER_LABEL = "Poli[\u00e7\u00c7cC]e\\s*No|Poli[\u00e7\u00c7cC]e\\s*Numaras[\u0131\u0130Ii]|Poli[\u00e7\u00c7cC]e\\s*Numara";
    private static final String TC_TAX_NO_LABEL = "TC\\s*Kimlik\\s*(?:&|/|ve)?\\s*Vergi\\s*No|T\\.C\\.\\s*Kimlik\\s*No|Vergi\\s*No|TCKN\\s*/\\s*VKN|Kimlik\\s*No";
    private static final String INSURED_LABEL = "Sigortal[\u0131\u0130Ii]|Sigorta\\s*Ettiren|M[\u00fc\u00dcUu][\u015f\u015eSs]teri";
    private static final String NET_PREMIUM_LABEL = "Net\\s*Prim(?:\\s*\\([^)]*\\))?";
    private static final String GROSS_PREMIUM_LABEL = "Br[\u00fc\u00dcUu]t\\s*Prim(?:\\s*\\([^)]*\\))?|Toplam\\s*Prim";
    private static final String PAYMENT_METHOD_LABEL = "[\u00d6\u00f6Oo]deme\\s*[\u015e\u015fSs]ekli|[\u00d6\u00f6Oo]deme\\s*Tipi|Tahsilat\\s*[\u015e\u015fSs]ekli";
    private static final String PHONE_NUMBER_LABEL = "Cep\\s*Telefonu|Telefon|GSM";
    private static final String EMAIL_LABEL = "E-?posta|E-?mail";

    private static final String ALL_LABELS = String.join("|",
            ISSUE_DATE_LABEL,
            START_DATE_LABEL,
            END_DATE_LABEL,
            POLICY_TYPE_LABEL,
            COMPANY_LABEL,
            PLATE_NUMBER_LABEL,
            DOCUMENT_SERIAL_LABEL,
            POLICY_NUMBER_LABEL,
            TC_TAX_NO_LABEL,
            INSURED_LABEL,
            NET_PREMIUM_LABEL,
            GROSS_PREMIUM_LABEL,
            PAYMENT_METHOD_LABEL,
            PHONE_NUMBER_LABEL,
            EMAIL_LABEL
    );

    private static final String DATE_VALUE = "\\d{1,2}[./-]\\d{1,2}[./-]\\d{2,4}|\\d{4}-\\d{1,2}-\\d{1,2}";
    private static final String MONEY_VALUE = "(?:TL|TRY|\u20ba)?\\s*-?\\d[\\d.,\\s]*(?:\\s*(?:TL|TRY|\u20ba))?";
    private static final Pattern PLATE_PATTERN = Pattern.compile("^(0[1-9]|[1-7][0-9]|8[01])[A-Z]{1,3}[0-9]{2,4}$");
    private static final Pattern AXA_POLICY_TYPE_PATTERN = Pattern.compile(
            "AXA\\s+.*POL[\u0130I][\u00c7C]ES[\u0130I]",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
    );
    private static final String AXA_STATIC_POLICY_TYPE = "AXA GEN\u0130\u015eLET\u0130LM\u0130\u015e MAKS\u0130MUM KASKO POL\u0130\u00c7ES\u0130";
    private static final String AXA_COMPANY = "AXA S\u0130GORTA A\u015e";

    private static final Pattern AXA_POLICY_NUMBER_PATTERN = Pattern.compile(
            "Poliçe No:\\s*\\|\\s*:\\s*(\\d+)"
    );
    private static final Pattern AXA_START_DATE_PATTERN = Pattern.compile(
            "Başlangıç Tarihi:\\s*\\|\\s*:\\s*(\\d{2}/\\d{2}/\\d{4})"
    );
    private static final Pattern AXA_END_DATE_PATTERN = Pattern.compile(
            "Bitiş Tarihi:\\s*\\|\\s*:\\s*(\\d{2}/\\d{2}/\\d{4})"
    );
    private static final Pattern AXA_ISSUE_DATE_PATTERN = Pattern.compile(
            "Tanzim Tarihi\\s*\\|\\s*:\\s*(\\d{2}/\\d{2}/\\d{4})"
    );
    private static final Pattern AXA_INSURED_PATTERN = Pattern.compile(
            "Sigortalının Adı Soyadı\\s*\\|\\s*([^\\r\\n]+)"
    );
    private static final Pattern AXA_NET_PREMIUM_PATTERN = Pattern.compile(
            "Net Prim\\s*\\|\\s*([\\d.,]+)"
    );
    private static final Pattern AXA_GROSS_PREMIUM_PATTERN = Pattern.compile(
            "Ödenecek Prim\\s*\\|\\s*([\\d.,]+)"
    );
    private static final Pattern AXA_TC_TAX_NO_PATTERN = Pattern.compile(
            "Kimlik No:\\s*\\|\\s*([^\\r\\n]+)"
    );
    private static final Pattern AXA_PHONE_NUMBER_PATTERN = Pattern.compile(
            "(?:Cep Telefonu|Telefon|GSM)\\s*:?\\s*\\|\\s*:?\\s*([^\\r\\n]+)"
    );
    private static final Pattern AXA_EMAIL_PATTERN = Pattern.compile(
            "(?:E-?posta|E-?mail)\\s*:?\\s*\\|\\s*:?\\s*([^\\r\\n]+)",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
    );
    private static final Pattern AXA_PLATE_NUMBER_PATTERN = Pattern.compile(
            "Plaka No\\s*\\|\\s*([A-Z0-9*\\$\\^{}~\\- ]+)"
    );
    private static final Pattern MASK_CHARACTER_PATTERN = Pattern.compile("[*\\$\\^{}~]");

    private static final List<DateTimeFormatter> DATE_FORMATTERS = List.of(
            DateTimeFormatter.ofPattern("d.M.uuuu"),
            DateTimeFormatter.ofPattern("d/M/uuuu"),
            DateTimeFormatter.ofPattern("d-M-uuuu"),
            DateTimeFormatter.ofPattern("uuuu-M-d")
    );

    private static final List<DateTimeFormatter> SHORT_YEAR_DATE_FORMATTERS = List.of(
            shortYearFormatter("d.M."),
            shortYearFormatter("d/M/"),
            shortYearFormatter("d-M-")
    );

    private static final DateTimeFormatter AXA_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/uuuu");

    public InsuranceRecord parsePdf(MultipartFile file) throws IOException {
        validatePdfFile(file);

        String extractedText = extractTextFromPdf(file);
        return parseExtractedText(extractedText);
    }

    InsuranceRecord parseExtractedText(String extractedText) {
        String normalizedText = normalizeText(extractedText);

        if (isAxaPolicy(normalizedText)) {
            return parseAxaPolicy(normalizedText);
        }

        return InsuranceRecord.builder()
                .issueDate(extractDate(normalizedText, ISSUE_DATE_LABEL).orElse(null))
                .startDate(extractDate(normalizedText, START_DATE_LABEL).orElse(null))
                .policyEndDate(extractDate(normalizedText, END_DATE_LABEL).orElse(null))
                .policyType(extractTextValue(normalizedText, POLICY_TYPE_LABEL).orElse(null))
                .company(extractTextValue(normalizedText, COMPANY_LABEL).orElse(null))
                .plateNumber(extractTextValue(normalizedText, PLATE_NUMBER_LABEL).orElse(null))
                .documentSerial(extractTextValue(normalizedText, DOCUMENT_SERIAL_LABEL).orElse(null))
                .policyNumber(extractTextValue(normalizedText, POLICY_NUMBER_LABEL).orElse(null))
                .tcTaxNo(extractTextValue(normalizedText, TC_TAX_NO_LABEL).orElse(null))
                .insured(extractTextValue(normalizedText, INSURED_LABEL).orElse(null))
                .netPremium(extractMoney(normalizedText, NET_PREMIUM_LABEL).orElse(null))
                .grossPremium(extractMoney(normalizedText, GROSS_PREMIUM_LABEL).orElse(null))
                .paymentMethod(extractTextValue(normalizedText, PAYMENT_METHOD_LABEL).orElse(null))
                .phoneNumber(extractTextValue(normalizedText, PHONE_NUMBER_LABEL).orElse(null))
                .email(extractTextValue(normalizedText, EMAIL_LABEL).orElse(null))
                .build();
    }

    public boolean canAutoSave(InsuranceRecord insuranceRecord) {
        return insuranceRecord != null
                && hasRequiredFields(insuranceRecord)
                && isValidTcTaxNo(insuranceRecord.getTcTaxNo())
                && isValidPlateNumber(insuranceRecord.getPlateNumber());
    }

    public InsuranceRecord sanitizeForManualInput(InsuranceRecord insuranceRecord) {
        if (insuranceRecord == null) {
            return null;
        }

        String tcTaxNo = sanitizeTextValue(insuranceRecord.getTcTaxNo());
        String plateNumber = sanitizeTextValue(insuranceRecord.getPlateNumber());

        return InsuranceRecord.builder()
                .issueDate(insuranceRecord.getIssueDate())
                .startDate(insuranceRecord.getStartDate())
                .policyEndDate(insuranceRecord.getPolicyEndDate())
                .policyType(sanitizeTextValue(insuranceRecord.getPolicyType()))
                .company(sanitizeTextValue(insuranceRecord.getCompany()))
                .plateNumber(isValidPlateNumber(plateNumber) ? plateNumber : null)
                .documentSerial(sanitizeTextValue(insuranceRecord.getDocumentSerial()))
                .policyNumber(sanitizeTextValue(insuranceRecord.getPolicyNumber()))
                .tcTaxNo(isValidTcTaxNo(tcTaxNo) ? tcTaxNo : null)
                .insured(sanitizeTextValue(insuranceRecord.getInsured()))
                .netPremium(insuranceRecord.getNetPremium())
                .grossPremium(insuranceRecord.getGrossPremium())
                .paymentMethod(sanitizeTextValue(insuranceRecord.getPaymentMethod()))
                .phoneNumber(sanitizeTextValue(insuranceRecord.getPhoneNumber()))
                .email(sanitizeTextValue(insuranceRecord.getEmail()))
                .build();
    }

    private void validatePdfFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded PDF file is empty.");
        }

        String originalFilename = Optional.ofNullable(file.getOriginalFilename())
                .orElse("")
                .toLowerCase(Locale.ROOT);
        String contentType = Optional.ofNullable(file.getContentType()).orElse("");

        if (!originalFilename.endsWith(".pdf") && !"application/pdf".equalsIgnoreCase(contentType)) {
            throw new IllegalArgumentException("Only PDF files are supported.");
        }
    }

    private String extractTextFromPdf(MultipartFile file) throws IOException {
        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            return new PDFTextStripper().getText(document);
        }
    }

    private String normalizeText(String text) {
        return Optional.ofNullable(text)
                .orElse("")
                .replace('\u00A0', ' ')
                .replaceAll("[\\t ]+", " ")
                .replaceAll("\\R[\\t ]+", System.lineSeparator())
                .trim();
    }

    private Optional<String> extractTextValue(String text, String labelPattern) {
        String valuePattern = "(?:" + labelPattern + ")\\s*[:\\-]?\\s*(.+?)(?=\\s+(?:" + ALL_LABELS + ")\\s*[:\\-]?|\\R|$)";
        Matcher matcher = compile(valuePattern, Pattern.DOTALL | Pattern.MULTILINE).matcher(text);

        if (!matcher.find()) {
            return Optional.empty();
        }

        String value = cleanupValue(matcher.group(1));
        return value.isBlank() || containsMask(value) ? Optional.empty() : Optional.of(value);
    }

    private Optional<LocalDate> extractDate(String text, String labelPattern) {
        String datePattern = "(?:" + labelPattern + ")\\s*[:\\-]?\\s*(" + DATE_VALUE + ")";
        Matcher matcher = compile(datePattern, Pattern.MULTILINE).matcher(text);

        if (!matcher.find()) {
            return Optional.empty();
        }

        return parseDate(matcher.group(1));
    }

    private Optional<BigDecimal> extractMoney(String text, String labelPattern) {
        String moneyPattern = "(?:" + labelPattern + ")\\s*[:\\-]?\\s*(" + MONEY_VALUE + ")";
        Matcher matcher = compile(moneyPattern, Pattern.MULTILINE).matcher(text);

        if (!matcher.find()) {
            return Optional.empty();
        }

        return parseMoney(matcher.group(1));
    }

    private InsuranceRecord parseAxaPolicy(String text) {
        return InsuranceRecord.builder()
                .issueDate(extractAxaDate(text, AXA_ISSUE_DATE_PATTERN).orElse(null))
                .startDate(extractAxaDate(text, AXA_START_DATE_PATTERN).orElse(null))
                .policyEndDate(extractAxaDate(text, AXA_END_DATE_PATTERN).orElse(null))
                .policyType(extractAxaPolicyType(text).orElse(null))
                .company(AXA_COMPANY)
                .plateNumber(extractAxaTextValue(text, AXA_PLATE_NUMBER_PATTERN).orElse(null))
                .policyNumber(extractAxaTextValue(text, AXA_POLICY_NUMBER_PATTERN).orElse(null))
                .tcTaxNo(extractAxaTextValue(text, AXA_TC_TAX_NO_PATTERN).orElse(null))
                .insured(extractAxaTextValue(text, AXA_INSURED_PATTERN).orElse(null))
                .netPremium(extractAxaMoney(text, AXA_NET_PREMIUM_PATTERN).orElse(null))
                .grossPremium(extractAxaMoney(text, AXA_GROSS_PREMIUM_PATTERN).orElse(null))
                .phoneNumber(extractAxaTextValue(text, AXA_PHONE_NUMBER_PATTERN).orElse(null))
                .email(extractAxaTextValue(text, AXA_EMAIL_PATTERN).orElse(null))
                .build();
    }

    private boolean isAxaPolicy(String text) {
        return text.toUpperCase(Locale.ROOT).contains("AXA")
                || text.contains(AXA_STATIC_POLICY_TYPE)
                || AXA_POLICY_NUMBER_PATTERN.matcher(text).find();
    }

    private Optional<String> extractAxaPolicyType(String text) {
        if (text.contains(AXA_STATIC_POLICY_TYPE)) {
            return Optional.of(AXA_STATIC_POLICY_TYPE);
        }

        Matcher matcher = AXA_POLICY_TYPE_PATTERN.matcher(text);
        return matcher.find()
                ? Optional.of(cleanupValue(matcher.group()))
                : Optional.empty();
    }

    private Optional<String> extractAxaTextValue(String text, Pattern pattern) {
        Matcher matcher = pattern.matcher(text);

        if (!matcher.find()) {
            return Optional.empty();
        }

        String value = cleanupValue(matcher.group(1));
        return value.isBlank() || containsMask(value) ? Optional.empty() : Optional.of(value);
    }

    private Optional<LocalDate> extractAxaDate(String text, Pattern pattern) {
        return extractAxaTextValue(text, pattern).flatMap(this::parseAxaDate);
    }

    private Optional<LocalDate> parseAxaDate(String value) {
        try {
            return Optional.of(LocalDate.parse(cleanupValue(value), AXA_DATE_FORMATTER));
        } catch (DateTimeParseException exception) {
            return Optional.empty();
        }
    }

    private Optional<BigDecimal> extractAxaMoney(String text, Pattern pattern) {
        return extractAxaTextValue(text, pattern).flatMap(this::parseAxaMoney);
    }

    private Optional<BigDecimal> parseAxaMoney(String rawValue) {
        String formattedValue = cleanupValue(rawValue)
                .replace(".", "")
                .replace(",", ".");

        if (formattedValue.isBlank()) {
            return Optional.empty();
        }

        try {
            return Optional.of(new BigDecimal(formattedValue));
        } catch (NumberFormatException exception) {
            return Optional.empty();
        }
    }

    private Optional<LocalDate> parseDate(String rawValue) {
        String value = cleanupValue(rawValue);

        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                return Optional.of(LocalDate.parse(value, formatter));
            } catch (DateTimeParseException ignored) {
                // Try the next policy date format.
            }
        }

        for (DateTimeFormatter formatter : SHORT_YEAR_DATE_FORMATTERS) {
            try {
                return Optional.of(LocalDate.parse(value, formatter));
            } catch (DateTimeParseException ignored) {
                // Try the next short-year policy date format.
            }
        }

        return Optional.empty();
    }

    private Optional<BigDecimal> parseMoney(String rawValue) {
        String value = cleanupValue(rawValue)
                .replaceAll("(?i)(TL|TRY)", "")
                .replace("\u20ba", "")
                .replace(" ", "");

        if (value.isBlank()) {
            return Optional.empty();
        }

        int lastComma = value.lastIndexOf(',');
        int lastDot = value.lastIndexOf('.');

        if (lastComma >= 0 && lastDot >= 0) {
            value = lastComma > lastDot
                    ? value.replace(".", "").replace(",", ".")
                    : value.replace(",", "");
        } else if (lastComma >= 0) {
            value = hasDecimalPart(value, ',')
                    ? value.substring(0, lastComma).replace(",", "") + "." + value.substring(lastComma + 1)
                    : value.replace(",", "");
        } else if (lastDot >= 0 && countOccurrences(value, '.') > 1) {
            value = hasDecimalPart(value, '.')
                    ? value.substring(0, lastDot).replace(".", "") + "." + value.substring(lastDot + 1)
                    : value.replace(".", "");
        }

        try {
            return Optional.of(new BigDecimal(value));
        } catch (NumberFormatException exception) {
            return Optional.empty();
        }
    }

    private String cleanupValue(String value) {
        return Optional.ofNullable(value)
                .orElse("")
                .replaceAll("\\s+", " ")
                .replaceAll("^[\\s:;\\-]+|[\\s:;\\-]+$", "")
                .trim();
    }

    private boolean hasDecimalPart(String value, char separator) {
        int separatorIndex = value.lastIndexOf(separator);
        return separatorIndex >= 0 && value.length() - separatorIndex - 1 == 2;
    }

    private int countOccurrences(String value, char character) {
        int count = 0;

        for (int index = 0; index < value.length(); index++) {
            if (value.charAt(index) == character) {
                count++;
            }
        }

        return count;
    }

    private Pattern compile(String pattern, int flags) {
        return Pattern.compile(pattern, flags | Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    }

    private boolean hasRequiredFields(InsuranceRecord insuranceRecord) {
        return insuranceRecord.getIssueDate() != null
                && insuranceRecord.getStartDate() != null
                && insuranceRecord.getPolicyEndDate() != null
                && hasText(insuranceRecord.getPolicyType())
                && hasText(insuranceRecord.getCompany())
                && hasText(insuranceRecord.getPlateNumber())
                && hasText(insuranceRecord.getPolicyNumber())
                && hasText(insuranceRecord.getTcTaxNo())
                && hasText(insuranceRecord.getInsured())
                && hasText(insuranceRecord.getPhoneNumber())
                && hasText(insuranceRecord.getEmail());
    }

    private boolean containsMask(String value) {
        return value != null && MASK_CHARACTER_PATTERN.matcher(value).find();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private boolean isValidTcTaxNo(String value) {
        if (!hasText(value)) {
            return false;
        }

        String digits = value.replaceAll("\\D", "");
        return digits.length() >= 11;
    }

    private boolean isValidPlateNumber(String value) {
        if (!hasText(value)) {
            return false;
        }

        String normalizedPlate = value
                .replaceAll("[\\s\\-]", "")
                .toUpperCase(Locale.ROOT);

        return PLATE_PATTERN.matcher(normalizedPlate).matches();
    }

    private String sanitizeTextValue(String value) {
        if (!hasText(value) || containsMask(value)) {
            return null;
        }

        return value.trim();
    }

    private static DateTimeFormatter shortYearFormatter(String dayMonthPattern) {
        return new DateTimeFormatterBuilder()
                .appendPattern(dayMonthPattern)
                .appendValueReduced(ChronoField.YEAR, 2, 2, 2000)
                .toFormatter();
    }
}
