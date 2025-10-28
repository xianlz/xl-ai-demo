package com.zxl.rag.util;



import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.xmlbeans.XmlException;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
public class DocumentParser {

    /**
     *  根据文件后缀名选择解析逻辑
     * @param inputStream
     * @param fileName
     * @return
     * @throws Exception
     */
    public List<Document> parse(InputStream inputStream, String fileName) throws Exception {
        if (fileName.endsWith(".txt")) {
            return parseTxt(inputStream,fileName);
        } else if (fileName.endsWith(".pdf")) {
            return parsePdf(inputStream,fileName);
        } else if (fileName.endsWith(".docx")) {
            return parseDocx(inputStream,fileName);
        } else {
            throw new UnsupportedOperationException("不支持的文件格式：" + fileName);
        }
    }

    /**
     * 解析TXT
     * @param inputStream
     * @return
     * @throws IOException
     */
    private List<Document> parseTxt(InputStream inputStream, String fileName) throws IOException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    lines.add(line);
                }
            }
        }
        // 封装为 Spring AI Document（content 为全文，metadata 可添加文件名等信息）
        Document doc = new Document(String.join("\n", lines));
        doc.getMetadata().put("fileName", fileName); // 添加元数据
        return List.of(doc);
    }

    /**
     * 解析PDF（依赖 PDFBox 库）
     * @param inputStream
     * @return
     * @throws IOException
     */
    private List<Document> parsePdf(InputStream inputStream, String fileName) throws Exception {
        try (PDDocument document = PDDocument.load(inputStream)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String content = stripper.getText(document);
            Document doc = new Document(content);
            doc.getMetadata().put("fileName", fileName);
            return List.of(doc);
        }
    }

    /**
     * 解析DOCX（依赖 POI 库）
     * @param inputStream
     * @return
     * @throws IOException
     * @throws XmlException
     */
    private List<Document> parseDocx(InputStream inputStream, String fileName) throws Exception {

        try (XWPFDocument document = new XWPFDocument(inputStream)) {
            XWPFWordExtractor extractor = new XWPFWordExtractor(document);
            String content = extractor.getText();
            Document doc = new Document(content);
            doc.getMetadata().put("fileName", fileName);
            return List.of(doc);
        }

    }
}