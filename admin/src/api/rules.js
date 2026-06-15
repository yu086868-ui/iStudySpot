import request from './request'

export function getRuleList(params) {
  return request.get('/api/rules', { params })
}

export function getRuleDetail(id) {
  return request.get(`/api/rules/${id}`)
}
