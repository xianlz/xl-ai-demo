<template>
  <div class="file-upload">
    <el-upload
      ref="uploadRef"
      class="upload-demo"
      drag
      :action="uploadUrl"
      :data="uploadData"
      :auto-upload="false"
      :on-change="handleChange"
      :on-success="handleSuccess"
      :on-error="handleError"
      :on-exceed="handleExceed"
      :limit="10"
      accept=".txt,.md,.pdf,.doc,.docx"
      multiple
    >
      <el-icon class="el-icon--upload"><upload-filled /></el-icon>
      <div class="el-upload__text">
        拖拽文件到这里 或 <em>点击上传</em>
      </div>
      <template #tip>
        <div class="el-upload__tip">
          支持 txt、md、pdf、doc、docx 格式，单个文件不超过 10MB
        </div>
      </template>
    </el-upload>

    <div class="upload-actions">
      <el-button type="primary" :loading="uploading" @click="submitUpload">
        {{ uploading ? '上传中...' : '确认上传' }}
      </el-button>
      <el-button @click="clearFiles">清空</el-button>
    </div>

    <div class="file-list" v-if="fileList.length > 0">
      <h4>已选择文件：</h4>
      <el-list>
        <el-list-item v-for="(file, index) in fileList" :key="index">
          <div class="file-item">
            <el-icon><document /></el-icon>
            <span>{{ file.name }}</span>
            <span class="file-size">{{ formatSize(file.size) }}</span>
          </div>
        </el-list-item>
      </el-list>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { UploadFilled, Document } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const uploadRef = ref(null)
const uploading = ref(false)
const fileList = ref([])
const userId = ref('user_' + Math.random().toString(36).substr(2, 9))

const uploadUrl = '/api/knowledge/upload'
const uploadData = ref({ userId: userId.value })

const handleChange = (file, files) => {
  fileList.value = files
}

const handleSuccess = (response) => {
  uploading.value = false
  ElMessage.success(response.msg || '上传成功')
  emit('upload-success', response.documentCount)
  clearFiles()
}

const handleError = (error) => {
  uploading.value = false
  ElMessage.error('上传失败: ' + error.message)
}

const handleExceed = () => {
  ElMessage.warning('最多只能上传 10 个文件')
}

const submitUpload = () => {
  if (fileList.value.length === 0) {
    ElMessage.warning('请先选择文件')
    return
  }
  uploading.value = true
  uploadRef.value.submit()
}

const clearFiles = () => {
  uploadRef.value.clearFiles()
  fileList.value = []
}

const formatSize = (size) => {
  if (size < 1024) return size + ' B'
  if (size < 1024 * 1024) return (size / 1024).toFixed(1) + ' KB'
  return (size / (1024 * 1024)).toFixed(1) + ' MB'
}

const emit = defineEmits(['upload-success'])
</script>

<style scoped>
.file-upload {
  padding: 10px;
}

.upload-demo {
  width: 100%;
}

:deep(.el-upload-dragger) {
  padding: 20px;
}

.upload-actions {
  margin-top: 20px;
  display: flex;
  gap: 10px;
}

.file-list {
  margin-top: 20px;
}

.file-list h4 {
  margin-bottom: 10px;
  color: #606266;
}

.file-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 0;
}

.file-size {
  color: #909399;
  font-size: 12px;
  margin-left: auto;
}
</style>
