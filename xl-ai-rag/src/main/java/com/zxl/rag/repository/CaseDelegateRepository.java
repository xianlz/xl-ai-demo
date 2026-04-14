package com.zxl.rag.repository;

import com.zxl.rag.entity.CaseDelegate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CaseDelegateRepository extends JpaRepository<CaseDelegate, Long> {
    Optional<CaseDelegate> findByInsuranceCaseId(Long caseId);
}
