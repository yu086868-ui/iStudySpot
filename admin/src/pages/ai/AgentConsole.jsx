import { useEffect, useMemo, useState } from 'react'
import { Alert, Button, Card, Col, Input, List, message, Row, Select, Space, Tag, Typography } from 'antd'
import { ApiOutlined, SendOutlined, ToolOutlined } from '@ant-design/icons'
import { executeAgentTool, getAgentToolCatalog, sendAgentMessage } from '../../api/agent'

const { Title, Paragraph, Text } = Typography
const { TextArea } = Input

function formatJson(value) {
  if (value === null || value === undefined) return ''
  try {
    return JSON.stringify(value, null, 2)
  } catch {
    return String(value)
  }
}

function parseArguments(raw) {
  if (!raw.trim()) return {}
  return JSON.parse(raw)
}

export default function AgentConsole() {
  const [catalog, setCatalog] = useState([])
  const [catalogLoading, setCatalogLoading] = useState(false)
  const [selectedTool, setSelectedTool] = useState()
  const [toolArgs, setToolArgs] = useState('{}')
  const [toolResult, setToolResult] = useState(null)
  const [toolLoading, setToolLoading] = useState(false)
  const [messageText, setMessageText] = useState('')
  const [sessionId, setSessionId] = useState('')
  const [chatResult, setChatResult] = useState(null)
  const [chatLoading, setChatLoading] = useState(false)

  useEffect(() => {
    loadCatalog()
  }, [])

  const toolOptions = useMemo(
    () => catalog.map((item) => ({
      value: item.name,
      label: item.name,
    })),
    [catalog]
  )

  const currentTool = catalog.find((item) => item.name === selectedTool)

  const loadCatalog = async () => {
    setCatalogLoading(true)
    try {
      const res = await getAgentToolCatalog()
      setCatalog(res.data || [])
      if (!selectedTool && res.data?.length) {
        setSelectedTool(res.data[0].name)
      }
    } catch (err) {
      message.error(err.message || '获取 Agent 工具目录失败')
    } finally {
      setCatalogLoading(false)
    }
  }

  const handleExecuteTool = async () => {
    if (!selectedTool) {
      message.warning('请选择工具')
      return
    }
    let args
    try {
      args = parseArguments(toolArgs)
    } catch {
      message.error('工具参数必须是合法 JSON')
      return
    }

    setToolLoading(true)
    try {
      const res = await executeAgentTool({ tool: selectedTool, arguments: args })
      setToolResult(res.data)
    } catch (err) {
      message.error(err.message || '执行工具失败')
      setToolResult(null)
    } finally {
      setToolLoading(false)
    }
  }

  const handleSend = async () => {
    if (!messageText.trim()) {
      message.warning('请输入消息')
      return
    }
    setChatLoading(true)
    try {
      const res = await sendAgentMessage({
        message: messageText.trim(),
        sessionId: sessionId || undefined,
      })
      setChatResult(res.data)
      if (res.data?.sessionId) {
        setSessionId(res.data.sessionId)
      }
    } catch (err) {
      message.error(err.message || '发送 Agent 消息失败')
      setChatResult(null)
    } finally {
      setChatLoading(false)
    }
  }

  return (
    <div>
      <Title level={4} style={{ marginBottom: 24 }}>Agent 调试台</Title>
      <Row gutter={[16, 16]}>
        <Col xs={24} lg={10}>
          <Card
            title={<Space><ToolOutlined />工具目录</Space>}
            extra={<Button onClick={loadCatalog} loading={catalogLoading}>刷新</Button>}
          >
            <List
              loading={catalogLoading}
              dataSource={catalog}
              rowKey={(item) => item.name}
              renderItem={(item) => (
                <List.Item onClick={() => setSelectedTool(item.name)} style={{ cursor: 'pointer' }}>
                  <List.Item.Meta
                    title={<Space><Text strong>{item.name}</Text>{selectedTool === item.name && <Tag color="blue">当前</Tag>}</Space>}
                    description={item.description || item.summary || '只读工具'}
                  />
                </List.Item>
              )}
            />
          </Card>

          <Card title="工具执行" style={{ marginTop: 16 }}>
            <Space direction="vertical" style={{ width: '100%' }}>
              <Select
                value={selectedTool}
                options={toolOptions}
                onChange={setSelectedTool}
                style={{ width: '100%' }}
                placeholder="选择 Agent 工具"
              />
              {currentTool && (
                <Alert
                  type="info"
                  showIcon
                  message={currentTool.description || currentTool.summary || currentTool.name}
                  description={currentTool.parameters ? <pre style={{ margin: 0 }}>{formatJson(currentTool.parameters)}</pre> : null}
                />
              )}
              <TextArea
                value={toolArgs}
                onChange={(event) => setToolArgs(event.target.value)}
                rows={6}
                placeholder='工具参数 JSON，例如 {"studyRoomId": 1}'
              />
              <Button type="primary" icon={<ApiOutlined />} loading={toolLoading} onClick={handleExecuteTool}>
                执行工具
              </Button>
              {toolResult && (
                <pre style={{ whiteSpace: 'pre-wrap', background: '#f5f5f5', padding: 12, borderRadius: 6 }}>
                  {formatJson(toolResult)}
                </pre>
              )}
            </Space>
          </Card>
        </Col>

        <Col xs={24} lg={14}>
          <Card title={<Space><SendOutlined />Agent 对话</Space>}>
            <Space direction="vertical" style={{ width: '100%' }}>
              <Input
                value={sessionId}
                onChange={(event) => setSessionId(event.target.value)}
                placeholder="会话 ID，可留空由后端生成"
              />
              <TextArea
                value={messageText}
                onChange={(event) => setMessageText(event.target.value)}
                rows={5}
                placeholder="输入要发送给 Agent 的消息"
              />
              <Button type="primary" icon={<SendOutlined />} loading={chatLoading} onClick={handleSend}>
                发送
              </Button>
              {chatResult && (
                <Card size="small" title="回复结果">
                  <Paragraph style={{ whiteSpace: 'pre-wrap' }}>
                    {chatResult.replyText || chatResult.reply || '-'}
                  </Paragraph>
                  <pre style={{ whiteSpace: 'pre-wrap', background: '#f5f5f5', padding: 12, borderRadius: 6 }}>
                    {formatJson(chatResult)}
                  </pre>
                </Card>
              )}
            </Space>
          </Card>
        </Col>
      </Row>
    </div>
  )
}
