package com.zxl.rag.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.io.File;

@Configuration
public class DocumentConfig {

    @Value("${app.file.storage-path}")
    private String storagePath;

    @Value("${app.knowledge.doc-path}")
    private String knowledgeDocPath;

    @Value("${app.report.output-path}")
    private String reportOutputPath;

    @PostConstruct
    public void init() {
        // 创建必要的目录
        createDirIfNotExists(storagePath);
        createDirIfNotExists(knowledgeDocPath);
        createDirIfNotExists(reportOutputPath);
    }

    private void createDirIfNotExists(String path) {
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public String getStoragePath() {
        return storagePath;
    }

    public String getKnowledgeDocPath() {
        return knowledgeDocPath;
    }

    public String getReportOutputPath() {
        return reportOutputPath;
    }
}
