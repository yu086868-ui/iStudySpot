import { useState, useEffect, useRef } from 'react'
import { Card, Typography, Input, Button, Space, List, Avatar, Tag, message, Select, Spin } from 'antd'
import { RobotOutlined, UserOutlined, SendOutlined } from '@ant-design/icons'
import { getCharacters, chat, getCustomerServiceWelcome, customerServiceChat, getCustomerServiceHistory } from '../../api/ai'

const { Title } = Typography
const { TextArea } = Input

export default function AIChatMonitor() {
  const [characters, setCharacters] = useState([])
  const [selectedChar, setSelectedChar] = useState(null)
  const [messages, setMessages] = useState([])
  const [input, setInput] = useState('')
  const [sending, setSending] = useState(false)
  const [mode, setMode] = useState('ai')
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
      setCharacters(res.data || [])
    } catch {}
  }

  const handleSend = async () => {
    if (!input.trim()) return
    const userMsg = { role: 'user', content: input }
    setMessages(prev => [...prev, userMsg])
    setInput('')
    setSending(true)
    try {
      let res
      if (mode === 'ai') {
        if (!selectedChar) {
          message.warning('请选择AI角色')
          setSending(false)
          return
        }
        res = await chat({
          session_id: `admin-${Date.now()}`,
          character_id: selectedChar,
          message: input,
        })
      } else {
        res = await customerServiceChat({
          sessionId: `admin-cs-${Date.now()}`,
          message: input,
        })
      }
      const reply = res.data?.reply || res.data?.response || '（无回复）'
      setMessages(prev => [...prev, { role: 'assistant', content: reply }])
    } catch (err) {
      message.error(err.message || '发送失败')
      setMessages(prev => [...prev, { role: 'assistant', content: `错误: ${err.message}` }])
    } finally {
      setSending(false)
    }
  }

  const handleKeyDown = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault()
      handleSend()
    }
  }

  return (
    <div>
      <Title level={4} style={{ marginBottom: 24 }}>AI 聊天监控</Title>
      <Card>
        <Space style={{ marginBottom: 16 }} wrap>
          <Select
            value={mode}
            onChange={v => { setMode(v); setMessages([]) }}
            style={{ width: 140 }}
            options={[
              { label: 'AI 角色聊天', value: 'ai' },
              { label: '智能客服', value: 'cs' },
            ]}
          />
          {mode === 'ai' && (
            <Select
              placeholder="选择AI角色"
              value={selectedChar}
              onChange={setSelectedChar}
              style={{ width: 160 }}
              options={characters.map(c => ({ label: c.name, value: c.id }))}
            />
          )}
          <Button onClick={() => setMessages([])}>清空对话</Button>
        </Space>

        <div style={{
          height: 400,
          overflow: 'auto',
          border: '1px solid #f0f0f0',
          borderRadius: 8,
          padding: 16,
          marginBottom: 16,
          background: '#fafafa',
        }}>
          {messages.length === 0 && (
            <div style={{ textAlign: 'center', color: '#999', padding: '100px 0' }}>
              选择模式后发送消息开始测试
            </div>
          )}
          {messages.map((msg, i) => (
            <div key={i} style={{
              display: 'flex',
              justifyContent: msg.role === 'user' ? 'flex-end' : 'flex-start',
              marginBottom: 12,
            }}>
              <div style={{
                maxWidth: '70%',
                display: 'flex',
                gap: 8,
                flexDirection: msg.role === 'user' ? 'row-reverse' : 'row',
              }}>
                <Avatar
                  icon={msg.role === 'user' ? <UserOutlined /> : <RobotOutlined />}
                  style={{ background: msg.role === 'user' ? '#1677ff' : '#52c41a', flexShrink: 0 }}
                />
                <div style={{
                  padding: '8px 12px',
                  borderRadius: 8,
                  background: msg.role === 'user' ? '#1677ff' : '#fff',
                  color: msg.role === 'user' ? '#fff' : '#333',
                  border: msg.role === 'user' ? 'none' : '1px solid #e8e8e8',
                  whiteSpace: 'pre-wrap',
                  wordBreak: 'break-word',
                }}>
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
            onChange={e => setInput(e.target.value)}
            onKeyDown={handleKeyDown}
            placeholder="输入消息... (Enter 发送, Shift+Enter 换行)"
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
