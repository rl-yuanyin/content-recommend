<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  ArrowLeft,
  Bookmark,
  Download,
  Eye,
  Heart,
  MessageSquare,
  Send,
  UserRound,
} from 'lucide-vue-next'
import {
  addCommentApi,
  collectImageApi,
  downloadImageApi,
  getCommentsApi,
  getSimilarImagesApi,
  getImageDetailApi,
  getImageListApi,
  likeImageApi,
  reportBehaviorApi,
} from '@/api'
import { useUserStore } from '@/store/user'
import { formatCount, formatDate } from '@/utils/format'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const image = ref(null)
const comments = ref([])
const relatedImages = ref([])
const loading = ref(true)
const commentContent = ref('')
const commentLoading = ref(false)
const actionLoading = ref(false)

const imageId = computed(() => Number(route.params.id))

const categoryName = computed(
  () =>
    userStore.categories.find(
      (category) => category.id === image.value?.categoryId,
    )?.name || '其他',
)

const tagList = computed(() =>
  String(image.value?.tags || '')
    .split(',')
    .map((tag) => tag.trim())
    .filter(Boolean),
)

onMounted(async () => {
  await Promise.allSettled([
    userStore.fetchCategories(),
    loadImage(),
    loadComments(),
    loadRelated(),
  ])
  reportBehavior(1, 0)
})

async function loadImage() {
  loading.value = true
  try {
    image.value = await getImageDetailApi(imageId.value)
  } catch (error) {
    ElMessage.error(error.message || '图片加载失败')
  } finally {
    loading.value = false
  }
}

async function loadComments() {
  try {
    const page = await getCommentsApi({
      imageId: imageId.value,
      pageNum: 1,
      pageSize: 50,
    })
    comments.value = page?.records || []
  } catch {
    comments.value = []
  }
}

async function loadRelated() {
  try {
    const [recommendedIds, imagePage] = await Promise.all([
      getSimilarImagesApi(imageId.value, { count: 12 }),
      getImageListApi({ pageNum: 1, pageSize: 100 }),
    ])
    const imageMap = new Map(
      (imagePage?.records || []).map((item) => [item.id, item]),
    )
    relatedImages.value = (recommendedIds || [])
      .map((id) => imageMap.get(Number(id)))
      .filter((item) => item && item.id !== imageId.value)
      .slice(0, 12)
  } catch {
    relatedImages.value = []
  }
}

function reportBehavior(behaviorType, duration = 0) {
  reportBehaviorApi({
    imageId: imageId.value,
    behaviorType,
    duration,
  }).catch(() => {})
}

async function toggleLike() {
  await toggleInteraction('like')
}

async function toggleCollect() {
  await toggleInteraction('collect')
}

async function toggleInteraction(type) {
  if (actionLoading.value || !image.value) return
  actionLoading.value = true
  try {
    const active =
      type === 'like'
        ? await likeImageApi(imageId.value)
        : await collectImageApi(imageId.value)
    const countField = type === 'like' ? 'likeCount' : 'collectCount'
    const current = Number(image.value[countField] || 0)
    image.value[countField] = active ? current + 1 : Math.max(current - 1, 0)
    ElMessage.success(
      type === 'like'
        ? active
          ? '点赞成功'
          : '已取消点赞'
        : active
          ? '收藏成功'
          : '已取消收藏',
    )
    reportBehavior(type === 'like' ? 2 : 5, 0)
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  } finally {
    actionLoading.value = false
  }
}

async function downloadImage() {
  if (!image.value || actionLoading.value) return
  actionLoading.value = true
  try {
    const url = await downloadImageApi(imageId.value)
    const link = document.createElement('a')
    link.href = url
    link.target = '_blank'
    link.download = image.value.title || 'image'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    image.value.downloadCount = Number(image.value.downloadCount || 0) + 1
    reportBehavior(6, 0)
  } catch (error) {
    ElMessage.error(error.message || '下载失败')
  } finally {
    actionLoading.value = false
  }
}

async function submitComment() {
  const content = commentContent.value.trim()
  if (!content) {
    ElMessage.warning('请输入评论内容')
    return
  }

  commentLoading.value = true
  try {
    await addCommentApi({ imageId: imageId.value, content, parentId: 0 })
    commentContent.value = ''
    await loadComments()
    reportBehavior(3, 0)
    ElMessage.success('评论发布成功')
  } catch (error) {
    ElMessage.error(error.message || '评论发布失败')
  } finally {
    commentLoading.value = false
  }
}

function openRelated(item) {
  router.push({ name: 'image-detail', params: { id: item.id } })
}
</script>

<template>
  <div class="image-detail-page">
    <button class="back-button" type="button" @click="router.back()">
      <ArrowLeft :size="17" />
      返回
    </button>

    <el-skeleton v-if="loading" class="surface detail-skeleton" :rows="10" animated />

    <template v-else-if="image">
      <section class="detail-layout surface">
        <div class="hero-image">
          <el-image
            :src="image.url"
            :preview-src-list="[image.url]"
            fit="contain"
            :alt="image.title"
          />
        </div>

        <aside class="image-sidebar">
          <el-tag effect="plain">{{ categoryName }}</el-tag>
          <h1>{{ image.title || '未命名图片' }}</h1>
          <p class="description">
            {{ image.description || '这张图片暂时没有描述。' }}
          </p>

          <div class="tag-list" v-if="tagList.length">
            <el-tag
              v-for="tag in tagList"
              :key="tag"
              type="info"
              effect="plain"
              size="small"
            >
              # {{ tag }}
            </el-tag>
          </div>

          <div class="author-row">
            <el-avatar :size="40">
              {{ (image.authorName || '用').slice(0, 1).toUpperCase() }}
            </el-avatar>
            <div>
              <strong>{{ image.authorName || `用户${image.authorId}` }}</strong>
              <span>{{ formatDate(image.createTime) }}</span>
            </div>
          </div>

          <div class="stats-row">
            <span><Eye :size="16" /> {{ formatCount(image.viewCount) }}</span>
            <span><Heart :size="16" /> {{ formatCount(image.likeCount) }}</span>
            <span><Bookmark :size="16" /> {{ formatCount(image.collectCount) }}</span>
            <span><Download :size="16" /> {{ formatCount(image.downloadCount) }}</span>
          </div>

          <div class="action-row">
            <el-button
              type="primary"
              :loading="actionLoading"
              @click="toggleLike"
            >
              <Heart :size="17" /> 点赞
            </el-button>
            <el-button :loading="actionLoading" @click="toggleCollect">
              <Bookmark :size="17" /> 收藏
            </el-button>
            <el-button :loading="actionLoading" @click="downloadImage">
              <Download :size="17" /> 下载
            </el-button>
          </div>
        </aside>
      </section>

      <section class="comment-section surface">
        <div class="section-heading">
          <div>
            <h2>评论</h2>
            <p>聊聊这张图片给你的感受</p>
          </div>
          <span><MessageSquare :size="16" /> {{ comments.length }}</span>
        </div>

        <div class="comment-editor">
          <el-avatar :size="36">
            {{ userStore.displayName.slice(0, 1).toUpperCase() }}
          </el-avatar>
          <div class="editor-main">
            <el-input
              v-model="commentContent"
              type="textarea"
              :rows="3"
              maxlength="500"
              show-word-limit
              placeholder="写下你的评论..."
              @keyup.ctrl.enter="submitComment"
            />
            <div class="editor-actions">
              <el-button
                type="primary"
                :loading="commentLoading"
                @click="submitComment"
              >
                <Send :size="16" /> 发表评论
              </el-button>
            </div>
          </div>
        </div>

        <div v-if="comments.length" class="comment-list">
          <div v-for="comment in comments" :key="comment.id" class="comment-item">
            <el-avatar :size="34">
              {{ (comment.username || '用').slice(0, 1).toUpperCase() }}
            </el-avatar>
            <div class="comment-body">
              <div class="comment-head">
                <strong>{{ comment.username || `用户${comment.userId}` }}</strong>
                <span>{{ formatDate(comment.createTime) }}</span>
              </div>
              <p>{{ comment.content }}</p>
            </div>
          </div>
        </div>
        <div v-else class="empty-state">还没有评论，来留下第一条吧。</div>
      </section>

      <section v-if="relatedImages.length" class="related-section">
        <div class="section-heading">
          <div>
            <h2>相似图片</h2>
            <p>基于图像深度学习特征相似度</p>
          </div>
        </div>
        <div class="related-grid">
          <article
            v-for="item in relatedImages"
            :key="item.id"
            class="related-card"
            @click="openRelated(item)"
          >
            <div class="related-image">
              <el-image lazy :src="item.thumbnailUrl || item.url" fit="cover" />
            </div>
            <h3>{{ item.title || '未命名图片' }}</h3>
            <span>{{ item.authorName || `用户${item.authorId}` }}</span>
          </article>
        </div>
      </section>
    </template>

    <div v-else class="empty-state surface">图片不存在或已被删除。</div>
  </div>
</template>

<style scoped>
.image-detail-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.back-button {
  width: fit-content;
  padding: 5px 0;
  display: inline-flex;
  align-items: center;
  gap: 5px;
  border: 0;
  background: transparent;
  color: #697983;
  cursor: pointer;
}

.detail-skeleton {
  padding: 28px;
}

.detail-layout {
  display: grid;
  grid-template-columns: minmax(0, 1.55fr) minmax(310px, 0.75fr);
  overflow: hidden;
}

.hero-image {
  min-height: 560px;
  padding: 18px;
  display: grid;
  place-items: center;
  background: #101a20;
}

.hero-image :deep(.el-image) {
  width: 100%;
  height: min(72vh, 720px);
}

.image-sidebar {
  padding: 30px 26px;
  display: flex;
  flex-direction: column;
}

.image-sidebar h1 {
  margin: 16px 0 12px;
  color: #172a35;
  font-size: 27px;
  line-height: 1.35;
  letter-spacing: 0;
}

.description {
  margin: 0;
  color: #5f707a;
  font-size: 15px;
  line-height: 1.8;
}

.tag-list {
  margin-top: 18px;
  display: flex;
  flex-wrap: wrap;
  gap: 7px;
}

.author-row {
  margin-top: 26px;
  padding: 18px 0;
  display: flex;
  align-items: center;
  gap: 11px;
  border-top: 1px solid var(--app-border);
  border-bottom: 1px solid var(--app-border);
}

.author-row div {
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.author-row strong {
  color: #263943;
}

.author-row span {
  color: #8a969e;
  font-size: 12px;
}

.stats-row {
  padding: 18px 0;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  color: #667680;
  font-size: 13px;
}

.stats-row span {
  display: inline-flex;
  align-items: center;
  gap: 5px;
}

.action-row {
  margin-top: auto;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.action-row :deep(.el-button) {
  flex: 1;
  margin-left: 0;
}

.comment-section {
  padding: 24px;
}

.section-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
}

.section-heading h2 {
  margin: 0;
  color: #1d303a;
  font-size: 19px;
}

.section-heading p {
  margin: 4px 0 0;
  color: var(--app-muted);
  font-size: 13px;
}

.section-heading > span {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  color: var(--app-muted);
  font-size: 13px;
}

.comment-editor {
  padding: 16px;
  display: flex;
  gap: 12px;
  border-radius: 6px;
  background: #f6f8f9;
}

.editor-main {
  min-width: 0;
  flex: 1;
}

.editor-actions {
  margin-top: 10px;
  display: flex;
  justify-content: flex-end;
}

.comment-list {
  margin-top: 10px;
}

.comment-item {
  padding: 18px 4px;
  display: flex;
  gap: 12px;
  border-bottom: 1px solid #edf1f3;
}

.comment-item:last-child {
  border-bottom: 0;
}

.comment-body {
  min-width: 0;
  flex: 1;
}

.comment-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.comment-head strong {
  color: #2c404b;
  font-size: 14px;
}

.comment-head span {
  color: #98a3aa;
  font-size: 12px;
}

.comment-body p {
  margin: 8px 0 0;
  color: #4f606a;
  line-height: 1.7;
  white-space: pre-wrap;
}

.related-section {
  padding-top: 6px;
}

.related-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}

.related-card {
  overflow: hidden;
  border: 1px solid var(--app-border);
  border-radius: 8px;
  background: #fff;
  cursor: pointer;
  transition:
    transform 0.2s ease,
    box-shadow 0.2s ease;
}

.related-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 28px rgba(32, 62, 75, 0.1);
}

.related-image {
  aspect-ratio: 4 / 3;
  overflow: hidden;
  background: #edf2f4;
}

.related-image :deep(.el-image) {
  width: 100%;
  height: 100%;
}

.related-card h3 {
  margin: 12px 12px 5px;
  overflow: hidden;
  color: #263943;
  font-size: 14px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.related-card > span {
  display: block;
  margin: 0 12px 13px;
  color: #89959d;
  font-size: 12px;
}

@media (max-width: 900px) {
  .detail-layout {
    grid-template-columns: 1fr;
  }

  .hero-image {
    min-height: 360px;
  }

  .hero-image :deep(.el-image) {
    height: 52vh;
  }
}

@media (max-width: 640px) {
  .image-sidebar,
  .comment-section {
    padding: 20px 16px;
  }

  .related-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
