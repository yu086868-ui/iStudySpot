import request from './request'

export function getStudyRoomGuides() {
  return request.get('/api/studyrooms/guides')
}

export function getStudyRoomGuideDetail(studyRoomId) {
  return request.get(`/api/studyrooms/guides/${studyRoomId}`)
}
