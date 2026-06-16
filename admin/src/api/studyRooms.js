import request from './request'

export function getStudyRoomList(params) {
  return request.get('/api/studyrooms', { params })
}

export function getStudyRoomDetail(id) {
  return request.get(`/api/studyrooms/${id}`)
}

export function getStudyRoomStatistics(id, params) {
  return request.get(`/api/studyrooms/${id}/statistics`, { params })
}

export { getStudyRoomGuides, getStudyRoomGuideDetail } from './guides'
