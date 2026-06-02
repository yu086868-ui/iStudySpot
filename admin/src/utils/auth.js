export function getToken() {
  return localStorage.getItem('admin_token')
}

export function setToken(token) {
  localStorage.setItem('admin_token', token)
}

export function removeToken() {
  localStorage.removeItem('admin_token')
  localStorage.removeItem('admin_refresh_token')
  localStorage.removeItem('admin_user')
}

export function getRefreshToken() {
  return localStorage.getItem('admin_refresh_token')
}

export function setRefreshToken(token) {
  localStorage.setItem('admin_refresh_token', token)
}

export function getUser() {
  const user = localStorage.getItem('admin_user')
  return user ? JSON.parse(user) : null
}

export function setUser(user) {
  localStorage.setItem('admin_user', JSON.stringify(user))
}
