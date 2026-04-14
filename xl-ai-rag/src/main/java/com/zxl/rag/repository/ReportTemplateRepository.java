package com.zxl.rag.repository;

import com.zxl.rag.entity.ReportTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReportTemplateRepository extends JpaRepository<ReportTemplate, String> {
    Optional<ReportTemplate> findByTemplateType(String templateType);
    Optional<ReportTemplate> findByIsDefaultTrue();
    List<ReportTemplate> findByActiveTrue();
}
