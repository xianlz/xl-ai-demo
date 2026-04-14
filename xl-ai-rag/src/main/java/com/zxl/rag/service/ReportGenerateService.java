package com.zxl.rag.service;

import com.zxl.rag.config.DocumentConfig;
import com.zxl.rag.config.ReportConfig;
import com.zxl.rag.entity.*;
import com.zxl.rag.repository.GeneratedReportRepository;
import com.zxl.rag.repository.InsuranceCaseRepository;
import com.zxl.rag.repository.ReportTemplateRepository;
import org.apache.poi.xwpf.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 报告生成服务 - 核心业务逻辑
 */
@Service
public class ReportGenerateService {

    private static final Logger logger = LoggerFactory.getLogger(ReportGenerateService.class);

    private final CaseInfoService caseInfoService;
    private final VectorService vectorService;
    private final EmbeddingModel embeddingModel;
    private final ChatModel chatModel;
    private final GeneratedReportRepository reportRepository;
    private final ReportTemplateRepository templateRepository;
    private final InsuranceCaseRepository caseRepository;
    private final DocumentConfig documentConfig;
    private final ReportConfig reportConfig;

    public ReportGenerateService(
            CaseInfoService caseInfoService,
            VectorService vectorService,
            EmbeddingModel embeddingModel,
            ChatModel chatModel,
            GeneratedReportRepository reportRepository,
            ReportTemplateRepository templateRepository,
            InsuranceCaseRepository caseRepository,
            DocumentConfig documentConfig,
            ReportConfig reportConfig) {
        this.caseInfoService = caseInfoService;
        this.vectorService = vectorService;
        this.embeddingModel = embeddingModel;
        this.chatModel = chatModel;
        this.reportRepository = reportRepository;
        this.templateRepository = templateRepository;
        this.caseRepository = caseRepository;
        this.documentConfig = documentConfig;
        this.reportConfig = reportConfig;
    }

    /**
     * 生成报告
     * @param caseId 案件ID
     * @param templateId 模板ID（可选）
     * @return 生成结果
     */
    public Map<String, Object> generateReport(Long caseId, String templateId) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 1. 获取案件信息
            InsuranceCase insuranceCase = caseRepository.findById(caseId)
                    .orElseThrow(() -> new IllegalArgumentException("案件不存在: " + caseId));

            String caseSummary = caseInfoService.getCaseSummary(caseId);
            logger.info("获取案件摘要成功，长度: {}", caseSummary.length());

            // 2. 检索相关知识库内容
            List<VectorDocument> relevantDocs = retrieveRelevantDocs(insuranceCase, caseSummary);
            String knowledgeContext = buildKnowledgeContext(relevantDocs);
            logger.info("检索到 {} 条相关知识库内容", relevantDocs.size());

            // 3. 获取模板
            ReportTemplate template = getTemplate(templateId, insuranceCase.getInsuranceType());
            String sectionRequirements = buildSectionRequirements(template);

            // 4. 构建Prompt
            String prompt = buildPrompt(caseSummary, knowledgeContext, sectionRequirements);

            // 5. 调用LLM生成报告
            String reportContent = callLlm(prompt);
            logger.info("LLM生成报告成功，长度: {}", reportContent.length());

            // 6. 生成docx文件
            String reportNumber = generateReportNumber();
            String filePath = generateDocx(reportNumber, insuranceCase, reportContent, template);

            // 7. 保存报告记录
            GeneratedReport report = new GeneratedReport();
            report.setReportNumber(reportNumber);
            report.setInsuranceCase(insuranceCase);
            report.setTemplateId(template.getTemplateId());
            report.setReportTitle(insuranceCase.getCaseNumber() + " 公估报告");
            report.setFilePath(filePath);
            report.setStatus("GENERATED");
            report.setGeneratedBy("SYSTEM");
            report.setGenerateTime(LocalDateTime.now());
            report.setContentSummary(reportContent.substring(0, Math.min(500, reportContent.length())));
            reportRepository.save(report);

            result.put("success", true);
            result.put("reportId", report.getId());
            result.put("reportNumber", reportNumber);
            result.put("filePath", filePath);

            logger.info("报告生成成功: {}", reportNumber);

        } catch (Exception e) {
            logger.error("报告生成失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }

        return result;
    }

    /**
     * 检索相关知识库内容
     */
    private List<VectorDocument> retrieveRelevantDocs(InsuranceCase insuranceCase, String caseSummary) {
        try {
            // 生成查询向量
            float[] queryVector = embeddingModel.embed(caseSummary);

            // 混合搜索
            List<VectorDocument> results = vectorService.hybridSearch(
                    queryVector,
                    caseSummary,
                    10L
            );

            // 按险种类型过滤
            if (insuranceCase.getInsuranceType() != null) {
                results = results.stream()
                        .filter(doc -> insuranceCase.getInsuranceType().equals(doc.getInsuranceType()) ||
                                       insuranceCase.getInsuranceType().contains(doc.getInsuranceType()))
                        .limit(5)
                        .collect(Collectors.toList());
            }

            return results;

        } catch (Exception e) {
            logger.error("知识库检索失败: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * 构建知识库上下文
     */
    private String buildKnowledgeContext(List<VectorDocument> docs) {
        if (docs.isEmpty()) {
            return "（知识库中未找到相关内容）";
        }

        StringBuilder context = new StringBuilder();
        for (int i = 0; i < docs.size(); i++) {
            VectorDocument doc = docs.get(i);
            context.append("【参考").append(i + 1).append("】\n");
            context.append("标题: ").append(doc.getTitle()).append("\n");
            context.append("内容: ").append(doc.getContent()).append("\n\n");
        }
        return context.toString();
    }

    /**
     * 获取模板
     */
    private ReportTemplate getTemplate(String templateId, String insuranceType) {
        if (templateId != null && !templateId.isEmpty()) {
            return templateRepository.findById(templateId)
                    .orElseThrow(() -> new IllegalArgumentException("模板不存在: " + templateId));
        }

        // 根据险种类型查找模板
        Optional<ReportTemplate> byType = templateRepository.findByTemplateType(insuranceType);
        if (byType.isPresent()) {
            return byType.get();
        }

        // 返回默认模板
        return templateRepository.findByIsDefaultTrue()
                .orElseGet(this::getDefaultTemplate);
    }

    /**
     * 获取默认模板
     */
    private ReportTemplate getDefaultTemplate() {
        ReportTemplate template = new ReportTemplate();
        template.setTemplateId("default");
        template.setName("默认模板");
        template.setTemplateType("GENERAL");
        template.setActive(true);
        template.setIsDefault(true);

        List<ReportSection> sections = Arrays.asList(
                createSection("一、基本信息", "根据案件信息填写基本信息", 1),
                createSection("二、案件经过", "描述案件发生的经过", 2),
                createSection("三、损失评估", "评估损失情况", 3),
                createSection("四、理算意见", "提供理赔建议", 4),
                createSection("五、附注说明", "其他需要说明的事项", 5)
        );
        template.setSections(sections);

        return template;
    }

    private ReportSection createSection(String name, String prompt, int order) {
        ReportSection section = new ReportSection();
        section.setSectionName(name);
        section.setSectionPrompt(prompt);
        section.setOrder(order);
        section.setRequired(true);
        return section;
    }

    /**
     * 构建章节要求
     */
    private String buildSectionRequirements(ReportTemplate template) {
        StringBuilder requirements = new StringBuilder();
        if (template.getSections() != null) {
            for (ReportSection section : template.getSections()) {
                requirements.append(section.getSectionName())
                        .append(": ")
                        .append(section.getSectionPrompt())
                        .append("\n");
            }
        }
        return requirements.toString();
    }

    /**
     * 构建Prompt
     */
    private String buildPrompt(String caseSummary, String knowledgeContext, String sectionRequirements) {
        String templateStr = """
                你是一位资深保险公估师，请根据以下案件信息和知识库内容，生成一份专业的保险公估报告。

                ## 案件信息
                {caseInfo}

                ## 知识库参考内容
                {knowledgeContext}

                ## 报告章节要求
                {sectionRequirements}

                ## 要求
                1. 报告语言：专业、客观、严谨
                2. 报告格式：严格按照上述章节结构输出
                3. 内容来源：主要依据案件信息，参考知识库内容进行专业分析
                4. 结论明确：损失评估和理赔意见需清晰明确

                请生成完整的报告内容：
                """;

        PromptTemplate promptTemplate = new PromptTemplate(templateStr);
        Prompt prompt = promptTemplate.create(Map.of(
                "caseInfo", caseSummary,
                "knowledgeContext", knowledgeContext,
                "sectionRequirements", sectionRequirements
        ));

        return prompt.getContents();
    }

    /**
     * 调用LLM生成报告
     */
    private String callLlm(String prompt) {
        try {
            ChatResponse response = chatModel.call(new Prompt(prompt));
            return response.getResult().getOutput().getText();
        } catch (Exception e) {
            logger.error("LLM调用失败: {}", e.getMessage());
            throw new RuntimeException("LLM调用失败: " + e.getMessage());
        }
    }

    /**
     * 生成docx文件
     */
    private String generateDocx(String reportNumber, InsuranceCase insuranceCase,
                                String reportContent, ReportTemplate template) throws IOException {

        String outputPath = documentConfig.getReportOutputPath();
        Files.createDirectories(Paths.get(outputPath));

        String fileName = reportNumber + ".docx";
        String filePath = outputPath + File.separator + fileName;

        try (XWPFDocument document = new XWPFDocument()) {
            // 添加标题
            XWPFParagraph title = document.createParagraph();
            title.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun titleRun = title.createRun();
            titleRun.setText(insuranceCase.getCaseNumber() + " 保险公估报告");
            titleRun.setBold(true);
            titleRun.setFontSize(18);

            // 添加分隔线
            XWPFParagraph divider = document.createParagraph();
            divider.setBorderBottom(Borders.SINGLE);
            divider.setSpacingAfter(400);

            // 添加报告内容（按章节分割）
            String[] sections = reportContent.split("\n(?=[一二三四五六七八九十])");

            for (String sectionContent : sections) {
                if (sectionContent.trim().isEmpty()) {
                    continue;
                }

                XWPFParagraph paragraph = document.createParagraph();
                paragraph.setSpacingBefore(200);
                paragraph.setSpacingAfter(200);

                XWPFRun run = paragraph.createRun();
                run.setText(sectionContent.trim());
                run.setFontSize(12);
            }

            // 添加报告信息
            XWPFParagraph info = document.createParagraph();
            info.setSpacingBefore(600);
            info.setAlignment(ParagraphAlignment.RIGHT);

            XWPFRun infoRun = info.createRun();
            infoRun.setText("\n报告生成时间: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            infoRun.setFontSize(10);

            // 保存文件
            try (FileOutputStream out = new FileOutputStream(filePath)) {
                document.write(out);
            }
        }

        logger.info("DOCX文件生成成功: {}", filePath);
        return filePath;
    }

    /**
     * 生成报告编号
     */
    private String generateReportNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.format("%04d", new Random().nextInt(10000));
        return "RPT-" + timestamp + "-" + random;
    }

    /**
     * 获取报告
     */
    public Optional<GeneratedReport> getReport(Long reportId) {
        return reportRepository.findById(reportId);
    }

    /**
     * 获取报告文件路径
     */
    public Path getReportFilePath(Long reportId) {
        GeneratedReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("报告不存在: " + reportId));
        return Paths.get(report.getFilePath());
    }

    /**
     * 获取案件生成的报告列表
     */
    public List<GeneratedReport> getReportsByCaseId(Long caseId) {
        return reportRepository.findByInsuranceCaseId(caseId);
    }
}
