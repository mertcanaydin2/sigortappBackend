package com.sigorta.backend.repository;

import com.sigorta.backend.dto.report.CompanyPolicyCountProjection;
import com.sigorta.backend.dto.report.MonthlyProductionProjection;
import com.sigorta.backend.entity.InsuranceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface InsuranceRecordRepository extends JpaRepository<InsuranceRecord, Long> {

    List<InsuranceRecord> findByTcTaxNo(String tcTaxNo);

    boolean existsByTcTaxNo(String tcTaxNo);

    boolean existsByPolicyNumber(String policyNumber);

    @Query("""
            select record
            from InsuranceRecord record
            where record.policyEndDate is null
               or record.policyEndDate >= :date
            order by record.policyEndDate asc
            """)
    List<InsuranceRecord> findActiveRecords(@Param("date") LocalDate date);

    List<InsuranceRecord> findAllByOrderByInsuredAsc();

    @Query("""
            select record
            from InsuranceRecord record
            where record.policyEndDate is not null
              and record.policyEndDate >= :date
            order by record.policyEndDate asc
            """)
    List<InsuranceRecord> findApproachingExpirations(@Param("date") LocalDate date);

    List<InsuranceRecord> findByPolicyEndDateBetween(LocalDate startDate, LocalDate endDate);

    @Query("SELECT i FROM InsuranceRecord i WHERE i.policyEndDate < CURRENT_DATE ORDER BY i.policyEndDate DESC")
    List<InsuranceRecord> findExpiredPolicies();

    @Query("""
            select count(distinct record.tcTaxNo)
            from InsuranceRecord record
            where record.tcTaxNo is not null
              and trim(record.tcTaxNo) <> ''
            """)
    long countDistinctCustomers();

    @Query("""
            select count(record)
            from InsuranceRecord record
            where record.policyEndDate is not null
              and record.policyEndDate >= :date
            """)
    long countActivePolicies(@Param("date") LocalDate date);

    @Query("""
            select count(record)
            from InsuranceRecord record
            where record.policyEndDate is not null
              and record.policyEndDate between :startDate and :endDate
            """)
    long countUpcomingRenewals(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("""
            select sum(record.netPremium)
            from InsuranceRecord record
            where record.startDate is not null
              and record.startDate >= :startDate
              and record.startDate < :endDate
            """)
    BigDecimal sumNetPremiumByStartDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("""
            select year(record.issueDate) as year,
                   month(record.issueDate) as month,
                   sum(record.grossPremium) as totalGrossPremium
            from InsuranceRecord record
            where record.issueDate is not null
            group by year(record.issueDate), month(record.issueDate)
            order by year(record.issueDate), month(record.issueDate)
            """)
    List<MonthlyProductionProjection> findMonthlyGrossPremiumProduction();

    @Query("""
            select coalesce(record.company, 'Bilinmeyen') as company,
                   count(record) as policyCount
            from InsuranceRecord record
            group by record.company
            order by count(record) desc
            """)
    List<CompanyPolicyCountProjection> countPoliciesByCompany();
}
