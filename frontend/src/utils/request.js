import axios from 'axios'

export const TOKEN_KEY = 'content_recommend_token'

const request = axios.create({
  baseURL: '/api',
  timeout: 12000,
})

function redirectToLogin() {
  localStorage.removeItem(TOKEN_KEY)
  if (window.location.pathname !== '/login') {
    const redirect = encodeURIComponent(
      `${window.location.pathname}${window.location.search}`,
    )
    window.location.href = `/login?redirect=${redirect}`
  }
}

request.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_KEY)
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

request.interceptors.response.use(
  (response) => {
    const payload = response.data
    if (
      payload &&
      typeof payload === 'object' &&
      Object.prototype.hasOwnProperty.call(payload, 'code')
    ) {
      if (payload.code === 200) {
        return payload.data
      }
      if (payload.code === 401) {
        redirectToLogin()
      }
      return Promise.reject(new Error(payload.msg || '请求失败'))
    }
    return payload
  },
  (error) => {
    if (error.response?.status === 401) {
      redirectToLogin()
    }
    const message =
      error.response?.data?.msg ||
      error.response?.data?.message ||
      error.message ||
      '网络请求失败'
    return Promise.reject(new Error(message))
  },
)

export default request
