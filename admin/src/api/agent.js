import request from './request'

export function getAgentToolCatalog() {
  return request.get('/api/agent/tools/catalog')
}

export function sendAgentMessage(data) {
  return request.post('/api/agent/chat', data)
}

export function executeAgentTool(data) {
  return request.post('/api/agent/tools/execute', data)
}
