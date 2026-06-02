import request from './request'

export function getAnnouncementList(params) {
  return request.get('/api/announcements', { params })
}

export function getAnnouncementDetail(id) {
  return request.get(`/api/announcements/${id}`)
}
