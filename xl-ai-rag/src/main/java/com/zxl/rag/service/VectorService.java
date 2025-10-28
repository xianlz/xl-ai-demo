package com.zxl.rag.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.zxl.rag.entity.VectorDocument;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zxl
 * 向量操作服务类
 */
@Service
@AllArgsConstructor
public class VectorService {
    private final ElasticsearchClient client;
    private static final String INDEX_NAME = "vector-documents";


    /**
     * 插入向量文档
     */
    public String insertDocument(VectorDocument document) throws Exception {
        IndexResponse response = client.index(i -> i
                .index(INDEX_NAME)
                .id(document.getId())
                .document(document)
        );

        System.out.println("文档插入成功: " + response.id());
        return response.id();
    }

    /**
     * 批量插入向量文档
     */
    public void bulkInsertDocuments(List<VectorDocument> documents) throws Exception {
        BulkRequest.Builder br = new BulkRequest.Builder();

        for (VectorDocument doc : documents) {
            br.operations(op -> op
                    .index(idx -> idx
                            .index(INDEX_NAME)
                            .id(doc.getId())
                            .document(doc)
                    )
            );
        }

        BulkResponse response = client.bulk(br.build());

        if (response.errors()) {
            System.out.println("批量插入存在错误");
            response.items().forEach(item -> {
                if (item.error() != null) {
                    System.out.println("错误: " + item.error().reason());
                }
            });
        } else {
            System.out.println("批量插入成功，处理了 " + documents.size() + " 个文档");
        }
    }

    /**
     * 向量搜索
     */
    public List<VectorDocument> vectorSearch(List<Float> queryVector, Long k1) throws Exception {
        SearchResponse<VectorDocument> response = client.search(s -> s
                        .index(INDEX_NAME)
                        .knn(k -> k
                                .field("vector")
                                .queryVector(queryVector)
                                .k(k1)
                                .numCandidates(100L)
                        )
                        .source(src -> src.filter(f -> f
                                .includes("id", "title", "content")
                        )),
                VectorDocument.class
        );

        List<VectorDocument> results = new ArrayList<>();
        for (Hit<VectorDocument> hit : response.hits().hits()) {
            VectorDocument doc = hit.source();
            if (doc != null) {
                doc.setId(hit.id());
                results.add(doc);
            }
        }

        return results;
    }

    /**
     * 混合搜索（向量 + 文本）
     */
    public List<VectorDocument> hybridSearch(List<Float> queryVector, String queryText, Long k1) throws Exception {
        SearchResponse<VectorDocument> response = client.search(s -> s
                        .index(INDEX_NAME)
                        .knn(k -> k
                                .field("vector")
                                .queryVector(queryVector)
                                .k(k1)
                                .numCandidates(100L)
                        )
                        .query(q -> q
                                .multiMatch(m -> m
                                        .fields("title", "content")
                                        .query(queryText)
                                )
                        )
                        .source(src -> src.filter(f -> f
                                .includes("id", "title", "content")
                        )),
                VectorDocument.class
        );

        List<VectorDocument> results = new ArrayList<>();
        for (Hit<VectorDocument> hit : response.hits().hits()) {
            VectorDocument doc = hit.source();
            if (doc != null) {
                doc.setId(hit.id());
                results.add(doc);
            }
        }

        return results;
    }

    /**
     * 根据ID获取文档
     */
    public VectorDocument getDocumentById(String id) throws Exception {
        GetResponse<VectorDocument> response = client.get(g -> g
                        .index(INDEX_NAME)
                        .id(id),
                VectorDocument.class
        );

        if (response.found()) {
            VectorDocument doc = response.source();
            doc.setId(response.id());
            return doc;
        }
        return null;
    }

}
