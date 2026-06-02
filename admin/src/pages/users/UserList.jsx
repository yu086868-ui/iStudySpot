import { useState, useEffect } from 'react'
import { Card, Tag, Input, Button, Modal, Descriptions, Space, message, Typography } from 'antd'
import { EditOutlined, KeyOutlined } from '@ant-design/icons'
import { getUserInfo, updateUserInfo, updatePassword } from '../../api/users'
import { formatDateTime } from '../../utils'

const { Title } = Typography

export default function UserList() {
  const [loading, setLoading] = useState(false)
  const [user, setUser] = useState(null)
  const [editVisible, setEditVisible] = useState(false)
  const [editForm, setEditForm] = useState({})
  const [passwordVisible, setPasswordVisible] = useState(false)
  const [passwordForm, setPasswordForm] = useState({ oldPassword: '', newPassword: '' })

  useEffect(() => {
    loadUser()
  }, [])

  const loadUser = async () => {
    setLoading(true)
    try {
      const res = await getUserInfo()
      setUser(res.data)
    } catch (err) {
      message.error(err.message || '获取用户信息失败')
    } finally {
      setLoading(false)
    }
  }

  const handleUpdateUser = async () => {
    try {
      await updateUserInfo(editForm)
      message.success('更新成功')
      setEditVisible(false)
      loadUser()
    } catch (err) {
      message.error(err.message || '更新失败')
    }
  }

  const handleUpdatePassword = async () => {
    if (!passwordForm.oldPassword || !passwordForm.newPassword) {
      message.warning('请填写完整')
      return
    }
    try {
      await updatePassword(passwordForm)
      message.success('密码修改成功')
      setPasswordVisible(false)
      setPasswordForm({ oldPassword: '', newPassword: '' })
    } catch (err) {
      message.error(err.message || '密码修改失败')
    }
  }

  const userStatus = user?.status
  const isActive = userStatus === 1 || userStatus === '1'

  return (
    <div>
      <Title level={4} style={{ marginBottom: 24 }}>用户管理</Title>
      <Card loading={loading}>
        {user && (
          <div>
            <Descriptions
              title="当前用户信息"
              bordered
              column={{ xs: 1, sm: 2, lg: 3 }}
              extra={
                <Space>
                  <Button type="primary" icon={<EditOutlined />} onClick={() => { setEditForm({ nickname: user.nickname, phone: user.phone, email: user.email }); setEditVisible(true) }}>
                    编辑信息
                  </Button>
                  <Button icon={<KeyOutlined />} onClick={() => setPasswordVisible(true)}>修改密码</Button>
                </Space>
              }
            >
              <Descriptions.Item label="ID">{user.id}</Descriptions.Item>
              <Descriptions.Item label="用户名">{user.username}</Descriptions.Item>
              <Descriptions.Item label="昵称">{user.nickname || '-'}</Descriptions.Item>
              <Descriptions.Item label="手机号">{user.phone || '-'}</Descriptions.Item>
              <Descriptions.Item label="邮箱">{user.email || '-'}</Descriptions.Item>
              <Descriptions.Item label="学号">{user.studentId || '-'}</Descriptions.Item>
              <Descriptions.Item label="信用分">
                <Tag color={(user.creditScore ?? 100) >= 80 ? 'green' : (user.creditScore ?? 100) >= 60 ? 'orange' : 'red'}>
                  {user.creditScore ?? 100}
                </Tag>
              </Descriptions.Item>
              <Descriptions.Item label="余额">¥{user.balance || 0}</Descriptions.Item>
              <Descriptions.Item label="积分">{user.points || 0}</Descriptions.Item>
              <Descriptions.Item label="状态">
                <Tag color={isActive ? 'green' : 'red'}>
                  {isActive ? '正常' : '封禁'}
                </Tag>
              </Descriptions.Item>
              <Descriptions.Item label="违规次数">{user.violationCount ?? 0}</Descriptions.Item>
              <Descriptions.Item label="最后登录">{formatDateTime(user.lastLoginTime)}</Descriptions.Item>
            </Descriptions>

            <Modal title="编辑用户信息" open={editVisible} onOk={handleUpdateUser} onCancel={() => setEditVisible(false)}>
              <div style={{ display: 'flex', flexDirection: 'column', gap: 12, padding: '16px 0' }}>
                <Input placeholder="昵称" value={editForm.nickname} onChange={e => setEditForm({ ...editForm, nickname: e.target.value })} />
                <Input placeholder="手机号" value={editForm.phone} onChange={e => setEditForm({ ...editForm, phone: e.target.value })} />
                <Input placeholder="邮箱" value={editForm.email} onChange={e => setEditForm({ ...editForm, email: e.target.value })} />
              </div>
            </Modal>

            <Modal title="修改密码" open={passwordVisible} onOk={handleUpdatePassword} onCancel={() => setPasswordVisible(false)}>
              <div style={{ display: 'flex', flexDirection: 'column', gap: 12, padding: '16px 0' }}>
                <Input.Password placeholder="旧密码" value={passwordForm.oldPassword} onChange={e => setPasswordForm({ ...passwordForm, oldPassword: e.target.value })} />
                <Input.Password placeholder="新密码" value={passwordForm.newPassword} onChange={e => setPasswordForm({ ...passwordForm, newPassword: e.target.value })} />
              </div>
            </Modal>
          </div>
        )}
      </Card>
    </div>
  )
}
