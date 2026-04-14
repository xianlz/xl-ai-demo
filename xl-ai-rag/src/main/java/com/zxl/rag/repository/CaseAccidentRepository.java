package com.zxl.rag.repository;

import com.zxl.rag.entity.CaseAccident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CaseAccidentRepository extends JpaRepository<CaseAccident, Long> {
    Optional<CaseAccident> findByInsuranceCaseId(Long caseId);
}
