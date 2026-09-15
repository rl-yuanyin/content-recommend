import { defineStore } from 'pinia'
import {
  getCategoriesApi,
  getUnreadCountApi,
  getUserInfoApi,
  loginApi,
} from '@/api'
import { TOKEN_KEY } from '@/utils/request'

function readStoredUser() {
  try {
    return JSON.parse(localStorage.getItem('content_recommend_user') || 'null')
  } catch {
    return null
  }
}

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem(TOKEN_KEY) || '',
    user: readStoredUser(),
    unreadCount: 0,
    categories: [],
  }),

  getters: {
    isLoggedIn: (state) => Boolean(state.token),
    displayName: (state) =>
      state.user?.nickname || state.user?.username || '用户',
  },

  actions: {
    setToken(token) {
      this.token = token || ''
      if (token) {
        localStorage.setItem(TOKEN_KEY, token)
      } else {
        localStorage.removeItem(TOKEN_KEY)
      }
    },

    setUser(user) {
      this.user = user
      if (user) {
        localStorage.setItem('content_recommend_user', JSON.stringify(user))
      } else {
        localStorage.removeItem('content_recommend_user')
      }
    },

    async login(credentials) {
      const token = await loginApi(credentials)
      if (!token) {
        throw new Error('登录失败，请检查用户名和密码')
      }
      this.setToken(token)
      await Promise.allSettled([this.fetchUserInfo(), this.fetchUnreadCount()])
      return token
    },

    async fetchUserInfo() {
      if (!this.token) return null
      const user = await getUserInfoApi()
      this.setUser(user)
      return user
    },

    async fetchUnreadCount() {
      if (!this.token) {
        this.unreadCount = 0
        return 0
      }
      const count = await getUnreadCountApi()
      this.unreadCount = Number(count || 0)
      return this.unreadCount
    },

    async fetchCategories() {
      if (this.categories.length) return this.categories
      const categories = await getCategoriesApi()
      this.categories = categories || []
      return this.categories
    },

    logout() {
      this.setToken('')
      this.setUser(null)
      this.unreadCount = 0
      this.categories = []
    },
  },
})
