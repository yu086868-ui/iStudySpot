import { useState, useEffect } from 'react'
import { Table, Card, Tag, Button, Typography, message, Select, Space } from 'antd'
import { ArrowLeftOutlined } from '@ant-design/icons'
import { useParams, useNavigate } from 'react-router-dom'
import { getSeatList } from '../../api/seats'
import { formatDateTime, statusColor, statusLabel } from '../../utils'

const { Title } = Typography

export default function SeatList() {
  const { id: studyRoomId } = useParams()
  const navigate = useNavigate()
  const [loading, setLoading] = useState(false)
  const [data, setData] = useState([])
  const [statusFilter, setStatusFilter] = useState(undefined)

  useEffect(() => {
    loadData()
  }, [studyRoomId, statusFilter])

  const loadData = async () => {
    setLoading(true)
    try {
      const params = {}
      if (statusFilter) params.status = statusFilter
      const res = await getSeatList(studyRoomId, params)
      setData(res.data || [])
    } catch (err) {
      message.error(err.message || '获取座位列表失败')
    } finally {
      setLoading(false)
    }
  }

  const columns = [
    { title: 'ID', dataIndex: 'id', key: 'id', width: 80 },
    { title: '座位号', dataIndex: 'seatNumber', key: 'seatNumber' },
    { title: '行', dataIndex: 'rowNum', key: 'rowNum', width: 60 },
    { title: '列', dataIndex: 'colNum', key: 'colNum', width: 60 },
    {
      title: '类型',
      dataIndex: 'seatType',
      key: 'seatType',
      render: (v) => <Tag color={v === 2 ? 'gold' : 'default'}>{v === 2 ? 'VIP' : '普通'}</Tag>,
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (v) => <Tag color={statusColor(v)}>{statusLabel(v)}</Tag>,
    },
    { title: '价格/时', dataIndex: 'pricePerHour', key: 'pricePerHour', render: (v) => v ? `¥${v}` : '-' },
    {
      title: '设施',
      key: 'facilities',
      render: (_, r) => (
        <Space size={4}>
          {r.hasPower === 1 && <Tag color="blue">插座</Tag>}
          {r.hasLamp === 1 && <Tag color="cyan">台灯</Tag>}
          {r.isWindow === 1 && <Tag color="green">靠窗</Tag>}
        </Space>
      ),
    },
    { title: '描述', dataIndex: 'description', key: 'description', ellipsis: true },
    { title: '更新时间', dataIndex: 'updateTime', key: 'updateTime', render: (v) => formatDateTime(v) },
  ]

  return (
    <div>
      <Space style={{ marginBottom: 16 }}>
        <Button icon={<ArrowLeftOutlined />} onClick={() => navigate('/studyrooms')}>返回自习室列表</Button>
      </Space>
      <Title level={4}>座位管理 - 自习室 #{studyRoomId}</Title>
      <Card>
        <Space style={{ marginBottom: 16 }}>
          <Select
            placeholder="状态筛选"
            value={statusFilter}
            onChange={v => setStatusFilter(v)}
            allowClear
            style={{ width: 120 }}
            options={[
              { label: '可用', value: 'available' },
              { label: '已预约', value: 'booked' },
              { label: '占用', value: 'occupied' },
              { label: '不可用', value: 'unavailable' },
            ]}
          />
          <Button type="primary" onClick={loadData}>刷新</Button>
        </Space>
        <Table
          rowKey="id"
          loading={loading}
          columns={columns}
          dataSource={data}
          pagination={{ pageSize: 20, showTotal: (t) => `共 ${t} 条` }}
        />
      </Card>
    </div>
  )
}

