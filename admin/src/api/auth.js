import request from './request'

export function login(data) {
  return request.post('/api/auth/login', data)
}

export function register(data) {
  return request.post('/api/auth/register', data)
}

export function refreshToken(refreshToken) {
  return request.post('/api/auth/refresh', { refreshToken })
}

export function logout() {
  return request.post('/api/auth/logout')
}
