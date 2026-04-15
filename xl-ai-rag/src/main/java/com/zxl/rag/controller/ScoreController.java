package com.zxl.rag.controller;

import com.zxl.rag.service.ReportScoreService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 报告评分控制器
 */
@RestController
@RequestMapping("/api/score")
public class ScoreController {

    private final ReportScoreService reportScoreService;

    public ScoreController(ReportScoreService reportScoreService) {
        this.reportScoreService = reportScoreService;
    }

    /**
     * 上传报告并评分
     */
    @PostMapping("/{caseId}")
    public ResponseEntity<Map<String, Object>> scoreReport(
            @PathVariable Long caseId,
            @RequestParam("file") MultipartFile file) {

        Map<String, Object> result = new HashMap<>();

        try {
            // 解析上传的文件
            String content = reportScoreService.getClass()
                    .getClassLoader()
                    .loadClass("com.zxl.rag.service.ApacheTikaParser")
                    .getMethod("parse", java.io.InputStream.class)
                    .invoke(null, file.getInputStream())
                    .toString();

            // 评分
            result = reportScoreService.scoreReport(caseId, content);

        } catch (Exception e) {
            result.put("success", false);
            result.put("error", "文件处理失败: " + e.getMessage());
        }

        return ResponseEntity.ok(result);
    }

    /**
     * 直接提交报告内容进行评分
     */
    @PostMapping("/{caseId}/content")
    public ResponseEntity<Map<String, Object>> scoreReportContent(
            @PathVariable Long caseId,
            @RequestBody Map<String, String> request) {

        String content = request.get("content");
        if (content == null || content.trim().isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "报告内容不能为空");
            return ResponseEntity.badRequest().body(error);
        }

        Map<String, Object> result = reportScoreService.scoreReport(caseId, content);
        return ResponseEntity.ok(result);
    }

    /**
     * 获取评分详情
     */
    @GetMapping("/{scoreId}")
    public ResponseEntity<Map<String, Object>> getScoreDetail(@PathVariable String scoreId) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "TODO: 实现评分详情查询");
        return ResponseEntity.ok(response);
    }
}
