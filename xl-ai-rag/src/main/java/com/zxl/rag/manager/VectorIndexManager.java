package com.zxl.rag.manager;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import co.elastic.clients.elasticsearch.indices.DeleteIndexResponse;
import com.zxl.rag.entity.VectorDocument;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 向量索引管理类
 * @author zxl
 */
@Component
@AllArgsConstructor
public class VectorIndexManager {

    private final ElasticsearchClient client;
    private static final String INDEX_NAME = "vector-documents";
    private static final int INDEX_DIMS = 384;

    /**
     * 创建向量索引
     * 服务启动时自动检查并创建索引（仅执行一次）
     */
    @PostConstruct
    public void createVectorIndex() throws Exception {
        // 检查索引是否存在
        boolean exists = client.indices().exists(e -> e.index(INDEX_NAME)).value();

        if (!exists) {
            CreateIndexResponse response = client.indices().create(c -> c
                    .index(INDEX_NAME)
                    .mappings(m -> m
                            .properties("id", p -> p.keyword(k -> k))
                            .properties("title", p -> p.text(t -> t
                                    .analyzer("standard")
                                    .fields("keyword", f -> f.keyword(k -> k))
                            ))
                            .properties("content", p -> p.text(t -> t
                                    .analyzer("standard")
                            ))
                            .properties("vector", p -> p.denseVector(d -> d
                                    .dims(INDEX_DIMS)  //  根据你的向量维度调整
                                    .similarity("cosine")  // 支持 cosine, l2_norm, dot_product
                            ))
                    )
                    .settings(s -> s
                            .numberOfShards("1")
                            .numberOfReplicas("0")
                    )
            );

            System.out.println("索引创建成功: " + response.index());
        } else {
            System.out.println("索引已存在");
        }
    }

    /**
     * 删除索引
     */
    public void deleteIndex() throws Exception {
        DeleteIndexResponse response = client.indices().delete(d -> d.index(INDEX_NAME));
        System.out.println("索引删除成功: " + response.acknowledged());
    }

    /**
     *     更新文档（部分字段，如content或vector）
      */
    public void updateDocument(String id, Map<String, Object> fields) throws IOException {
        Map<String, Object> doc = new HashMap<>();
        doc.put("doc", fields);
        client.update(u -> u.index(INDEX_NAME).id(id).doc(doc), VectorDocument.class);
    }

}
