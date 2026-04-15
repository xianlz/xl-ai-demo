package com.zxl.rag.entity;

import lombok.Data;

/**
 * 向量文档实体类
 */
@Data
public class VectorDocument {

    private String id;
    private String title;
    private String content;
    private float[] vector;

    // 知识库相关字段
    private String insuranceType;
    private String subType;
    private String docUuid;
    private Integer chunkIndex;

}
