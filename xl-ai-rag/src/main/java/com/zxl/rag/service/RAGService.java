package com.zxl.rag.service;


import com.zxl.rag.entity.VectorDocument;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class RAGService {

    private final VectorService vectorService;

    /**
     *  文本嵌入模型
     */
    private final EmbeddingModel embeddingModel;

    /**
     * 大模型客户端
     */
    private final ChatModel chatModel;

    /**
     * RAG核心方法：基于知识库回答用户问题
     * @param userQuestion 用户问题
     * @param topK 检索最相关的前K个文档
     * @return 结合知识库的回答
     */
    public String answerWithKnowledge(String userQuestion, Long topK) {
        try {
            // 步骤1：将用户问题转换为向量
            float[] queryVector = embeddingModel.embed(userQuestion);
            ArrayList queryVectorList = new ArrayList<>(List.of(queryVector));

            // 步骤2：调用VectorService检索相关文档
             List<VectorDocument> relevantDocs = vectorService.hybridSearch(queryVectorList, userQuestion, topK);

            // 步骤3：将检索到的文档拼接为上下文
            String context = buildContext(relevantDocs);

            // 步骤4：构建提示词（Prompt）
            String promptStr = buildPrompt(userQuestion, context);
            Prompt prompt = new Prompt(promptStr);
            List<Generation> results = chatModel.call(prompt).getResults();
            // 步骤5：调用大模型生成回答
            return results.stream().map(x -> x.getOutput().getContent()).collect(Collectors.joining());
        } catch (Exception e) {
            log.error("RAG回答生成失败", e);
            return "抱歉，处理您的问题时出错了，请稍后再试。";
        }
    }

    /**
     * 拼接检索到的文档为上下文文本
     */
    private String buildContext(List<VectorDocument> docs) {
        if (docs.isEmpty()) {
            return "没有找到相关信息。";
        }
        StringBuilder contextBuilder = new StringBuilder();
        contextBuilder.append("以下是相关参考信息：\n");
        for (int i = 0; i < docs.size(); i++) {
            VectorDocument doc = docs.get(i);
            contextBuilder.append(String.format("【参考%d】标题：%s\n内容：%s\n\n",
                    i + 1, doc.getTitle(), doc.getContent()));
        }
        return contextBuilder.toString();
    }

    /**
     * 构建大模型系统提示词（Prompt Engineering）
     */
    private String buildPrompt(String userQuestion, String context) {
        return String.format("请基于以下参考信息，回答用户的问题。如果参考信息中没有相关内容，直接说明“没有找到相关信息”，不要编造答案。\n" +
                        "参考信息：\n%s\n" +
                        "用户问题：%s\n" +
                        "回答：",
                context, userQuestion);
    }

}
