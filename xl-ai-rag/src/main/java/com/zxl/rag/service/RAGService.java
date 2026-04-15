package com.zxl.rag.service;

import com.zxl.rag.entity.VectorDocument;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RAGService {

    private final VectorService vectorService;
    private final EmbeddingModel embeddingModel;
    private final ChatModel chatModel;

    public RAGService(VectorService vectorService, EmbeddingModel embeddingModel, ChatModel chatModel) {
        this.vectorService = vectorService;
        this.embeddingModel = embeddingModel;
        this.chatModel = chatModel;
    }

    /**
     * RAG core method: answer user questions based on knowledge base
     */
    public String answerWithKnowledge(String userQuestion, Long topK) {
        try {
            // Step 1: Convert user question to vector
            float[] queryVector = embeddingModel.embed(userQuestion);

            // Step 2: Search relevant documents via VectorService
            List<VectorDocument> relevantDocs = vectorService.hybridSearch(queryVector, userQuestion, topK);

            // Step 3: Build context from retrieved documents
            String context = buildContext(relevantDocs);

            // Step 4: Build prompt
            String promptStr = buildPrompt(userQuestion, context);
            Prompt prompt = new Prompt(promptStr);
            List<Generation> results = chatModel.call(prompt).getResults();

            // Step 5: Generate answer via LLM
            return results.stream().map(x -> x.getOutput().getContent()).collect(Collectors.joining());
        } catch (Exception e) {
            log.error("RAG answer generation failed", e);
            return "Sorry, an error occurred while processing your question. Please try again later.";
        }
    }

    private String buildContext(List<VectorDocument> docs) {
        if (docs.isEmpty()) {
            return "No relevant information found.";
        }
        StringBuilder contextBuilder = new StringBuilder();
        contextBuilder.append("Here is the relevant reference information:\n");
        for (int i = 0; i < docs.size(); i++) {
            VectorDocument doc = docs.get(i);
            contextBuilder.append(String.format("[Reference %d] Title: %s\nContent: %s\n\n",
                    i + 1, doc.getTitle(), doc.getContent()));
        }
        return contextBuilder.toString();
    }

    private String buildPrompt(String userQuestion, String context) {
        return String.format("Please answer the user's question based on the following reference information. " +
                        "If there is no relevant content in the reference, say 'No relevant information found.' " +
                        "Do not make up answers.\n" +
                        "Reference:\n%s\n" +
                        "User question: %s\n" +
                        "Answer:",
                context, userQuestion);
    }
}
