import request from './request'

export function getAnnouncementList(params) {
  return request.get('/api/announcements', { params })
}

export function getAnnouncementDetail(id) {
  return request.get(`/api/announcements/${id}`)
}

export function getAdminAnnouncementList(params) {
  return request.get('/api/admin/announcements', { params })
}

export function getAdminAnnouncementDetail(id) {
  return request.get(`/api/admin/announcements/${id}`)
}

export function deleteAdminAnnouncement(id) {
  return request.delete(`/api/admin/announcements/${id}`)
}
