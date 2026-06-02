import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { Row, Col, Card, Statistic, Typography, Spin, Tag } from 'antd'
import {
  UserOutlined,
  HomeOutlined,
  UnorderedListOutlined,
  NotificationOutlined,
  CheckCircleOutlined,
  ClockCircleOutlined,
  WarningOutlined,
  HeartOutlined,
} from '@ant-design/icons'
import { getStudyRoomList } from '../api/studyRooms'
import { getMyReservations } from '../api/orders'
import { getHealth } from '../api/health'

const { Title } = Typography

const quickLinks = [
  { title: '自习室管理', desc: '查看和管理所有自习室', icon: <HomeOutlined />, color: '#1677ff', path: '/studyrooms' },
  { title: '订单管理', desc: '查看和处理预约订单', icon: <UnorderedListOutlined />, color: '#52c41a', path: '/orders' },
  { title: '公告管理', desc: '发布和管理公告', icon: <NotificationOutlined />, color: '#faad14', path: '/announcements' },
  { title: '系统健康', desc: '监控系统运行状态', icon: <HeartOutlined />, color: '#f5222d', path: '/system/health' },
]

export default function Dashboard() {
  const [loading, setLoading] = useState(true)
  const [stats, setStats] = useState({
    studyRooms: 0,
    orders: 0,
    healthStatus: '-',
  })
  const navigate = useNavigate()

  useEffect(() => {
    loadStats()
  }, [])

  const loadStats = async () => {
    setLoading(true)
    try {
      const [roomRes, orderRes, healthRes] = await Promise.allSettled([
        getStudyRoomList({ page: 1, pageSize: 1 }),
        getMyReservations({ page: 1, pageSize: 1 }),
        getHealth(),
      ])
      setStats({
        studyRooms: roomRes.status === 'fulfilled' ? (roomRes.value.data?.total || 0) : 0,
        orders: orderRes.status === 'fulfilled' ? (orderRes.value.data?.total || 0) : 0,
        healthStatus: healthRes.status === 'fulfilled' ? (healthRes.value.data?.status || 'unknown') : 'error',
      })
    } finally {
      setLoading(false)
    }
  }

  const isHealthy = stats.healthStatus === 'UP' || stats.healthStatus === 'healthy'
  const isError = stats.healthStatus === 'error' || stats.healthStatus === 'DOWN'

  return (
    <div>
      <Title level={4} style={{ marginBottom: 24 }}>仪表盘</Title>
      <Spin spinning={loading}>
        <Row gutter={[16, 16]}>
          <Col xs={24} sm={12} lg={6}>
            <Card className="stat-card" hoverable onClick={() => navigate('/studyrooms')} style={{ cursor: 'pointer' }}>
              <Statistic
                title="自习室总数"
                value={stats.studyRooms}
                prefix={<HomeOutlined style={{ color: '#1677ff' }} />}
              />
            </Card>
          </Col>
          <Col xs={24} sm={12} lg={6}>
            <Card className="stat-card" hoverable onClick={() => navigate('/orders')} style={{ cursor: 'pointer' }}>
              <Statistic
                title="订单总数"
                value={stats.orders}
                prefix={<UnorderedListOutlined style={{ color: '#52c41a' }} />}
              />
            </Card>
          </Col>
          <Col xs={24} sm={12} lg={6}>
            <Card className="stat-card" hoverable onClick={() => navigate('/system/health')} style={{ cursor: 'pointer' }}>
              <Statistic
                title="系统状态"
                value={isHealthy ? '正常' : isError ? '异常' : stats.healthStatus}
                prefix={
                  isHealthy
                    ? <CheckCircleOutlined style={{ color: '#52c41a' }} />
                    : isError
                      ? <WarningOutlined style={{ color: '#f5222d' }} />
                      : <ClockCircleOutlined style={{ color: '#faad14' }} />
                }
              />
            </Card>
          </Col>
          <Col xs={24} sm={12} lg={6}>
            <Card className="stat-card" hoverable onClick={() => navigate('/users')} style={{ cursor: 'pointer' }}>
              <Statistic
                title="管理后台"
                value="v1.0"
                prefix={<UserOutlined style={{ color: '#722ed1' }} />}
                suffix={<Tag color="blue">在线</Tag>}
              />
            </Card>
          </Col>
        </Row>
      </Spin>

      <Row gutter={[16, 16]} style={{ marginTop: 24 }}>
        <Col span={24}>
          <Card title="快捷入口">
            <Row gutter={[16, 16]}>
              {quickLinks.map((item, i) => (
                <Col xs={24} sm={12} lg={6} key={i}>
                  <Card
                    size="small"
                    hoverable
                    style={{ textAlign: 'center', cursor: 'pointer' }}
                    onClick={() => navigate(item.path)}
                  >
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
