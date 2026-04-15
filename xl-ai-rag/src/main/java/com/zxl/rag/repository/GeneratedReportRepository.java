package com.zxl.rag.repository;

import com.zxl.rag.entity.GeneratedReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GeneratedReportRepository extends JpaRepository<GeneratedReport, Long> {
    Optional<GeneratedReport> findByReportNumber(String reportNumber);
    List<GeneratedReport> findByInsuranceCaseId(Long caseId);
    boolean existsByReportNumber(String reportNumber);
}
