<script setup>
import { onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ImagePlus, Send, UploadCloud, X } from 'lucide-vue-next'
import { predictImageApi, uploadImageApi } from '@/api'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const uploading = ref(false)
const previewUrl = ref('')
const selectedFile = ref(null)
const predicting = ref(false)
const aiApplied = ref(false)

const form = reactive({
  title: '',
  description: '',
  categoryId: null,
  tags: '',
})

const rules = {
  title: [
    { required: true, message: '请输入图片标题', trigger: 'blur' },
    { max: 200, message: '标题不能超过 200 个字符', trigger: 'blur' },
  ],
  categoryId: [{ required: true, message: '请选择图片分类', trigger: 'change' }],
  description: [
    { max: 500, message: '描述不能超过 500 个字符', trigger: 'blur' },
  ],
}

onMounted(() => {
  userStore.fetchCategories()
})

onBeforeUnmount(() => {
  if (previewUrl.value) URL.revokeObjectURL(previewUrl.value)
})

async function handleFileChange(uploadFile) {
  const raw = uploadFile.raw
  if (!raw?.type?.startsWith('image/')) {
    ElMessage.warning('请选择图片文件')
    selectedFile.value = null
    return
  }
  if (previewUrl.value) URL.revokeObjectURL(previewUrl.value)
  selectedFile.value = raw
  previewUrl.value = URL.createObjectURL(raw)
  aiApplied.value = false
  predicting.value = true

  try {
    const data = new FormData()
    data.append('file', raw)
    const prediction = await predictImageApi(data)
    if (prediction?.titleSuggestion) form.title = prediction.titleSuggestion
    if (prediction?.descriptionSuggestion) {
      form.description = prediction.descriptionSuggestion
    }
    if (prediction?.categoryId) form.categoryId = prediction.categoryId
    if (prediction?.tagsSuggestion) form.tags = prediction.tagsSuggestion
    aiApplied.value = Boolean(prediction)
  } catch {
    ElMessage.warning('AI识别失败，请手动填写图片信息')
  } finally {
    if (!form.title) form.title = raw.name.replace(/\.[^.]+$/, '')
    predicting.value = false
  }
}

function clearFile() {
  if (previewUrl.value) URL.revokeObjectURL(previewUrl.value)
  previewUrl.value = ''
  selectedFile.value = null
  predicting.value = false
  aiApplied.value = false
}

async function submitUpload() {
  if (!selectedFile.value) {
    ElMessage.warning('请先选择图片')
    return
  }
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  uploading.value = true
  try {
    const data = new FormData()
    data.append('file', selectedFile.value)
    data.append('title', form.title)
    data.append('description', form.description || '')
    data.append('categoryId', String(form.categoryId))
    data.append('tags', form.tags || '')

    const image = await uploadImageApi(data)
    ElMessage.success('图片上传成功')
    router.replace({
      name: 'image-detail',
      params: { id: image.id },
    })
  } catch (error) {
    ElMessage.error(error.message || '图片上传失败')
  } finally {
    uploading.value = false
  }
}
</script>

<template>
  <div class="upload-page">
    <div class="upload-heading">
      <div class="heading-icon"><ImagePlus :size="20" /></div>
      <div>
        <h1 class="page-title">上传图片</h1>
        <p class="page-subtitle">分享视觉灵感，系统会自动提取图像特征</p>
      </div>
    </div>

    <section class="upload-form surface">
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        size="large"
      >
        <el-form-item label="图片文件" required>
          <div class="upload-area">
            <el-upload
              v-if="!previewUrl"
              drag
              action="#"
              accept="image/*"
              :auto-upload="false"
              :show-file-list="false"
              :on-change="handleFileChange"
            >
              <UploadCloud :size="42" />
              <div class="upload-text">拖拽图片到此处，或点击选择</div>
              <div class="upload-hint">支持 JPG、PNG 等常见图片格式</div>
            </el-upload>

            <template v-else>
              <div class="preview-wrap">
                <img :src="previewUrl" alt="上传预览" />
                <button class="remove-preview" type="button" @click="clearFile">
                  <X :size="18" />
                </button>
              </div>
              <div v-if="predicting" class="ai-status">
                AI 正在分析图片并生成建议...
              </div>
              <div v-else-if="aiApplied" class="ai-status success">
                AI 已自动填充标题、描述、分类和标签，可继续修改
              </div>
            </template>
          </div>
        </el-form-item>

        <el-form-item label="图片标题" prop="title">
          <el-input
            v-model="form.title"
            maxlength="200"
            show-word-limit
            placeholder="输入图片标题"
          />
        </el-form-item>

        <el-form-item label="图片分类" prop="categoryId">
          <el-select
            v-model="form.categoryId"
            placeholder="选择分类"
            style="width: 100%"
          >
            <el-option
              v-for="category in userStore.categories"
              :key="category.id"
              :label="category.name"
              :value="category.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="图片描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="4"
            maxlength="500"
            show-word-limit
            placeholder="描述图片内容、场景或创作思路"
          />
        </el-form-item>

        <el-form-item label="标签" prop="tags">
          <el-input
            v-model="form.tags"
            maxlength="200"
            placeholder="多个标签用英文逗号分隔，例如：风景,自然,旅行"
          />
        </el-form-item>

        <div class="form-actions">
          <el-button @click="router.back()">取消</el-button>
          <el-button
            type="primary"
            :loading="uploading"
            @click="submitUpload"
          >
            <Send :size="17" />
            上传图片
          </el-button>
        </div>
      </el-form>
    </section>
  </div>
</template>

<style scoped>
.upload-heading {
  min-height: 70px;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.heading-icon {
  display: grid;
  width: 42px;
  height: 42px;
  place-items: center;
  border-radius: 8px;
  background: #e8f1f3;
  color: var(--app-primary);
}

.upload-form {
  padding: 28px 30px 24px;
}

.upload-area {
  width: 100%;
}

.upload-area :deep(.el-upload) {
  width: 100%;
}

.upload-area :deep(.el-upload-dragger) {
  width: 100%;
  min-height: 250px;
  padding: 48px 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  border-color: #ccd8dd;
  background: #f8fafb;
}

.upload-area :deep(.el-upload-dragger:hover) {
  border-color: var(--app-primary);
}

.upload-text {
  color: #3a4e59;
  font-size: 16px;
  font-weight: 600;
}

.upload-hint {
  color: #8c989f;
  font-size: 13px;
}

.ai-status {
  margin-top: 12px;
  padding: 10px 12px;
  border-radius: 6px;
  background: #eef4f6;
  color: #55707d;
  font-size: 13px;
}

.ai-status.success {
  background: #edf7f1;
  color: #3f7757;
}
.preview-wrap {
  position: relative;
  width: 100%;
  max-height: 520px;
  overflow: hidden;
  border-radius: 8px;
  background: #eaf0f2;
}

.preview-wrap img {
  display: block;
  width: 100%;
  max-height: 520px;
  object-fit: contain;
}

.remove-preview {
  position: absolute;
  top: 12px;
  right: 12px;
  display: grid;
  width: 36px;
  height: 36px;
  place-items: center;
  border: 0;
  border-radius: 50%;
  background: rgba(15, 28, 35, 0.72);
  color: #fff;
  cursor: pointer;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  padding-top: 6px;
}

@media (max-width: 640px) {
  .upload-form {
    padding: 20px 16px;
  }

  .upload-area :deep(.el-upload-dragger) {
    min-height: 190px;
  }
}
</style>
