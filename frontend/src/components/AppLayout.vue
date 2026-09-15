<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  Bell,
  Building2,
  Camera,
  ChevronRight,
  Cpu,
  Home,
  LogOut,
  Menu,
  Mountain,
  Palette,
  PawPrint,
  PenLine,
  Search,
  Shapes,
  UtensilsCrossed,
} from 'lucide-vue-next'
import { useUserStore } from '@/store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const searchKeyword = ref(String(route.query.keyword || ''))
const drawerVisible = ref(false)

const activeCategoryId = computed(() =>
  route.query.categoryId ? Number(route.query.categoryId) : 0,
)

const avatarText = computed(() =>
  userStore.displayName.slice(0, 1).toUpperCase(),
)

watch(
  () => route.query.keyword,
  (value) => {
    searchKeyword.value = String(value || '')
  },
)

onMounted(async () => {
  await Promise.allSettled([
    userStore.fetchUserInfo(),
    userStore.fetchCategories(),
    userStore.fetchUnreadCount(),
  ])
})

function submitSearch() {
  router.push({
    name: 'home',
    query: {
      ...route.query,
      keyword: searchKeyword.value.trim() || undefined,
    },
  })
  drawerVisible.value = false
}

function selectCategory(categoryId) {
  router.push({
    name: 'home',
    query: {
      ...route.query,
      categoryId: categoryId || undefined,
    },
  })
  drawerVisible.value = false
}

function categoryIcon(name) {
  if (name?.includes('风景')) return Mountain
  if (name?.includes('人物')) return Camera
  if (name?.includes('动物')) return PawPrint
  if (name?.includes('建筑')) return Building2
  if (name?.includes('美食')) return UtensilsCrossed
  if (name?.includes('科技')) return Cpu
  if (name?.includes('艺术')) return Palette
  return Shapes
}

function handleUserCommand(command) {
  if (command === 'logout') {
    userStore.logout()
    router.replace('/login')
    return
  }
  router.push({ name: command })
}
</script>

<template>
  <div class="app-layout">
    <header class="topbar">
      <div class="topbar-inner">
        <button class="mobile-menu" type="button" @click="drawerVisible = true">
          <Menu :size="21" />
        </button>

        <router-link class="brand" :to="{ name: 'home' }">
          <span class="brand-mark">智</span>
          <span class="brand-copy">
            <strong>智荐</strong>
            <small>图像推荐平台</small>
          </span>
        </router-link>

        <div class="search-area">
          <el-input
            v-model="searchKeyword"
            clearable
            placeholder="搜索图片、标签或作者"
            @keyup.enter="submitSearch"
          >
            <template #prefix>
              <Search :size="17" />
            </template>
          </el-input>
        </div>

        <nav class="topnav">
          <router-link :to="{ name: 'home' }">
            <Home :size="17" />
            <span>发现</span>
          </router-link>
          <router-link :to="{ name: 'upload' }">
            <PenLine :size="17" />
            <span>上传</span>
          </router-link>
        </nav>

        <router-link class="notification-link" :to="{ name: 'notifications' }">
          <el-badge
            :value="userStore.unreadCount"
            :hidden="userStore.unreadCount <= 0"
            :max="99"
          >
            <Bell :size="20" />
          </el-badge>
        </router-link>

        <el-dropdown trigger="click" @command="handleUserCommand">
          <button class="user-trigger" type="button">
            <el-avatar :size="34" :src="userStore.user?.avatar || ''">
              {{ avatarText }}
            </el-avatar>
            <span>{{ userStore.displayName }}</span>
          </button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="upload">
                <PenLine :size="15" /> 上传图片
              </el-dropdown-item>
              <el-dropdown-item command="notifications">
                <Bell :size="15" /> 通知中心
              </el-dropdown-item>
              <el-dropdown-item divided command="logout">
                <LogOut :size="15" /> 退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </header>

    <main class="main-area">
      <div class="page-shell layout-grid">
        <aside class="sidebar surface">
          <div class="sidebar-heading">图片分类</div>
          <button
            class="category-item"
            :class="{ active: activeCategoryId === 0 }"
            type="button"
            @click="selectCategory(0)"
          >
            <span class="category-icon"><Shapes :size="18" /></span>
            <span>全部图片</span>
            <ChevronRight class="category-arrow" :size="16" />
          </button>
          <button
            v-for="category in userStore.categories"
            :key="category.id"
            class="category-item"
            :class="{ active: activeCategoryId === category.id }"
            type="button"
            @click="selectCategory(category.id)"
          >
            <span class="category-icon">
              <component :is="categoryIcon(category.name)" :size="18" />
            </span>
            <span>{{ category.name }}</span>
            <ChevronRight class="category-arrow" :size="16" />
          </button>

          <div class="sidebar-user">
            <span>当前用户</span>
            <strong>{{ userStore.displayName }}</strong>
          </div>
        </aside>

        <section class="content-column">
          <router-view />
        </section>
      </div>
    </main>

    <el-drawer
      v-model="drawerVisible"
      direction="ltr"
      size="280px"
      :with-header="false"
    >
      <div class="mobile-drawer">
        <div class="sidebar-heading">图片分类</div>
        <button
          class="category-item"
          :class="{ active: activeCategoryId === 0 }"
          type="button"
          @click="selectCategory(0)"
        >
          <span class="category-icon"><Shapes :size="18" /></span>
          <span>全部图片</span>
        </button>
        <button
          v-for="category in userStore.categories"
          :key="category.id"
          class="category-item"
          :class="{ active: activeCategoryId === category.id }"
          type="button"
          @click="selectCategory(category.id)"
        >
          <span class="category-icon">
            <component :is="categoryIcon(category.name)" :size="18" />
          </span>
          <span>{{ category.name }}</span>
        </button>
      </div>
    </el-drawer>
  </div>
</template>

<style scoped>
.app-layout {
  min-height: 100vh;
}

.topbar {
  position: sticky;
  top: 0;
  z-index: 30;
  height: 64px;
  background: rgba(255, 255, 255, 0.96);
  border-bottom: 1px solid var(--app-border);
  backdrop-filter: blur(12px);
}

.topbar-inner {
  width: min(1240px, calc(100% - 32px));
  height: 100%;
  margin: 0 auto;
  display: flex;
  align-items: center;
  gap: 22px;
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: max-content;
}

.brand-mark {
  display: grid;
  width: 36px;
  height: 36px;
  place-items: center;
  border-radius: 8px;
  background: var(--app-primary);
  color: #fff;
  font-weight: 800;
  font-size: 19px;
}

.brand-copy {
  display: flex;
  flex-direction: column;
  line-height: 1.1;
}

.brand-copy strong {
  color: var(--app-text);
  font-size: 18px;
}

.brand-copy small {
  margin-top: 3px;
  color: var(--app-muted);
  font-size: 11px;
}

.search-area {
  flex: 1;
  max-width: 480px;
  margin: 0 auto;
}

.topnav {
  display: flex;
  align-items: center;
  gap: 4px;
}

.topnav a {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 10px;
  border-radius: 6px;
  color: #60717d;
  font-size: 14px;
}

.topnav a.router-link-active {
  color: var(--app-primary);
  background: var(--el-color-primary-light-9);
  font-weight: 600;
}

.notification-link {
  display: grid;
  width: 36px;
  height: 36px;
  place-items: center;
  color: #536570;
}

.user-trigger {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 0;
  border: 0;
  background: transparent;
  color: #354650;
  cursor: pointer;
}

.user-trigger :deep(.el-avatar) {
  background: #dbe9ed;
  color: var(--app-primary-dark);
  font-weight: 700;
}

.mobile-menu {
  display: none;
  width: 36px;
  height: 36px;
  border: 0;
  border-radius: 6px;
  background: #eef3f5;
  color: #40525e;
  cursor: pointer;
}

.main-area {
  padding: 28px 0 60px;
}

.layout-grid {
  display: grid;
  grid-template-columns: 220px minmax(0, 1fr);
  gap: 24px;
  align-items: start;
}

.sidebar {
  position: sticky;
  top: 88px;
  padding: 14px 10px 12px;
}

.sidebar-heading {
  padding: 0 10px 10px;
  color: #88949d;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.08em;
}

.category-item {
  position: relative;
  width: 100%;
  min-height: 44px;
  padding: 8px 8px;
  display: grid;
  grid-template-columns: 32px minmax(0, 1fr) 16px;
  align-items: center;
  gap: 8px;
  border: 0;
  border-radius: 6px;
  background: transparent;
  color: #52636f;
  text-align: left;
  cursor: pointer;
  transition:
    background 0.2s ease,
    color 0.2s ease;
}

.category-item:hover,
.category-item.active {
  background: var(--el-color-primary-light-9);
  color: var(--app-primary-dark);
}

.category-item.active {
  font-weight: 700;
}

.category-icon {
  display: grid;
  width: 30px;
  height: 30px;
  place-items: center;
  border-radius: 6px;
  background: #f0f4f6;
}

.category-item.active .category-icon {
  background: #dbe9ed;
}

.category-arrow {
  opacity: 0;
}

.category-item.active .category-arrow {
  opacity: 1;
}

.sidebar-user {
  margin: 16px 6px 0;
  padding: 14px 10px 4px;
  border-top: 1px solid var(--app-border);
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.sidebar-user span {
  color: var(--app-muted);
  font-size: 12px;
}

.sidebar-user strong {
  color: #334650;
  font-size: 14px;
}

.content-column {
  min-width: 0;
}

.mobile-drawer {
  padding: 18px 4px;
}

@media (max-width: 960px) {
  .layout-grid {
    grid-template-columns: 1fr;
  }

  .sidebar {
    display: none;
  }

  .mobile-menu {
    display: grid;
    place-items: center;
  }

  .brand-copy small,
  .topnav span,
  .user-trigger > span {
    display: none;
  }

  .topnav a {
    padding: 8px;
  }
}

@media (max-width: 640px) {
  .topbar-inner {
    width: calc(100% - 20px);
    gap: 8px;
  }

  .brand-copy {
    display: none;
  }

  .search-area {
    min-width: 0;
  }

  .main-area {
    padding-top: 18px;
  }
}
</style>
