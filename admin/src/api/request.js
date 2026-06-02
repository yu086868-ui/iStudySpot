import axios from 'axios'
import { getToken, getRefreshToken, setToken, removeToken } from '../utils/auth'

const request = axios.create({
  baseURL: '',
  timeout: 30000,
  headers: { 'Content-Type': 'application/json' },
})

request.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

request.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code === 401) {
      removeToken()
      window.location.href = '/login'
      return Promise.reject(new Error(res.message || '未登录'))
    }
    if (res.code && res.code !== 200 && res.code !== 201) {
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res
  },
  async (error) => {
    if (error.response && error.response.status === 401) {
      const refreshToken = getRefreshToken()
      if (refreshToken) {
        try {
          const res = await axios.post('/api/auth/refresh', { refreshToken })
          if (res.data && res.data.code === 200) {
            setToken(res.data.data.token)
            error.config.headers.Authorization = `Bearer ${res.data.data.token}`
            return request(error.config)
          }
        } catch {
          removeToken()
          window.location.href = '/login'
        }
      } else {
        removeToken()
        window.location.href = '/login'
      }
    }
    return Promise.reject(error)
  }
)

export default request
