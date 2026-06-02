import request from './request'

export function getHealth() {
  return request.get('/health')
}

export function getReadiness() {
  return request.get('/health/ready')
}
