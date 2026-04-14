package com.zxl.rag.repository;

import com.zxl.rag.entity.CaseSurvey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CaseSurveyRepository extends JpaRepository<CaseSurvey, Long> {
    Optional<CaseSurvey> findByInsuranceCaseId(Long caseId);
}
