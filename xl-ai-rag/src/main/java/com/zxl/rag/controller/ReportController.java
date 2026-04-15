package com.zxl.rag.controller;

import com.zxl.rag.entity.GeneratedReport;
import com.zxl.rag.service.ReportGenerateService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 报告控制器
 */
@RestController
@RequestMapping("/api/report")
public class ReportController {

    private final ReportGenerateService reportGenerateService;

    public ReportController(ReportGenerateService reportGenerateService) {
        this.reportGenerateService = reportGenerateService;
    }

    /**
     * 生成报告
     */
    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generateReport(
            @RequestParam Long caseId,
            @RequestParam(required = false) String templateId) {

        Map<String, Object> result = reportGenerateService.generateReport(caseId, templateId);
        return ResponseEntity.ok(result);
    }

    /**
     * 下载报告
     */
    @GetMapping("/{reportId}/download")
    public ResponseEntity<Resource> downloadReport(@PathVariable Long reportId) {
        try {
            Path filePath = reportGenerateService.getReportFilePath(reportId);

            if (!filePath.toFile().exists()) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new FileSystemResource(filePath);
            String fileName = filePath.getFileName().toString();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取报告详情
     */
    @GetMapping("/{reportId}")
    public ResponseEntity<Map<String, Object>> getReport(@PathVariable Long reportId) {
        return reportGenerateService.getReport(reportId)
                .map(report -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("report", report);
                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", false);
                    response.put("message", "报告不存在");
                    return ResponseEntity.badRequest().body(response);
                });
    }

    /**
     * 获取案件的报告列表
     */
    @GetMapping("/case/{caseId}")
    public ResponseEntity<Map<String, Object>> getReportsByCaseId(@PathVariable Long caseId) {
        List<GeneratedReport> reports = reportGenerateService.getReportsByCaseId(caseId);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("reports", reports);
        return ResponseEntity.ok(response);
    }

    /**
     * 报告列表
     */
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> listReports() {
        // TODO: 实现分页查询
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "TODO: 实现分页查询");
        return ResponseEntity.ok(response);
    }
}
