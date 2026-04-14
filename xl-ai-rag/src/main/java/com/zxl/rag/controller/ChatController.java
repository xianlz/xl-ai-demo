package com.zxl.rag.controller;

import com.zxl.rag.service.RAGService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/knowledge")
@AllArgsConstructor
public class ChatController {

    private final RAGService ragService;

    /**
     * RAG 问答接口
     * @param request 请求体，包含 question、userId、topK
     * @return 回答结果
     */
    @PostMapping("/chat")
    public ResponseEntity<Map<String, Object>> chat(@RequestBody Map<String, Object> request) {
        String question = (String) request.get("question");
        String userId = (String) request.get("userId");
        Long topK = request.get("topK") != null
            ? Long.valueOf(request.get("topK").toString())
            : 5L;

        if (question == null || question.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("msg", "问题不能为空"));
        }

        // 调用 RAG 服务获取回答
        String answer = ragService.answerWithKnowledge(question, topK);

        Map<String, Object> result = new HashMap<>();
        result.put("answer", answer);
        result.put("question", question);

        return ResponseEntity.ok(result);
    }
}
