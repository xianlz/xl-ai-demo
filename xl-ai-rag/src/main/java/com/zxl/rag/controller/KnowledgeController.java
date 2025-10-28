package com.zxl.rag.controller;

import com.zxl.rag.service.FileUploadService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/knowledge")
@AllArgsConstructor
public class KnowledgeController {

    private final FileUploadService fileUploadService;

    /**
     * 用户上传文件到知识库
     * @param file 上传的文件
     * @param userId 用户ID（从登录态获取，此处简化为参数）
     * @return 处理结果
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") String userId) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("msg", "文件不能为空"));
        }

        int count = fileUploadService.handleUploadedFile(file, userId);
        Map<String, Object> result = new HashMap<>();
        result.put("msg", "文件导入成功");
        // 插入的向量文档数量
        result.put("documentCount", count);
        return ResponseEntity.ok(result);
    }
}