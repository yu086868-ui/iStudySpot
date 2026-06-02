import axios from 'axios'
import { getToken } from '../utils/auth'

const cardRequest = axios.create({
  baseURL: '',
  timeout: 30000,
  headers: { 'Content-Type': 'application/json' },
})

cardRequest.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

cardRequest.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.success === false) {
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    if (res.code === 401) {
      return Promise.reject(new Error('未登录'))
    }
    if (res.code && res.code !== 200 && res.code !== 201) {
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res
  },
  (error) => Promise.reject(error)
)

export function generateCard(data) {
  return cardRequest.post('/api/card/generate', data)
}

export function getCardDetail(uuid) {
  return cardRequest.get('/api/card/detail', { params: { id: uuid } })
}

export function getCardList(userId) {
  return cardRequest.get('/api/card/list', { params: { userID: userId } })
}
