import { createRouter, createWebHistory } from 'vue-router'
import { TOKEN_KEY } from '@/utils/request'

const routes = [
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/Login.vue'),
    meta: { public: true, title: '登录' },
  },
  {
    path: '/',
    component: () => import('@/components/AppLayout.vue'),
    redirect: '/home',
    children: [
      {
        path: 'home',
        name: 'home',
        component: () => import('@/views/Home.vue'),
        meta: { title: '发现' },
      },
      {
        path: 'image/:id',
        name: 'image-detail',
        component: () => import('@/views/ImageDetail.vue'),
        meta: { title: '图片详情' },
      },
      {
        path: 'notifications',
        name: 'notifications',
        component: () => import('@/views/Notification.vue'),
        meta: { title: '通知' },
      },
      {
        path: 'upload',
        name: 'upload',
        component: () => import('@/views/Upload.vue'),
        meta: { title: '上传图片' },
      },
    ],
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/home',
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 }),
})

router.beforeEach((to) => {
  const token = localStorage.getItem(TOKEN_KEY)
  if (to.meta.public) {
    return token && to.name === 'login' ? { name: 'home' } : true
  }
  if (!token) {
    return {
      name: 'login',
      query: { redirect: to.fullPath },
    }
  }
  return true
})

router.afterEach((to) => {
  document.title = to.meta.title
    ? `${to.meta.title} · 智荐`
    : '智荐 · 图像推荐平台'
})

export default router
