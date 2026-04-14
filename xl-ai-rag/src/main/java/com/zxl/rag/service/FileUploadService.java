package com.zxl.rag.service;


import com.zxl.rag.entity.VectorDocument;
import com.zxl.rag.util.DocumentParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FileUploadService {

    private final VectorService vectorService;
    private final EmbeddingModel embeddingModel;
    private final DocumentParser documentParser;

    public FileUploadService(VectorService vectorService, EmbeddingModel embeddingModel, DocumentParser documentParser) {
        this.vectorService = vectorService;
        this.embeddingModel = embeddingModel;
        this.documentParser = documentParser;
    }

    /**
     * 处理用户上传的文件，生成向量并插入ES
     * @param file 用户上传的文件（MultipartFile）
     * @param userId 上传用户ID（用于多用户隔离）
     * @return 插入成功的文档数量
     */
    public int handleUploadedFile(MultipartFile file, String userId) {
        try {
            // 步骤1：解析文件（根据后缀名选择解析器，如PDF用PDFBox，DOCX用POI）
            String fileName = file.getOriginalFilename();
            List<Document> fileContents = documentParser.parse(file.getInputStream(), fileName);

            // 步骤2：文本分割（避免单段文本过长，影响向量质量和检索精度）1200token/段，重叠350
            TokenTextSplitter splitter =  new TokenTextSplitter(1200,
                    350, 5,
                    100, true);
            List<Document> splitDocs = splitter.split(fileContents);

            // 步骤3：生成向量文档列表（给每个分割后的文本生成向量）
            List<VectorDocument> vectorDocuments = splitDocs.stream()
                    .map(text -> {
                        VectorDocument doc = new VectorDocument();
                        doc.setId(UUID.randomUUID().toString());
                        doc.setTitle(fileName);
                        doc.setContent(text.getText());
                        // 步骤4：生成向量（核心！必须在插入前赋值）
                        float[] embed = embeddingModel.embed(text.getText());
                        log.info("嵌入模型生成的向量维度：{}", embed.length);
                        doc.setVector(embed);
                        return doc;
                    })
                    .collect(Collectors.toList());

            // 步骤5：批量插入向量文档（调用你现有的 bulkInsertDocuments 方法）
            boolean isSuccess = vectorService.bulkInsertDocuments(vectorDocuments);
            if (isSuccess){
                log.info("用户 {} 上传文件 {} 处理完成，插入 {} 个向量文档", userId, fileName, vectorDocuments.size());
            }
            return vectorDocuments.size();

        } catch (Exception e) {
            log.error("处理用户 {} 上传文件失败", userId, e);
            throw new RuntimeException("文件导入知识库失败", e);
        }
    }
}