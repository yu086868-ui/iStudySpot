import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Card, Col, Row, Spin, Statistic, Tag, Typography } from 'antd'
import {
  CheckCircleOutlined,
  HeartOutlined,
  HomeOutlined,
  NotificationOutlined,
  UnorderedListOutlined,
  UserOutlined,
} from '@ant-design/icons'
import { getAdminAnnouncementList } from '../api/announcements'
import { getHealth } from '../api/health'
import { getAdminReservations } from '../api/orders'
import { getStudyRoomList } from '../api/studyRooms'
import { getAdminUsers } from '../api/users'

const { Title } = Typography

const quickLinks = [
  { title: '用户管理', desc: '查看系统已有用户', icon: <UserOutlined />, color: '#1677ff', path: '/users' },
  { title: '自习室', desc: '查看自习室和座位信息', icon: <HomeOutlined />, color: '#13c2c2', path: '/studyrooms' },
  { title: '订单管理', desc: '查看真实预约订单', icon: <UnorderedListOutlined />, color: '#52c41a', path: '/orders' },
  { title: '公告管理', desc: '查看系统公告内容', icon: <NotificationOutlined />, color: '#fa8c16', path: '/announcements' },
  { title: '系统健康', desc: '查看后端运行状态', icon: <HeartOutlined />, color: '#f5222d', path: '/system/health' },
]

export default function Dashboard() {
  const [loading, setLoading] = useState(true)
  const [stats, setStats] = useState({
    users: 0,
    studyRooms: 0,
    orders: 0,
    announcements: 0,
    healthStatus: '-',
  })
  const navigate = useNavigate()

  useEffect(() => {
    loadStats()
  }, [])

  const loadStats = async () => {
    setLoading(true)
    try {
      const [userRes, roomRes, orderRes, announcementRes, healthRes] = await Promise.allSettled([
        getAdminUsers({ page: 1, pageSize: 1 }),
        getStudyRoomList({ page: 1, pageSize: 1 }),
        getAdminReservations({ page: 1, pageSize: 1 }),
        getAdminAnnouncementList({ page: 1, pageSize: 1 }),
        getHealth(),
      ])

      setStats({
        users: userRes.status === 'fulfilled' ? userRes.value.data?.total || 0 : 0,
        studyRooms: roomRes.status === 'fulfilled' ? roomRes.value.data?.total || 0 : 0,
        orders: orderRes.status === 'fulfilled' ? orderRes.value.data?.total || 0 : 0,
        announcements: announcementRes.status === 'fulfilled' ? announcementRes.value.data?.total || 0 : 0,
        healthStatus: healthRes.status === 'fulfilled' ? healthRes.value.data?.status || 'unknown' : 'error',
      })
    } finally {
      setLoading(false)
    }
  }

  const isHealthy = ['UP', 'healthy', 'ready'].includes(stats.healthStatus)

  return (
    <div>
      <Title level={4} style={{ marginBottom: 24 }}>仪表盘</Title>
      <Spin spinning={loading}>
        <Row gutter={[16, 16]}>
          <Col xs={24} sm={12} lg={6}>
            <Card hoverable onClick={() => navigate('/users')} style={{ cursor: 'pointer' }}>
              <Statistic title="用户总数" value={stats.users} prefix={<UserOutlined style={{ color: '#1677ff' }} />} />
            </Card>
          </Col>
          <Col xs={24} sm={12} lg={6}>
            <Card hoverable onClick={() => navigate('/studyrooms')} style={{ cursor: 'pointer' }}>
              <Statistic title="自习室总数" value={stats.studyRooms} prefix={<HomeOutlined style={{ color: '#13c2c2' }} />} />
            </Card>
          </Col>
          <Col xs={24} sm={12} lg={6}>
            <Card hoverable onClick={() => navigate('/orders')} style={{ cursor: 'pointer' }}>
              <Statistic title="预约订单" value={stats.orders} prefix={<UnorderedListOutlined style={{ color: '#52c41a' }} />} />
            </Card>
          </Col>
          <Col xs={24} sm={12} lg={6}>
            <Card hoverable onClick={() => navigate('/announcements')} style={{ cursor: 'pointer' }}>
              <Statistic title="系统公告" value={stats.announcements} prefix={<NotificationOutlined style={{ color: '#fa8c16' }} />} />
            </Card>
          </Col>
        </Row>

        <Row gutter={[16, 16]} style={{ marginTop: 16 }}>
          <Col xs={24} sm={12} lg={6}>
            <Card hoverable onClick={() => navigate('/system/health')} style={{ cursor: 'pointer' }}>
              <Statistic
                title="系统状态"
                value={isHealthy ? '正常' : stats.healthStatus}
                prefix={<CheckCircleOutlined style={{ color: isHealthy ? '#52c41a' : '#faad14' }} />}
                suffix={<Tag color={isHealthy ? 'green' : 'orange'}>{stats.healthStatus}</Tag>}
              />
            </Card>
          </Col>
        </Row>
      </Spin>

      <Row gutter={[16, 16]} style={{ marginTop: 24 }}>
        <Col span={24}>
          <Card title="快捷入口">
            <Row gutter={[16, 16]}>
              {quickLinks.map((item) => (
                <Col xs={24} sm={12} lg={6} key={item.path}>
                  <Card size="small" hoverable style={{ textAlign: 'center', cursor: 'pointer' }} onClick={() => navigate(item.path)}>
                    <div style={{ fontSize: 32, color: item.color, marginBottom: 8 }}>{item.icon}</div>
                    <div style={{ fontWeight: 'bold', marginBottom: 4 }}>{item.title}</div>
                    <div style={{ color: '#999', fontSize: 12 }}>{item.desc}</div>
                  </Card>
                </Col>
              ))}
            </Row>
          </Card>
        </Col>
      </Row>
    </div>
  )
}
