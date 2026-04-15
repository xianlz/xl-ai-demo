package com.zxl.rag.repository;

import com.zxl.rag.entity.KnowledgeDoc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KnowledgeDocRepository extends JpaRepository<KnowledgeDoc, Long> {
    Optional<KnowledgeDoc> findByDocUuid(String docUuid);
    List<KnowledgeDoc> findByInsuranceType(String insuranceType);
    List<KnowledgeDoc> findByUploadStatus(String uploadStatus);
    boolean existsByDocUuid(String docUuid);
}
