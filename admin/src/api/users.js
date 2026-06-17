import request from './request'

export function getUserInfo() {
  return request.get('/api/users/me')
}

export function getAdminUsers(params) {
  return request.get('/api/admin/users', { params })
}

export function updateUserInfo(data) {
  return request.put('/api/users/me', data)
}

export function updatePassword(data) {
  return request.put('/api/users/me/password', data)
}
