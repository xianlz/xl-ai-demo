package com.zxl.rag.controller;

import com.zxl.rag.entity.KnowledgeDoc;
import com.zxl.rag.service.DocumentService;
import com.zxl.rag.service.FileUploadService;
import com.zxl.rag.service.RAGService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/knowledge")
@AllArgsConstructor
public class KnowledgeController {

    private final FileUploadService fileUploadService;
    private final DocumentService documentService;
    private final RAGService ragService;

    /**
     * 用户上传文件到知识库（使用Apache Tika解析）
     * @param file 上传的文件
     * @param insuranceType 险种类型
     * @param subType 子类型
     * @param userId 用户ID
     * @return 处理结果
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("insuranceType") String insuranceType,
            @RequestParam(value = "subType", required = false) String subType,
            @RequestParam("userId") String userId) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("msg", "文件不能为空"));
        }

        Map<String, Object> result = documentService.uploadDocument(file, insuranceType, subType, userId);

        if (Boolean.TRUE.equals(result.get("success"))) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 删除知识库文档
     * @param docId 文档UUID
     */
    @DeleteMapping("/{docId}")
    public ResponseEntity<Map<String, Object>> deleteDocument(@PathVariable String docId) {
        try {
            documentService.deleteDocument(docId);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "文档删除成功");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 获取知识库文档列表
     * @param insuranceType 险种类型（可选）
     */
    @GetMapping("/docs")
    public ResponseEntity<Map<String, Object>> getDocumentList(
            @RequestParam(value = "insuranceType", required = false) String insuranceType) {
        List<KnowledgeDoc> docs = documentService.getDocumentList(insuranceType);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("documents", docs);
        result.put("total", docs.size());
        return ResponseEntity.ok(result);
    }

    /**
     * 知识库问答
     */
    @GetMapping("/know")
    public String know(@RequestParam("userQuestion") String userQuestion, @RequestParam("topK") Long topK) {
        return ragService.answerWithKnowledge(userQuestion, topK);
    }
}