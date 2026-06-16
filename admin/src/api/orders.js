import request from './request'

export function createReservation(data) {
  return request.post('/api/reservations', data)
}

export function getMyReservations(params) {
  return request.get('/api/reservations/my', { params })
}

export function getAdminReservations(params) {
  return request.get('/api/admin/orders', { params })
}

export function getAdminReservationDetail(id) {
  return request.get(`/api/admin/orders/${id}`)
}

export function getReservationDetail(id) {
  return request.get(`/api/reservations/${id}`)
}

export function cancelReservation(id) {
  return request.post(`/api/reservations/${id}/cancel`)
}

export function payReservation(id) {
  return request.post(`/api/reservations/${id}/pay`)
}

export function getReservationRules() {
  return request.get('/api/reservations/rules')
}

export function checkin(data) {
  return request.post('/api/checkin', data)
}

export function checkout(data) {
  return request.post('/api/checkout', data)
}

export function getCheckinRecords(params) {
  return request.get('/api/checkin/records', { params })
}

export function getCurrentCheckin() {
  return request.get('/api/checkin/current')
}
