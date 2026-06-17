import { useEffect, useState } from 'react'
import { Table, Card, Tag, Button, Typography, message, Select, Space } from 'antd'
import { ArrowLeftOutlined } from '@ant-design/icons'
import { useParams, useNavigate } from 'react-router-dom'
import { getSeatList } from '../../api/seats'
import { formatDateTime, formatMoney, statusColor, statusLabel } from '../../utils'

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
      render: (value) => <Tag color={Number(value) === 2 ? 'gold' : 'default'}>{Number(value) === 2 ? 'VIP' : '普通'}</Tag>,
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (value) => <Tag color={statusColor(value)}>{statusLabel(value)}</Tag>,
    },
    { title: '价格/小时', dataIndex: 'pricePerHour', key: 'pricePerHour', render: (value) => (value ? formatMoney(value) : '-') },
    {
      title: '设施',
      key: 'facilities',
      render: (_, record) => (
        <Space size={4}>
          {Number(record.hasPower) === 1 && <Tag color="blue">插座</Tag>}
          {Number(record.hasLamp) === 1 && <Tag color="cyan">台灯</Tag>}
          {Number(record.isWindow) === 1 && <Tag color="green">靠窗</Tag>}
        </Space>
      ),
    },
    { title: '描述', dataIndex: 'description', key: 'description', ellipsis: true },
    { title: '更新时间', dataIndex: 'updateTime', key: 'updateTime', render: formatDateTime },
  ]

  return (
    <div>
      <Space style={{ marginBottom: 16 }}>
        <Button icon={<ArrowLeftOutlined />} onClick={() => navigate('/studyrooms')}>返回自习室列表</Button>
      </Space>
      <Title level={4}>座位列表 - 自习室 #{studyRoomId}</Title>
      <Card>
        <Space style={{ marginBottom: 16 }}>
          <Select
            placeholder="状态筛选"
            value={statusFilter}
            onChange={setStatusFilter}
            allowClear
            style={{ width: 140 }}
            options={[
              { label: '可用', value: 'available' },
              { label: '已预约', value: 'booked' },
              { label: '使用中', value: 'in_use' },
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
          pagination={{ pageSize: 20, showTotal: (total) => `共 ${total} 条` }}
        />
      </Card>
    </div>
  )
}
