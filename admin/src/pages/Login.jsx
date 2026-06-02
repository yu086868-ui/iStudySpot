import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Form, Input, Button, Card, message, Divider } from 'antd'
import { UserOutlined, LockOutlined, ThunderboltOutlined } from '@ant-design/icons'
import { login } from '../api/auth'
import { setToken, setRefreshToken, setUser } from '../utils/auth'

const DEFAULT_USERNAME = 'admin'
const DEFAULT_PASSWORD = 'admin@123456'

export default function Login() {
  const [loading, setLoading] = useState(false)
  const navigate = useNavigate()
  const [form] = Form.useForm()

  const handleSubmit = async (values) => {
    setLoading(true)
    try {
      const res = await login(values)
      const { token, refreshToken, user } = res.data
      setToken(token)
      setRefreshToken(refreshToken)
      setUser(user)
      message.success('登录成功')
      navigate('/dashboard', { replace: true })
    } catch (err) {
      message.error(err.message || '登录失败')
    } finally {
      setLoading(false)
    }
  }

  const handleQuickLogin = async () => {
    setLoading(true)
    try {
      const res = await login({ username: DEFAULT_USERNAME, password: DEFAULT_PASSWORD })
      const { token, refreshToken, user } = res.data
      setToken(token)
      setRefreshToken(refreshToken)
      setUser(user)
      message.success('管理员登录成功')
      navigate('/dashboard', { replace: true })
    } catch (err) {
      message.error(err.message || '管理员登录失败，请检查后端是否已创建 admin 用户')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="login-container">
      <div className="login-card" style={{ background: '#fff' }}>
        <div className="login-title">
          <h1>iStudySpot</h1>
          <p>管理后台</p>
        </div>
        <Form
          form={form}
          onFinish={handleSubmit}
          size="large"
          autoComplete="off"
          initialValues={{ username: DEFAULT_USERNAME, password: DEFAULT_PASSWORD }}
        >
          <Form.Item name="username" rules={[{ required: true, message: '请输入用户名' }]}>
            <Input prefix={<UserOutlined />} placeholder="用户名" />
          </Form.Item>
          <Form.Item name="password" rules={[{ required: true, message: '请输入密码' }]}>
            <Input.Password prefix={<LockOutlined />} placeholder="密码" />
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit" loading={loading} block>
              登录
            </Button>
          </Form.Item>
        </Form>
        <Divider style={{ color: '#bbb', fontSize: 12 }}>快捷操作</Divider>
        <Button
          icon={<ThunderboltOutlined />}
          loading={loading}
          block
          onClick={handleQuickLogin}
          style={{ marginTop: -8 }}
        >
          管理员快捷登录 (admin)
        </Button>
      </div>
    </div>
  )
}
