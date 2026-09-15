<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowRight, BookOpen, Sparkles, UserRound } from 'lucide-vue-next'
import { useUserStore } from '@/store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)

const form = reactive({
  username: 'admin',
  password: '123456',
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

async function handleLogin() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await userStore.login(form)
    ElMessage.success('登录成功')
    const redirect =
      typeof route.query.redirect === 'string'
        ? route.query.redirect
        : '/home'
    router.replace(redirect)
  } catch (error) {
    ElMessage.error(error.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <section class="login-brand">
      <div class="brand-badge">
        <Sparkles :size="18" />
        智能图像推荐
      </div>
      <h1>发现值得收藏的图片</h1>
      <p>结合深度学习图像特征、协同过滤与热门趋势，为每位用户生成个性化图片推荐。</p>

      <div class="feature-row">
        <div>
          <BookOpen :size="22" />
          <span>图片浏览</span>
        </div>
        <div>
          <UserRound :size="22" />
          <span>个性化推荐</span>
        </div>
      </div>
    </section>

    <section class="login-panel">
      <div class="login-card">
        <div class="login-title">
          <span class="logo">智</span>
          <div>
            <h2>欢迎回来</h2>
            <p>登录智荐图像推荐平台</p>
          </div>
        </div>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-position="top"
          size="large"
          @submit.prevent="handleLogin"
        >
          <el-form-item label="用户名" prop="username">
            <el-input
              v-model="form.username"
              placeholder="请输入用户名"
              autocomplete="username"
            />
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input
              v-model="form.password"
              type="password"
              show-password
              placeholder="请输入密码"
              autocomplete="current-password"
              @keyup.enter="handleLogin"
            />
          </el-form-item>
          <el-button
            class="login-button"
            type="primary"
            :loading="loading"
            @click="handleLogin"
          >
            登录
            <ArrowRight :size="17" />
          </el-button>
        </el-form>

        <div class="test-account">
          测试账号：<strong>admin</strong> / <strong>123456</strong>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.login-page {
  min-height: 100vh;
  display: grid;
  grid-template-columns: minmax(0, 1.05fr) minmax(420px, 0.95fr);
  background: #edf2f4;
}

.login-brand {
  position: relative;
  padding: 10vh 7vw;
  display: flex;
  flex-direction: column;
  justify-content: center;
  overflow: hidden;
  color: #f4fbfc;
  background:
    linear-gradient(rgba(255, 255, 255, 0.045) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 0.045) 1px, transparent 1px),
    #123b4a;
  background-size: 34px 34px;
}

.login-brand::after {
  content: "";
  position: absolute;
  right: -90px;
  bottom: -110px;
  width: 360px;
  height: 360px;
  border: 56px solid rgba(231, 111, 81, 0.2);
  border-radius: 50%;
}

.brand-badge {
  width: fit-content;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 6px;
  color: #b9dbe3;
  font-size: 13px;
}

.login-brand h1 {
  max-width: 560px;
  margin: 26px 0 16px;
  font-size: clamp(36px, 5vw, 64px);
  line-height: 1.12;
  letter-spacing: 0;
}

.login-brand > p {
  max-width: 560px;
  margin: 0;
  color: #b8ced6;
  font-size: 17px;
  line-height: 1.8;
}

.feature-row {
  position: relative;
  z-index: 1;
  display: flex;
  gap: 30px;
  margin-top: 46px;
}

.feature-row div {
  display: flex;
  align-items: center;
  gap: 9px;
  color: #d8e9ed;
  font-size: 14px;
}

.login-panel {
  display: grid;
  place-items: center;
  padding: 32px;
  background: #f7f9fa;
}

.login-card {
  width: min(420px, 100%);
  padding: 34px;
  border: 1px solid var(--app-border);
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 18px 44px rgba(25, 52, 65, 0.08);
}

.login-title {
  display: flex;
  align-items: center;
  gap: 13px;
  margin-bottom: 28px;
}

.logo {
  display: grid;
  width: 44px;
  height: 44px;
  place-items: center;
  border-radius: 8px;
  background: var(--app-primary);
  color: #fff;
  font-size: 22px;
  font-weight: 800;
}

.login-title h2 {
  margin: 0;
  color: #1b2b35;
  font-size: 23px;
}

.login-title p {
  margin: 4px 0 0;
  color: var(--app-muted);
  font-size: 13px;
}

.login-button {
  width: 100%;
  margin-top: 8px;
}

.test-account {
  margin-top: 20px;
  padding-top: 18px;
  border-top: 1px solid var(--app-border);
  color: #83909a;
  text-align: center;
  font-size: 13px;
}

@media (max-width: 840px) {
  .login-page {
    grid-template-columns: 1fr;
  }

  .login-brand {
    padding: 54px 28px 34px;
  }

  .login-brand h1 {
    font-size: 36px;
  }

  .feature-row {
    margin-top: 26px;
  }

  .login-panel {
    padding: 24px 16px 40px;
  }
}
</style>
