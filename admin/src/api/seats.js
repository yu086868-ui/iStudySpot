import request from './request'

export function getSeatList(studyRoomId, params) {
  return request.get(`/api/studyrooms/${studyRoomId}/seats`, { params })
}

export function getSeatDetail(id) {
  return request.get(`/api/seats/${id}`)
}

export function getSeatLayout(studyRoomId) {
  return request.get(`/api/studyrooms/${studyRoomId}/seat-layout`)
}
