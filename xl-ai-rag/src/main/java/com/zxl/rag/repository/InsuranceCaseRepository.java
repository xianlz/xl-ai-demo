package com.zxl.rag.repository;

import com.zxl.rag.entity.InsuranceCase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InsuranceCaseRepository extends JpaRepository<InsuranceCase, Long> {
    Optional<InsuranceCase> findByCaseNumber(String caseNumber);
    boolean existsByCaseNumber(String caseNumber);
}
