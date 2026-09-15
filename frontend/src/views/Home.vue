<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Bookmark,
  Heart,
  Image as ImageIcon,
  Sparkles,
} from 'lucide-vue-next'
import {
  getImageListApi,
  getRecommendationsApi,
} from '@/api'
import { useUserStore } from '@/store/user'
import { formatCount } from '@/utils/format'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const loading = ref(true)
const images = ref([])
const visibleCount = ref(20)

const selectedCategoryId = computed(() =>
  route.query.categoryId ? Number(route.query.categoryId) : 0,
)

const keyword = computed(() => String(route.query.keyword || '').trim())

const selectedCategoryName = computed(() => {
  if (!selectedCategoryId.value) return '发现灵感'
  return (
    userStore.categories.find(
      (category) => category.id === selectedCategoryId.value,
    )?.name || '图片推荐'
  )
})

const filteredImages = computed(() => {
  const normalizedKeyword = keyword.value.toLowerCase()
  return images.value.filter((image) => {
    const categoryMatches =
      !selectedCategoryId.value || image.categoryId === selectedCategoryId.value
    if (!categoryMatches) return false
    if (!normalizedKeyword) return true
    return [image.title, image.description, image.tags, image.authorName]
      .filter(Boolean)
      .some((value) => String(value).toLowerCase().includes(normalizedKeyword))
  })
})

const displayedImages = computed(() =>
  filteredImages.value.slice(0, visibleCount.value),
)

const hasMore = computed(() => visibleCount.value < filteredImages.value.length)

watch(
  () => [route.query.categoryId, route.query.keyword],
  () => {
    visibleCount.value = 20
  },
)

onMounted(async () => {
  await Promise.allSettled([
    userStore.fetchCategories(),
    loadImages(),
  ])
})

async function loadImages() {
  loading.value = true
  try {
    const [recommendedIds, imagePage] = await Promise.all([
      getRecommendationsApi({ count: 80 }),
      getImageListApi({ pageNum: 1, pageSize: 100 }),
    ])

    const allImages = imagePage?.records || []
    const imageMap = new Map(allImages.map((image) => [image.id, image]))
    const ordered = []
    const seen = new Set()

    for (const imageId of recommendedIds || []) {
      const image = imageMap.get(Number(imageId))
      if (image && !seen.has(image.id)) {
        ordered.push(image)
        seen.add(image.id)
      }
    }
    for (const image of allImages) {
      if (!seen.has(image.id)) ordered.push(image)
    }
    images.value = ordered
  } catch (error) {
    try {
      const imagePage = await getImageListApi({ pageNum: 1, pageSize: 100 })
      images.value = imagePage?.records || []
    } catch {
      images.value = []
      ElMessage.error(error.message || '图片加载失败')
    }
  } finally {
    loading.value = false
  }
}

function imageFrameStyle(image) {
  const width = Number(image.width || 0)
  const height = Number(image.height || 0)
  const ratio = width > 0 && height > 0 ? (height / width) * 100 : 75
  return { paddingTop: `${Math.min(Math.max(ratio, 55), 145)}%` }
}

function categoryName(categoryId) {
  return (
    userStore.categories.find((category) => category.id === categoryId)?.name ||
    '其他'
  )
}

function openImage(imageId) {
  router.push({ name: 'image-detail', params: { id: imageId } })
}

function loadMore() {
  visibleCount.value += 20
}
</script>

<template>
  <div class="home-page">
    <div class="gallery-heading">
      <div>
        <span class="heading-icon"><Sparkles :size="18" /></span>
        <div>
          <h1 class="page-title">{{ selectedCategoryName }}</h1>
          <p class="page-subtitle">
            {{
              keyword
                ? `“${keyword}” 的搜索结果`
                : '基于图像特征与你的浏览偏好持续更新'
            }}
          </p>
        </div>
      </div>
      <span>{{ filteredImages.length }} 张图片</span>
    </div>

    <div v-if="loading" class="masonry-grid">
      <div v-for="index in 9" :key="index" class="skeleton-card">
        <el-skeleton animated>
          <template #template>
            <el-skeleton-item
              variant="image"
              :style="{ height: `${180 + (index % 3) * 55}px`, width: '100%' }"
            />
            <div class="skeleton-copy">
              <el-skeleton-item variant="h3" style="width: 70%" />
              <el-skeleton-item variant="text" style="width: 45%" />
            </div>
          </template>
        </el-skeleton>
      </div>
    </div>

    <div v-else-if="displayedImages.length" class="masonry-grid">
      <article
        v-for="image in displayedImages"
        :key="image.id"
        class="image-card"
        @click="openImage(image.id)"
      >
        <div class="image-frame" :style="imageFrameStyle(image)">
          <el-image
            lazy
            class="gallery-image"
            :src="image.thumbnailUrl || image.url"
            :alt="image.title || '推荐图片'"
            fit="cover"
          >
            <template #placeholder>
              <div class="image-placeholder">
                <ImageIcon :size="28" />
              </div>
            </template>
            <template #error>
              <div class="image-placeholder">
                <ImageIcon :size="28" />
              </div>
            </template>
          </el-image>

          <div class="image-overlay">
            <div class="overlay-tag">{{ categoryName(image.categoryId) }}</div>
            <p>{{ image.description || image.tags || '探索这张图片的更多细节' }}</p>
          </div>
        </div>

        <div class="image-info">
          <h2>{{ image.title || '未命名图片' }}</h2>
          <div class="image-meta">
            <span>{{ image.authorName || `用户${image.authorId}` }}</span>
            <div>
              <span><Heart :size="14" /> {{ formatCount(image.likeCount) }}</span>
              <span><Bookmark :size="14" /> {{ formatCount(image.collectCount) }}</span>
            </div>
          </div>
        </div>
      </article>
    </div>

    <div v-else class="empty-state surface">
      <ImageIcon :size="38" />
      <p>暂时没有匹配的图片</p>
    </div>

    <div v-if="!loading && filteredImages.length" class="load-more">
      <el-button v-if="hasMore" size="large" @click="loadMore">
        加载更多
      </el-button>
      <span v-else>已加载全部图片</span>
    </div>
  </div>
</template>

<style scoped>
.gallery-heading {
  min-height: 70px;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.gallery-heading > div {
  display: flex;
  align-items: center;
  gap: 12px;
}

.gallery-heading > span {
  color: var(--app-muted);
  font-size: 13px;
}

.heading-icon {
  display: grid;
  width: 40px;
  height: 40px;
  place-items: center;
  border-radius: 8px;
  background: #fff3ee;
  color: var(--app-accent);
}

.masonry-grid {
  column-count: 3;
  column-gap: 16px;
}

.image-card,
.skeleton-card {
  display: inline-block;
  width: 100%;
  margin: 0 0 16px;
  overflow: hidden;
  border: 1px solid var(--app-border);
  border-radius: 8px;
  background: #fff;
  break-inside: avoid;
}

.image-card {
  cursor: pointer;
  transition:
    transform 0.2s ease,
    box-shadow 0.2s ease,
    border-color 0.2s ease;
}

.image-card:hover {
  transform: translateY(-2px);
  border-color: #bfd3da;
  box-shadow: 0 12px 30px rgba(30, 62, 75, 0.12);
}

.image-frame {
  position: relative;
  overflow: hidden;
  background: #e8eef1;
}

.gallery-image {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
}

.image-placeholder {
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  color: #93a3ac;
  background: #edf2f4;
}

.image-overlay {
  position: absolute;
  inset: 0;
  padding: 18px;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  gap: 8px;
  color: #fff;
  background: linear-gradient(transparent 38%, rgba(11, 28, 37, 0.86));
  opacity: 0;
  transition: opacity 0.22s ease;
}

.image-card:hover .image-overlay {
  opacity: 1;
}

.overlay-tag {
  width: fit-content;
  padding: 4px 8px;
  border-radius: 4px;
  background: rgba(255, 255, 255, 0.18);
  font-size: 12px;
}

.image-overlay p {
  display: -webkit-box;
  margin: 0;
  overflow: hidden;
  font-size: 13px;
  line-height: 1.55;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.image-info {
  padding: 12px 13px 14px;
}

.image-info h2 {
  margin: 0;
  overflow: hidden;
  color: #20323d;
  font-size: 15px;
  line-height: 1.45;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.image-meta {
  margin-top: 9px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  color: #7e8b94;
  font-size: 12px;
}

.image-meta > span {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.image-meta div {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
}

.image-meta div span {
  display: inline-flex;
  align-items: center;
  gap: 3px;
}

.skeleton-copy {
  padding: 13px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.load-more {
  padding: 20px 0 8px;
  display: flex;
  justify-content: center;
  color: var(--app-muted);
  font-size: 13px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.empty-state p {
  margin: 0;
}

@media (max-width: 1080px) {
  .masonry-grid {
    column-count: 2;
  }
}

@media (max-width: 640px) {
  .masonry-grid {
    column-count: 1;
  }
}
</style>
