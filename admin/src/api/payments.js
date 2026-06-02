import request from './request'

export function createPayment(data) {
  return request.post('/api/payments', data)
}

export function getPaymentStatus(id) {
  return request.get(`/api/payments/${id}`)
}

export function paymentCallback(data) {
  return request.post('/api/payments/callback', data)
}
