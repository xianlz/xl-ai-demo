package com.zxl.rag.service;

import com.zxl.rag.entity.InsuranceCase;
import com.zxl.rag.entity.VectorDocument;
import com.zxl.rag.repository.InsuranceCaseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 报告评分服务
 */
@Service
public class ReportScoreService {

    private static final Logger logger = LoggerFactory.getLogger(ReportScoreService.class);

    private final ApacheTikaParser tikaParser;
    private final VectorService vectorService;
    private final EmbeddingModel embeddingModel;
    private final ChatModel chatModel;
    private final InsuranceCaseRepository caseRepository;

    public ReportScoreService(
            ApacheTikaParser tikaParser,
            VectorService vectorService,
            EmbeddingModel embeddingModel,
            ChatModel chatModel,
            InsuranceCaseRepository caseRepository) {
        this.tikaParser = tikaParser;
        this.vectorService = vectorService;
        this.embeddingModel = embeddingModel;
        this.chatModel = chatModel;
        this.caseRepository = caseRepository;
    }

    /**
     * 评分用户上传的报告
     * @param caseId 案件ID
     * @param userReportContent 用户报告内容
     * @return 评分结果
     */
    public Map<String, Object> scoreReport(Long caseId, String userReportContent) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 1. 获取案件信息
            InsuranceCase insuranceCase = caseRepository.findById(caseId)
                    .orElseThrow(() -> new IllegalArgumentException("案件不存在: " + caseId));

            // 2. 获取知识库中的标准报告
            List<VectorDocument> standardDocs = retrieveRelevantDocs(insuranceCase, userReportContent);
            String standardContext = buildStandardContext(standardDocs);

            // 3. 调用LLM进行评分
            String scoreResult = callLlmScore(insuranceCase, userReportContent, standardContext);

            // 4. 解析评分结果
            result.put("success", true);
            result.put("caseId", caseId);
            result.put("score", extractScore(scoreResult));
            result.put("analysis", scoreResult);
            result.put("standardDocsCount", standardDocs.size());

            logger.info("报告评分完成: caseId={}, score={}", caseId, extractScore(scoreResult));

        } catch (Exception e) {
            logger.error("报告评分失败: {}", e.getMessage());
            result.put("success", false);
            result.put("error", e.getMessage());
        }

        return result;
    }

    private List<VectorDocument> retrieveRelevantDocs(InsuranceCase insuranceCase, String query) {
        try {
            float[] queryVector = embeddingModel.embed(query);
            List<VectorDocument> results = vectorService.hybridSearch(queryVector, query, 5L);

            if (insuranceCase.getInsuranceType() != null) {
                results = results.stream()
                        .filter(doc -> insuranceCase.getInsuranceType().equals(doc.getInsuranceType()))
                        .limit(3)
                        .toList();
            }

            return results;
        } catch (Exception e) {
            logger.error("知识库检索失败: {}", e.getMessage());
            return List.of();
        }
    }

    private String buildStandardContext(List<VectorDocument> docs) {
        if (docs.isEmpty()) {
            return "（知识库中未找到相关标准报告）";
        }

        StringBuilder context = new StringBuilder();
        for (VectorDocument doc : docs) {
            context.append("【标准报告片段】\n")
                    .append(doc.getContent())
                    .append("\n\n");
        }
        return context.toString();
    }

    private String callLlmScore(InsuranceCase insuranceCase, String userReport, String standardContext) {
        String template = """
                作为一位资深保险公估专家，请对用户提交的报告进行专业评分。

                ## 案件信息
                案件编号: {caseNumber}
                险种类型: {insuranceType}

                ## 用户提交的报告
                {userReport}

                ## 知识库中的标准报告参考
                {standardContext}

                ## 评分维度（每个维度1-10分）
                1. 内容完整性 - 报告是否涵盖了必要的要素
                2. 专业性 - 术语使用和评估方法是否专业
                3. 准确性 - 损失评估是否准确合理
                4. 规范性 - 格式和表述是否规范
                5. 实用性 - 报告对理赔决策的帮助程度

                ## 输出格式
                总分: X/50
                内容完整性: X/10
                专业性: X/10
                准确性: X/10
                规范性: X/10
                实用性: X/10
                评语: [简要评价]

                请给出评分：
                """;

        PromptTemplate promptTemplate = new PromptTemplate(template);
        Prompt prompt = promptTemplate.create(Map.of(
                "caseNumber", insuranceCase.getCaseNumber(),
                "insuranceType", insuranceCase.getInsuranceType(),
                "userReport", userReport,
                "standardContext", standardContext
        ));

        try {
            ChatResponse response = chatModel.call(prompt);
            return response.getResult().getOutput().getText();
        } catch (Exception e) {
            logger.error("LLM评分调用失败: {}", e.getMessage());
            throw new RuntimeException("LLM调用失败: " + e.getMessage());
        }
    }

    private int extractScore(String scoreResult) {
        // 简单提取总分
        try {
            String[] lines = scoreResult.split("\n");
            for (String line : lines) {
                if (line.startsWith("总分:")) {
                    String num = line.replaceAll("[^0-9]", "");
                    return Integer.parseInt(num);
                }
            }
            return 0;
        } catch (Exception e) {
            return 0;
        }
    }
}
