package com.zxl.rag.service;

import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;

/**
 * Apache Tika 文档解析服务
 * 支持解析 doc, docx, pdf 等格式，自动检测MIME类型
 * 提取纯文本内容，忽略图片
 */
@Service
public class ApacheTikaParser {

    private static final Logger logger = LoggerFactory.getLogger(ApacheTikaParser.class);

    private final Tika tika;
    private final AutoDetectParser parser;

    public ApacheTikaParser() {
        this.tika = new Tika();
        this.parser = new AutoDetectParser();
    }

    /**
     * 解析文档并提取纯文本
     * @param inputStream 文档输入流
     * @return 提取的纯文本内容
     */
    public String parse(InputStream inputStream) {
        return parse(inputStream, -1);
    }

    /**
     * 解析文档并提取纯文本
     * @param inputStream 文档输入流
     * @param maxStringLength 最大文本长度，-1表示不限制
     * @return 提取的纯文本内容
     */
    public String parse(InputStream inputStream, int maxStringLength) {
        try {
            // 使用BodyContentHandler自动忽略图片等二进制内容
            BodyContentHandler handler = new BodyContentHandler(maxStringLength);
            Metadata metadata = new Metadata();
            ParseContext context = new ParseContext();

            parser.parse(inputStream, handler, metadata, context);

            String content = handler.toString();
            logger.info("文档解析成功，内容长度: {} 字符", content.length());
            return content;

        } catch (IOException | SAXException | TikaException e) {
            logger.error("文档解析失败: {}", e.getMessage(), e);
            throw new RuntimeException("文档解析失败: " + e.getMessage(), e);
        }
    }

    /**
     * 检测文档MIME类型
     * @param inputStream 文档输入流
     * @return MIME类型字符串
     */
    public String detectContentType(InputStream inputStream) {
        try {
            return tika.detect(inputStream);
        } catch (IOException e) {
            logger.error("MIME类型检测失败: {}", e.getMessage());
            return "application/octet-stream";
        }
    }

    /**
     * 提取文档元数据
     * @param inputStream 文档输入流
     * @return 元数据对象
     */
    public Metadata extractMetadata(InputStream inputStream) {
        Metadata metadata = new Metadata();
        try {
            BodyContentHandler handler = new BodyContentHandler();
            ParseContext context = new ParseContext();
            parser.parse(inputStream, handler, metadata, context);
        } catch (Exception e) {
            logger.error("元数据提取失败: {}", e.getMessage());
        }
        return metadata;
    }

    /**
     * 检查文件类型是否支持
     * @param fileName 文件名
     * @return 是否支持
     */
    public boolean isSupported(String fileName) {
        String lowerName = fileName.toLowerCase();
        return lowerName.endsWith(".doc") ||
               lowerName.endsWith(".docx") ||
               lowerName.endsWith(".pdf") ||
               lowerName.endsWith(".txt") ||
               lowerName.endsWith(".rtf");
    }
}
