<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  BellRing,
  Bookmark,
  CheckCheck,
  Heart,
  MessageSquare,
  Trash2,
} from 'lucide-vue-next'
import {
  deleteNotificationApi,
  getNotificationsApi,
  markAllNotificationsReadApi,
  markNotificationReadApi,
} from '@/api'
import { useUserStore } from '@/store/user'
import { formatDate } from '@/utils/format'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const notifications = ref([])
const total = ref(0)
const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
})

onMounted(loadNotifications)

async function loadNotifications() {
  loading.value = true
  try {
    const page = await getNotificationsApi(pagination)
    notifications.value = page?.records || []
    total.value = Number(page?.total || 0)
  } catch (error) {
    ElMessage.error(error.message || '通知加载失败')
  } finally {
    loading.value = false
  }
}

async function openNotification(notification) {
  if (notification.isRead === 0) {
    await markAsRead(notification)
  }
  if (notification.relatedId) {
    router.push({
      name: 'image-detail',
      params: { id: notification.relatedId },
    })
  }
}

async function markAsRead(notification) {
  if (notification.isRead === 1) return
  try {
    await markNotificationReadApi(notification.id)
    notification.isRead = 1
    userStore.unreadCount = Math.max(userStore.unreadCount - 1, 0)
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  }
}

async function markAllAsRead() {
  if (!notifications.value.some((item) => item.isRead === 0)) return
  try {
    await markAllNotificationsReadApi()
    notifications.value.forEach((item) => {
      item.isRead = 1
    })
    userStore.unreadCount = 0
    ElMessage.success('已全部标记为已读')
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  }
}

async function removeNotification(notification) {
  try {
    await ElMessageBox.confirm('确定删除这条通知吗？', '删除通知', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await deleteNotificationApi(notification.id)
    if (notification.isRead === 0) {
      userStore.unreadCount = Math.max(userStore.unreadCount - 1, 0)
    }
    await loadNotifications()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

function notificationMeta(type) {
  const meta = {
    1: { icon: Heart, label: '点赞', className: 'like' },
    2: { icon: MessageSquare, label: '评论', className: 'comment' },
    3: { icon: Bookmark, label: '收藏', className: 'collect' },
    4: { icon: BellRing, label: '系统', className: 'system' },
  }
  return meta[type] || meta[4]
}

function handlePageChange(pageNum) {
  pagination.pageNum = pageNum
  loadNotifications()
}
</script>

<template>
  <div class="notification-page">
    <div class="page-heading">
      <div>
        <h1 class="page-title">通知中心</h1>
        <p class="page-subtitle">
          你有 {{ userStore.unreadCount }} 条未读消息
        </p>
      </div>
      <el-button :disabled="!userStore.unreadCount" @click="markAllAsRead">
        <CheckCheck :size="16" />
        全部已读
      </el-button>
    </div>

    <div v-loading="loading" class="notification-list surface">
      <article
        v-for="notification in notifications"
        :key="notification.id"
        class="notification-item"
        :class="{ unread: notification.isRead === 0 }"
        @click="openNotification(notification)"
      >
        <div
          class="notification-icon"
          :class="notificationMeta(notification.type).className"
        >
          <component :is="notificationMeta(notification.type).icon" :size="19" />
        </div>

        <div class="notification-main">
          <div class="notification-title">
            <strong>{{ notification.title || notificationMeta(notification.type).label }}</strong>
            <span v-if="notification.isRead === 0" class="unread-dot"></span>
          </div>
          <p>{{ notification.content }}</p>
          <time>{{ formatDate(notification.createTime) }}</time>
        </div>

        <div class="notification-actions">
          <el-button
            v-if="notification.isRead === 0"
            text
            type="primary"
            @click.stop="markAsRead(notification)"
          >
            标记已读
          </el-button>
          <el-button
            text
            type="danger"
            aria-label="删除通知"
            @click.stop="removeNotification(notification)"
          >
            <Trash2 :size="17" />
          </el-button>
        </div>
      </article>

      <div v-if="!loading && !notifications.length" class="empty-state">
        暂无通知
      </div>
    </div>

    <div v-if="total > pagination.pageSize" class="pagination-wrap">
      <el-pagination
        background
        layout="prev, pager, next"
        :current-page="pagination.pageNum"
        :page-size="pagination.pageSize"
        :total="total"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<style scoped>
.page-heading {
  min-height: 70px;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.notification-list {
  min-height: 280px;
  overflow: hidden;
}

.notification-item {
  position: relative;
  min-height: 96px;
  padding: 18px 18px 18px 20px;
  display: grid;
  grid-template-columns: 42px minmax(0, 1fr) auto;
  gap: 13px;
  align-items: start;
  border-bottom: 1px solid #edf1f3;
  cursor: pointer;
  transition: background 0.2s ease;
}

.notification-item:last-child {
  border-bottom: 0;
}

.notification-item:hover {
  background: #f7fafb;
}

.notification-item.unread {
  background: #f0f7f8;
}

.notification-icon {
  display: grid;
  width: 40px;
  height: 40px;
  place-items: center;
  border-radius: 8px;
  background: #e8f1f3;
  color: var(--app-primary);
}

.notification-icon.like {
  background: #fff0ec;
  color: #e25f43;
}

.notification-icon.comment {
  background: #eaf4fb;
  color: #347da8;
}

.notification-icon.follow {
  background: #eef1fa;
  color: #526ba5;
}

.notification-icon.collect {
  background: #fff4e8;
  color: #c87827;
}

.notification-title {
  display: flex;
  align-items: center;
  gap: 8px;
}

.notification-title strong {
  color: #263a45;
  font-size: 15px;
}

.unread-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: var(--app-accent);
}

.notification-main p {
  margin: 7px 0 6px;
  color: #61717b;
  line-height: 1.65;
}

.notification-main time {
  color: #98a3aa;
  font-size: 12px;
}

.notification-actions {
  display: flex;
  align-items: center;
}

.pagination-wrap {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

@media (max-width: 640px) {
  .page-heading {
    align-items: flex-start;
  }

  .notification-item {
    grid-template-columns: 38px minmax(0, 1fr);
    padding: 16px 12px;
  }

  .notification-actions {
    grid-column: 2;
    justify-content: flex-end;
  }
}
</style>
