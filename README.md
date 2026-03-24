# XL AI RAG 知识库系统

基于 Spring AI 整合阿里百炼（灵积）平台实现的 RAG（检索增强生成）知识库系统，提供前后端完整解决方案。

## 技术栈

### 后端
- Spring Boot 3.3.8
- Spring AI
- 阿里百炼 DashScope API
- Elasticsearch (带向量插件)

### 前端
- Vue 3
- Element Plus
- Vite
- Axios

## 项目结构

```
xl-ai-demo/
├── xl-ai-rag/               # 后端项目
│   ├── src/main/java/
│   │   └── com/zxl/rag/
│   │       ├── controller/  # REST API 控制器
│   │       ├── service/     # 业务服务
│   │       ├── entity/      # 实体类
│   │       ├── config/      # 配置类
│   │       ├── manager/     # 管理器
│   │       └── util/        # 工具类
│   └── src/main/resources/
│       └── application.properties
│
├── xl-ai-web/               # 前端项目
│   ├── src/
│   │   ├── components/
│   │   │   ├── FileUpload.vue    # 文件上传组件
│   │   │   └── ChatInterface.vue # 问答聊天界面
│   │   ├── App.vue
│   │   └── main.js
│   ├── index.html
│   ├── vite.config.js
│   └── package.json
│
└── docker-compose/
    └── ElasticsearchwithVectorPlugin.yml  # Elasticsearch 配置
```

## 功能特性

- 文件上传：支持 txt、md、pdf、doc、docx 格式
- 文档向量化：自动将文档内容转换为向量存储
- 智能问答：基于 RAG 技术的知识库问答
- 混合搜索：向量检索 + 文本匹配

## 快速开始

### 1. 启动 Elasticsearch

```bash
cd docker-compose
docker-compose -f ElasticsearchwithVectorPlugin.yml up -d
```

### 2. 配置后端

编辑 `xl-ai-rag/src/main/resources/application.properties`：

```properties
spring.application.name=xl-ai-rag
server.port=9006
spring.ai.dashscope.api-key=your-api-key

# Elasticsearch 配置
spring.elasticsearch.uris=https://localhost:9200
spring.elasticsearch.username=your-username
spring.elasticsearch.password=your-password
```

### 3. 启动后端

```bash
cd xl-ai-rag
mvn spring-boot:run
```

### 4. 启动前端

```bash
cd xl-ai-web
npm install
npm run dev
```

前端默认运行在 http://localhost:5173 ，会自动代理 API 请求到后端。

## API 接口

### 上传文件到知识库

```http
POST /api/knowledge/upload
Content-Type: multipart/form-data

Parameters:
- file: 文件
- userId: 用户ID

Response:
{
  "msg": "文件导入成功",
  "documentCount": 10
}
```

### RAG 问答

```http
POST /api/knowledge/chat
Content-Type: application/json

Request:
{
  "question": "你的问题",
  "userId": "user123",
  "topK": 5
}

Response:
{
  "question": "你的问题",
  "answer": "AI 回答"
}
```

## 使用说明

1. 启动前后端服务
2. 在左侧上传文档（txt/md/pdf/doc/docx）
3. 文档会自动解析、向量化并存储
4. 在右侧向 AI 助手提问
5. 系统会从知识库中检索相关内容并生成回答
