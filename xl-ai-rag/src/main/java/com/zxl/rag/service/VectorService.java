package com.zxl.rag.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.zxl.rag.entity.VectorDocument;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 向量操作服务类
 */
@Service
@Slf4j
public class VectorService {
    private final ElasticsearchClient client;
    private static final String INDEX_NAME = "vector-documents";

    public VectorService(ElasticsearchClient client) {
        this.client = client;
    }

    /**
     * 插入向量文档
     */
    public String insertDocument(VectorDocument document) throws Exception {
        IndexResponse response = client.index(i -> i
                .index(INDEX_NAME)
                .id(document.getId())
                .document(document)
        );

        log.info("文档插入成功: " + response.id());
        return response.id();
    }

    /**
     * 批量插入向量文档
     */
    public boolean bulkInsertDocuments(List<VectorDocument> documents) throws Exception {
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
            log.error("批量插入存在错误");
            response.items().forEach(item -> {
                if (item.error() != null) {
                    log.error("错误: " + item.error().reason());
                }
            });
            return false;
        } else {
            log.info("批量插入成功，处理了 " + documents.size() + " 个文档");
            return true ;
        }
    }

    /**
     * 向量搜索
     */
    public List<VectorDocument> vectorSearch(float[] queryVector, Long k1) throws Exception {
        // Convert float[] to List<Float> for ES client
        List<Float> queryVectorList = new ArrayList<>();
        for (float f : queryVector) {
            queryVectorList.add(f);
        }

        SearchResponse<VectorDocument> response = client.search(s -> s
                        .index(INDEX_NAME)
                        .knn(k -> k
                                .field("vector")
                                .queryVector(queryVectorList)
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
    public List<VectorDocument> hybridSearch(float[] queryVector, String queryText, Long k1) throws Exception {
        // Convert float[] to List<Float> for ES client
        List<Float> queryVectorList = new ArrayList<>();
        for (float f : queryVector) {
            queryVectorList.add(f);
        }

        SearchResponse<VectorDocument> response = client.search(s -> s
                        .index(INDEX_NAME)
                        .knn(k -> k
                                .field("vector")
                                .queryVector(queryVectorList)
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
                                .includes("id", "title", "content", "insuranceType", "subType", "docUuid", "chunkIndex")
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
            if (doc != null) {
                doc.setId(response.id());
            }
            return doc;
        }
        return null;
    }

    /**
     * 根据docUuid删除所有关联的向量文档
     */
    public void deleteByDocUuid(String docUuid) throws Exception {
        // 先查询所有匹配的文档
        SearchResponse<VectorDocument> response = client.search(s -> s
                        .index(INDEX_NAME)
                        .query(q -> q
                                .term(t -> t
                                        .field("docUuid")
                                        .value(docUuid)
                                )
                        )
                        .source(src -> src.filter(f -> f.includes("id")))
                        .size(1000),
                VectorDocument.class
        );

        if (response.hits().hits().isEmpty()) {
            log.info("未找到要删除的文档: {}", docUuid);
            return;
        }

        // 批量删除
        BulkRequest.Builder br = new BulkRequest.Builder();
        for (Hit<VectorDocument> hit : response.hits().hits()) {
            br.operations(op -> op
                    .delete(d -> d
                            .index(INDEX_NAME)
                            .id(hit.id())
                    )
            );
        }

        BulkResponse bulkResponse = client.bulk(br.build());
        if (bulkResponse.errors()) {
            log.error("批量删除存在错误");
        } else {
            log.info("批量删除成功，删除了 {} 个文档", response.hits().hits().size());
        }
    }

}
