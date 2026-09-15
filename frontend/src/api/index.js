import request from '@/utils/request'

export const loginApi = (data) => request.post('/user/login', data)

export const getUserInfoApi = () => request.get('/user/info')

export const getRecommendationsApi = (params) =>
  request.get('/recommend/get', { params })

export const getContentRecommendationsApi = (params) =>
  request.get('/recommend/content', { params })

export const reportBehaviorApi = (data) =>
  request.post('/recommend/behavior', data)

export const getImageListApi = (params) =>
  request.get('/image/list', { params })

export const getImageDetailApi = (id) =>
  request.get(`/image/${id}`)

export const likeImageApi = (imageId) =>
  request.post(`/image/like/${imageId}`)

export const collectImageApi = (imageId) =>
  request.post(`/image/collect/${imageId}`)

export const downloadImageApi = (imageId) =>
  request.post(`/image/download/${imageId}`)

export const uploadImageApi = (data) =>
  request.post('/image/upload', data)

export const getCategoriesApi = () =>
  request.get('/image/category/list')

export const getCommentsApi = (params) =>
  request.get('/image/comment/list', { params })

export const addCommentApi = (params) =>
  request.post('/image/comment/add', null, { params })

export const getNotificationsApi = (params) =>
  request.get('/notification/list', { params })

export const getUnreadCountApi = () =>
  request.get('/notification/unread-count')

export const markNotificationReadApi = (id) =>
  request.put(`/notification/read/${id}`)

export const markAllNotificationsReadApi = () =>
  request.put('/notification/read-all')

export const deleteNotificationApi = (id) =>
  request.delete(`/notification/${id}`)

export const getSimilarImagesApi = (imageId, params) =>
  request.get(`/recommend/content/${imageId}`, { params })

export const predictImageApi = (data) =>
  request.post('/recommend/predict', data, { headers: { 'Content-Type': 'multipart/form-data' } })
