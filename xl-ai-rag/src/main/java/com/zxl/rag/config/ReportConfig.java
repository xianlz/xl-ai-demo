package com.zxl.rag.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ReportConfig {

    @Value("${app.report.template.default}")
    private String defaultTemplate;

    @Value("${app.report.output-path}")
    private String outputPath;

    public String getDefaultTemplate() {
        return defaultTemplate;
    }

    public String getOutputPath() {
        return outputPath;
    }
}
