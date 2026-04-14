package com.zxl.rag.service;

import com.zxl.rag.config.DocumentConfig;
import com.zxl.rag.entity.KnowledgeDoc;
import com.zxl.rag.entity.VectorDocument;
import com.zxl.rag.repository.KnowledgeDocRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 文档服务 - 处理知识库文档的上传、解析、分割和入库
 */
@Service
public class DocumentService {

    private static final Logger logger = LoggerFactory.getLogger(DocumentService.class);

    private final ApacheTikaParser tikaParser;
    private final VectorService vectorService;
    private final KnowledgeDocRepository knowledgeDocRepository;
    private final EmbeddingModel embeddingModel;
    private final DocumentConfig documentConfig;

    public DocumentService(
            ApacheTikaParser tikaParser,
            VectorService vectorService,
            KnowledgeDocRepository knowledgeDocRepository,
            EmbeddingModel embeddingModel,
            DocumentConfig documentConfig) {
        this.tikaParser = tikaParser;
        this.vectorService = vectorService;
        this.knowledgeDocRepository = knowledgeDocRepository;
        this.embeddingModel = embeddingModel;
        this.documentConfig = documentConfig;
    }

    /**
     * 上传并处理文档到知识库
     * @param file 上传的文件
     * @param insuranceType 险种类型
     * @param subType 子类型
     * @param uploadedBy 上传者
     * @return 处理结果
     */
    public Map<String, Object> uploadDocument(
            MultipartFile file,
            String insuranceType,
            String subType,
            String uploadedBy) {

        Map<String, Object> result = new HashMap<>();

        try {
            // 1. 验证文件
            if (!tikaParser.isSupported(file.getOriginalFilename())) {
                throw new IllegalArgumentException("不支持的文件类型: " + file.getOriginalFilename());
            }

            String docUuid = UUID.randomUUID().toString();
            String originalFileName = file.getOriginalFilename();
            String fileExtension = getFileExtension(originalFileName);
            String storedFileName = docUuid + fileExtension;
            String filePath = documentConfig.getKnowledgeDocPath() + File.separator + storedFileName;

            // 2. 保存原始文件
            Path path = Paths.get(filePath);
            Files.createDirectories(path.getParent());
            file.transferTo(path.toFile());

            // 3. 解析文档内容
            String content;
            try (InputStream is = Files.newInputStream(path)) {
                content = tikaParser.parse(is);
            }

            if (content == null || content.trim().isEmpty()) {
                throw new RuntimeException("文档内容为空");
            }

            // 4. 分割文本
            String[] chunkArray = splitText(content, 1200, 350);
            List<String> chunks = Arrays.asList(chunkArray);
            logger.info("文档分割完成，共 {} 个chunks", chunks.size());

            // 5. 生成向量并存储到ES
            List<VectorDocument> vectorDocuments = new ArrayList<>();
            for (int i = 0; i < chunks.size(); i++) {
                String chunk = chunks.get(i);
                float[] embedding = embeddingModel.embed(chunk);

                VectorDocument vd = new VectorDocument();
                vd.setId(docUuid + "_" + i);
                vd.setTitle(originalFileName + " [chunk " + (i + 1) + "]");
                vd.setContent(chunk);
                vd.setInsuranceType(insuranceType);
                vd.setSubType(subType);
                vd.setDocUuid(docUuid);
                vd.setChunkIndex(i);
                vd.setVector(embedding);
                vectorDocuments.add(vd);
            }

            vectorService.bulkInsertDocuments(vectorDocuments);

            // 6. 保存文档元数据
            KnowledgeDoc knowledgeDoc = new KnowledgeDoc();
            knowledgeDoc.setDocUuid(docUuid);
            knowledgeDoc.setFileName(originalFileName);
            knowledgeDoc.setFilePath(filePath);
            knowledgeDoc.setFileType(fileExtension);
            knowledgeDoc.setFileSize(file.getSize());
            knowledgeDoc.setInsuranceType(insuranceType);
            knowledgeDoc.setSubType(subType);
            knowledgeDoc.setTitle(originalFileName);
            knowledgeDoc.setChunksCount(chunks.size());
            knowledgeDoc.setUploadStatus("COMPLETED");
            knowledgeDoc.setUploadedBy(uploadedBy);
            knowledgeDoc.setUploadTime(LocalDateTime.now());
            knowledgeDocRepository.save(knowledgeDoc);

            result.put("success", true);
            result.put("docUuid", docUuid);
            result.put("fileName", originalFileName);
            result.put("chunksCount", chunks.size());
            result.put("contentLength", content.length());

            logger.info("文档上传成功: {}, chunks: {}", originalFileName, chunks.size());

        } catch (Exception e) {
            logger.error("文档上传失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }

        return result;
    }

    /**
     * 删除知识库文档
     * @param docUuid 文档UUID
     */
    public void deleteDocument(String docUuid) {
        // 1. 获取文档信息
        KnowledgeDoc doc = knowledgeDocRepository.findByDocUuid(docUuid)
                .orElseThrow(() -> new IllegalArgumentException("文档不存在: " + docUuid));

        try {
            // 2. 删除ES中的向量
            vectorService.deleteByDocUuid(docUuid);
        } catch (Exception e) {
            logger.error("删除ES向量失败: {}", e.getMessage());
        }

        // 3. 删除原始文件
        File file = new File(doc.getFilePath());
        if (file.exists()) {
            file.delete();
        }

        // 4. 删除元数据
        knowledgeDocRepository.delete(doc);

        logger.info("文档删除成功: {}", docUuid);
    }

    /**
     * 获取知识库文档列表
     * @param insuranceType 险种类型（可选）
     * @return 文档列表
     */
    public List<KnowledgeDoc> getDocumentList(String insuranceType) {
        if (insuranceType != null && !insuranceType.isEmpty()) {
            return knowledgeDocRepository.findByInsuranceType(insuranceType);
        }
        return knowledgeDocRepository.findAll();
    }

    /**
     * 分割文本为chunks
     */
    private String[] splitText(String text, int maxTokens, int overlapTokens) {
        // 简单按段落分割，实际生产中可使用更复杂的语义分割
        List<String> chunks = new ArrayList<>();
        String[] paragraphs = text.split("\n\n+");

        StringBuilder currentChunk = new StringBuilder();
        int currentTokens = 0;

        for (String paragraph : paragraphs) {
            int paragraphTokens = estimateTokens(paragraph);

            if (currentTokens + paragraphTokens > maxTokens && currentChunk.length() > 0) {
                chunks.add(currentChunk.toString().trim());
                // 保留最后overlapTokens长度的内容作为重叠
                String overlap = getLastCharacters(currentChunk.toString(), overlapTokens);
                currentChunk = new StringBuilder(overlap);
                currentTokens = estimateTokens(overlap);
            }

            currentChunk.append(paragraph).append("\n\n");
            currentTokens += paragraphTokens;
        }

        if (currentChunk.length() > 0) {
            chunks.add(currentChunk.toString().trim());
        }

        return chunks.toArray(new String[0]);
    }

    private int estimateTokens(String text) {
        // 粗略估算：中文按字符数，英文按单词数
        return text.length() / 2;
    }

    private String getLastCharacters(String text, int count) {
        if (text.length() <= count) {
            return text;
        }
        return text.substring(text.length() - count);
    }

    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot > 0) {
            return fileName.substring(lastDot);
        }
        return "";
    }
}
