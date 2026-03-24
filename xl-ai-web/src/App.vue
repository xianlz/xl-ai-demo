<template>
  <div class="app-container">
    <el-container>
      <el-header class="header">
        <h1>XL AI 知识库问答系统</h1>
      </el-header>
      <el-main>
        <el-row :gutter="20">
          <!-- 左侧：文件上传 -->
          <el-col :span="8">
            <el-card class="upload-card">
              <template #header>
                <div class="card-header">
                  <span>知识库管理</span>
                </div>
              </template>
              <FileUpload @upload-success="handleUploadSuccess" />
            </el-card>
          </el-col>

          <!-- 右侧：问答界面 -->
          <el-col :span="16">
            <el-card class="chat-card">
              <template #header>
                <div class="card-header">
                  <span>智能问答</span>
                </div>
              </template>
              <ChatInterface />
            </el-card>
          </el-col>
        </el-row>
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import FileUpload from './components/FileUpload.vue'
import ChatInterface from './components/ChatInterface.vue'
import { ElMessage } from 'element-plus'

const handleUploadSuccess = (count) => {
  ElMessage.success(`文件上传成功，已处理 ${count} 个文档`)
}
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', Arial, sans-serif;
  background: #f5f7fa;
}

.app-container {
  min-height: 100vh;
}

.header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  align-items: center;
  padding: 0 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.header h1 {
  font-size: 24px;
  font-weight: 600;
}

.upload-card, .chat-card {
  height: calc(100vh - 120px);
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.upload-card .el-card__body, .chat-card .el-card__body {
  flex: 1;
  overflow: auto;
}

.card-header {
  font-weight: 600;
  font-size: 16px;
}
</style>
