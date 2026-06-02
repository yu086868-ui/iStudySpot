import { useState, useEffect } from 'react'
import { Card, Descriptions, Tag, Button, Spin, Typography, message, Row, Col, Statistic, Space } from 'antd'
import { ArrowLeftOutlined, HomeOutlined } from '@ant-design/icons'
import { useParams, useNavigate } from 'react-router-dom'
import { getStudyRoomDetail, getStudyRoomStatistics } from '../../api/studyRooms'
import { formatDateTime, statusColor } from '../../utils'

const { Title } = Typography

const statusMap = { 1: 'open', 0: 'closed' }

export default function StudyRoomDetail() {
  const { id } = useParams()
  const navigate = useNavigate()
  const [loading, setLoading] = useState(true)
  const [room, setRoom] = useState(null)
  const [stats, setStats] = useState(null)

  useEffect(() => {
    loadData()
  }, [id])

  const loadData = async () => {
    setLoading(true)
    try {
      const res = await getStudyRoomDetail(id)
      setRoom(res.data)
      try {
        const statRes = await getStudyRoomStatistics(id, {})
        setStats(statRes.data)
      } catch {}
    } catch (err) {
      message.error(err.message || '获取自习室详情失败')
    } finally {
      setLoading(false)
    }
  }

  if (loading) return <Spin size="large" style={{ display: 'block', margin: '100px auto' }} />

  return (
    <div>
      <Space style={{ marginBottom: 16 }}>
        <Button icon={<ArrowLeftOutlined />} onClick={() => navigate('/studyrooms')}>返回列表</Button>
      </Space>
      <Title level={4}>自习室详情</Title>
      {room && (
        <>
          <Card style={{ marginBottom: 16 }}>
            <Descriptions bordered column={{ xs: 1, sm: 2, lg: 3 }}>
              <Descriptions.Item label="ID">{room.id}</Descriptions.Item>
              <Descriptions.Item label="名称">{room.name}</Descriptions.Item>
              <Descriptions.Item label="地址">{room.address || '-'}</Descriptions.Item>
              <Descriptions.Item label="经度">{room.latitude || '-'}</Descriptions.Item>
              <Descriptions.Item label="纬度">{room.longitude || '-'}</Descriptions.Item>
              <Descriptions.Item label="开放时间">{room.openTime || '-'} ~ {room.closeTime || '-'}</Descriptions.Item>
              <Descriptions.Item label="状态">
                <Tag color={statusColor(statusMap[room.status] || 'closed')}>
                  {statusMap[room.status] === 'open' ? '开放' : '关闭'}
                </Tag>
              </Descriptions.Item>
              <Descriptions.Item label="描述" span={3}>{room.description || '-'}</Descriptions.Item>
              <Descriptions.Item label="规则" span={3}>{room.rules || '-'}</Descriptions.Item>
              <Descriptions.Item label="图片">{room.imageUrl ? <a href={room.imageUrl} target="_blank" rel="noreferrer">查看</a> : '-'}</Descriptions.Item>
              <Descriptions.Item label="创建时间">{formatDateTime(room.createTime)}</Descriptions.Item>
              <Descriptions.Item label="更新时间">{formatDateTime(room.updateTime)}</Descriptions.Item>
            </Descriptions>
          </Card>
          {stats && (
            <Card title="统计数据">
              <Row gutter={[16, 16]}>
                <Col xs={12} sm={6}>
                  <Statistic title="总预约数" value={stats.totalBookings || 0} />
                </Col>
                <Col xs={12} sm={6}>
                  <Statistic title="总使用时长" value={stats.totalHours || 0} suffix="小时" />
                </Col>
                <Col xs={12} sm={6}>
                  <Statistic title="平均上座率" value={stats.averageOccupancy || 0} suffix="%" precision={1} />
                </Col>
              </Row>
            </Card>
          )}
        </>
      )}
    </div>
  )
}

