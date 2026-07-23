package com.sigorta.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "insurance_records")
public class InsuranceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "issue_date")
    private LocalDate issueDate;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "policy_end_date")
    private LocalDate policyEndDate;

    @Builder.Default
    @ColumnDefault("false")
    @Column(name = "is_contacted", nullable = false)
    private Boolean isContacted = false;

    @Column(name = "policy_type")
    private String policyType;

    private String company;

    @Column(name = "plate_number")
    private String plateNumber;

    @Column(name = "document_serial")
    private String documentSerial;

    @Column(name = "policy_number")
    private String policyNumber;

    @Column(name = "tc_tax_no")
    private String tcTaxNo;

    private String insured;

    @Column(name = "net_premium", precision = 12, scale = 2)
    private BigDecimal netPremium;

    @Column(name = "gross_premium", precision = 12, scale = 2)
    private BigDecimal grossPremium;

    @Column(name = "payment_method")
    private String paymentMethod;

    @Column(name = "phone_number")
    private String phoneNumber;

    private String email;

    @PrePersist
    private void initializeContactedStatus() {
        isContacted = false;
    }
}
