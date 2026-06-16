import { useEffect, useRef, useState } from 'react'
import { Avatar, Button, Card, Input, Select, Space, Spin, Typography, message } from 'antd'
import { RobotOutlined, SendOutlined, UserOutlined } from '@ant-design/icons'
import { chat, customerServiceChat, getCharacters } from '../../api/ai'

const { Title } = Typography
const { TextArea } = Input

export default function AIChatMonitor() {
  const [characters, setCharacters] = useState([])
  const [selectedChar, setSelectedChar] = useState(null)
  const [messages, setMessages] = useState([])
  const [input, setInput] = useState('')
  const [sending, setSending] = useState(false)
  const [mode, setMode] = useState('ai')
  const [sessionId, setSessionId] = useState('')
  const messagesEndRef = useRef(null)

  useEffect(() => {
    loadCharacters()
  }, [])

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages])

  const loadCharacters = async () => {
    try {
      const res = await getCharacters()
      const next = res.data || []
      setCharacters(next)
      if (!selectedChar && next.length > 0) {
        setSelectedChar(next[0].id)
      }
    } catch (err) {
      message.error(err.message || '获取 AI 角色失败')
    }
  }

  const nextSessionId = sessionId || `admin-${Date.now()}`

  const handleSend = async () => {
    if (!input.trim()) return

    const content = input.trim()
    setMessages((prev) => [...prev, { role: 'user', content }])
    setInput('')
    setSending(true)

    try {
      let res
      if (mode === 'ai') {
        if (!selectedChar) {
          message.warning('请选择 AI 角色')
          setSending(false)
          return
        }
        res = await chat({
          sessionId: nextSessionId,
          characterId: selectedChar,
          message: content,
        })
      } else {
        res = await customerServiceChat({
          sessionId: nextSessionId,
          message: content,
        })
      }

      setSessionId(nextSessionId)
      const reply = res.data?.reply || res.data?.response || '无回复'
      setMessages((prev) => [...prev, { role: 'assistant', content: reply }])
    } catch (err) {
      message.error(err.message || '发送失败')
      setMessages((prev) => [...prev, { role: 'assistant', content: `错误: ${err.message}` }])
    } finally {
      setSending(false)
    }
  }

  const handleKeyDown = (event) => {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault()
      handleSend()
    }
  }

  return (
    <div>
      <Title level={4} style={{ marginBottom: 24 }}>AI 对话调试</Title>
      <Card>
        <Space style={{ marginBottom: 16 }} wrap>
          <Select
            value={mode}
            onChange={(value) => {
              setMode(value)
              setMessages([])
              setSessionId('')
            }}
            style={{ width: 160 }}
            options={[
              { label: 'AI 角色对话', value: 'ai' },
              { label: '智能客服对话', value: 'cs' },
            ]}
          />
          {mode === 'ai' && (
            <Select
              placeholder="选择 AI 角色"
              value={selectedChar}
              onChange={setSelectedChar}
              style={{ width: 180 }}
              options={characters.map((item) => ({ label: item.name, value: item.id }))}
            />
          )}
          <Input
            value={sessionId}
            onChange={(event) => setSessionId(event.target.value)}
            placeholder="会话 ID，可留空"
            style={{ width: 220 }}
          />
          <Button onClick={() => setMessages([])}>清空对话</Button>
        </Space>

        <div
          style={{
            height: 420,
            overflow: 'auto',
            border: '1px solid #f0f0f0',
            borderRadius: 8,
            padding: 16,
            marginBottom: 16,
            background: '#fafafa',
          }}
        >
          {messages.length === 0 && (
            <div style={{ textAlign: 'center', color: '#999', padding: '100px 0' }}>
              选择模式后即可开始调试对话
            </div>
          )}
          {messages.map((msg, index) => (
            <div
              key={`${msg.role}-${index}`}
              style={{
                display: 'flex',
                justifyContent: msg.role === 'user' ? 'flex-end' : 'flex-start',
                marginBottom: 12,
              }}
            >
              <div
                style={{
                  maxWidth: '72%',
                  display: 'flex',
                  gap: 8,
                  flexDirection: msg.role === 'user' ? 'row-reverse' : 'row',
                }}
              >
                <Avatar
                  icon={msg.role === 'user' ? <UserOutlined /> : <RobotOutlined />}
                  style={{ background: msg.role === 'user' ? '#1677ff' : '#52c41a', flexShrink: 0 }}
                />
                <div
                  style={{
                    padding: '8px 12px',
                    borderRadius: 8,
                    background: msg.role === 'user' ? '#1677ff' : '#fff',
                    color: msg.role === 'user' ? '#fff' : '#333',
                    border: msg.role === 'user' ? 'none' : '1px solid #e8e8e8',
                    whiteSpace: 'pre-wrap',
                    wordBreak: 'break-word',
                  }}
                >
                  {msg.content}
                </div>
              </div>
            </div>
          ))}
          {sending && (
            <div style={{ display: 'flex', gap: 8, marginBottom: 12 }}>
              <Avatar icon={<RobotOutlined />} style={{ background: '#52c41a' }} />
              <Spin size="small" style={{ marginTop: 8 }} />
            </div>
          )}
          <div ref={messagesEndRef} />
        </div>

        <Space.Compact style={{ width: '100%' }}>
          <TextArea
            value={input}
            onChange={(event) => setInput(event.target.value)}
            onKeyDown={handleKeyDown}
            placeholder="输入消息，Enter 发送，Shift+Enter 换行"
            autoSize={{ minRows: 1, maxRows: 4 }}
            style={{ flex: 1 }}
          />
          <Button type="primary" icon={<SendOutlined />} onClick={handleSend} loading={sending} style={{ height: 'auto' }}>
            发送
          </Button>
        </Space.Compact>
      </Card>
    </div>
  )
}
