import request from './request'

export function getUserInfo() {
  return request.get('/api/users/me')
}

export function updateUserInfo(data) {
  return request.put('/api/users/me', data)
}

export function updatePassword(data) {
  return request.put('/api/users/me/password', data)
}
