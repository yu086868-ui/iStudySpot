import request from './request'

export function getCharacters() {
  return request.get('/api/characters')
}

export function chat(data) {
  return request.post('/api/chat', data)
}

export function getCustomerServiceWelcome() {
  return request.get('/api/customer-service/welcome')
}

export function customerServiceChat(data) {
  return request.post('/api/customer-service/chat', data)
}

export function getCustomerServiceHistory(sessionId) {
  return request.get('/api/customer-service/history', { params: { sessionId } })
}
