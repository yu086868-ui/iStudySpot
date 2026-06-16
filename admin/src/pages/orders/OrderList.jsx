import { useEffect, useState } from 'react'
import { Button, Card, Descriptions, Input, message, Modal, Select, Space, Table, Tag, Typography } from 'antd'
import { EyeOutlined, SearchOutlined } from '@ant-design/icons'
import { getAdminReservationDetail, getAdminReservations } from '../../api/orders'
import { formatDateTime, formatMoney, statusColor, statusLabel } from '../../utils'

const { Title } = Typography
const { Search } = Input

export default function OrderList() {
  const [loading, setLoading] = useState(false)
  const [data, setData] = useState([])
  const [total, setTotal] = useState(0)
  const [page, setPage] = useState(1)
  const [pageSize, setPageSize] = useState(20)
  const [keyword, setKeyword] = useState('')
  const [statusFilter, setStatusFilter] = useState(undefined)
  const [detailVisible, setDetailVisible] = useState(false)
  const [detailLoading, setDetailLoading] = useState(false)
  const [currentOrder, setCurrentOrder] = useState(null)

  useEffect(() => {
    loadData()
  }, [page, pageSize, statusFilter])

  const loadData = async () => {
    setLoading(true)
    try {
      const params = { page, pageSize }
      if (keyword) params.keyword = keyword
      if (statusFilter) params.status = statusFilter
      const res = await getAdminReservations(params)
      const payload = res.data || {}
      setData(payload.list || [])
      setTotal(payload.total || 0)
    } catch (err) {
      message.error(err.message || '获取订单列表失败')
    } finally {
      setLoading(false)
    }
  }

  const openDetail = async (id) => {
    setDetailVisible(true)
    setDetailLoading(true)
    try {
      const res = await getAdminReservationDetail(id)
      setCurrentOrder(res.data || null)
    } catch (err) {
      message.error(err.message || '获取订单详情失败')
      setCurrentOrder(null)
    } finally {
      setDetailLoading(false)
    }
  }

  const columns = [
    { title: 'ID', dataIndex: 'id', key: 'id', width: 80 },
    { title: '订单号', dataIndex: 'orderNo', key: 'orderNo', ellipsis: true },
    { title: '用户 ID', dataIndex: 'userId', key: 'userId', width: 100 },
    { title: '自习室', key: 'roomName', render: (_, record) => record.studyRoomName || record.roomName || '-' },
    { title: '座位', key: 'seatNumber', render: (_, record) => record.seatNumber || record.seatPosition || '-' },
    { title: '开始时间', key: 'startTime', render: (_, record) => formatDateTime(record.startTime || record.planStartTime) },
    { title: '结束时间', key: 'endTime', render: (_, record) => formatDateTime(record.endTime || record.planEndTime) },
    { title: '金额', key: 'totalPrice', render: (_, record) => formatMoney(record.totalPrice || record.totalAmount) },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (value) => <Tag color={statusColor(value)}>{statusLabel(value)}</Tag>,
    },
    {
      title: '操作',
      key: 'action',
      width: 120,
      render: (_, record) => (
        <Button type="link" size="small" icon={<EyeOutlined />} onClick={() => openDetail(record.id)}>
          详情
        </Button>
      ),
    },
  ]

  return (
    <div>
      <Title level={4} style={{ marginBottom: 24 }}>订单管理</Title>
      <Card>
        <Space style={{ marginBottom: 16 }} wrap>
          <Search
            placeholder="搜索订单号、自习室、座位或用户 ID"
            value={keyword}
            onChange={(event) => setKeyword(event.target.value)}
            onSearch={() => {
              setPage(1)
              loadData()
            }}
            style={{ width: 320 }}
            enterButton={<SearchOutlined />}
          />
          <Select
            placeholder="状态筛选"
            value={statusFilter}
            onChange={(value) => {
              setStatusFilter(value)
              setPage(1)
            }}
            allowClear
            style={{ width: 140 }}
            options={[
              { label: '待支付', value: 'pending' },
              { label: '已支付', value: 'paid' },
              { label: '使用中', value: 'in_use' },
              { label: '已完成', value: 'completed' },
              { label: '已取消', value: 'cancelled' },
            ]}
          />
          <Button type="primary" onClick={loadData}>刷新</Button>
        </Space>
        <Table
          rowKey="id"
          loading={loading}
          columns={columns}
          dataSource={data}
          pagination={{
            current: page,
            pageSize,
            total,
            showSizeChanger: true,
            showTotal: (value) => `共 ${value} 条`,
            onChange: (nextPage, nextPageSize) => {
              setPage(nextPage)
              setPageSize(nextPageSize)
            },
          }}
        />
      </Card>

      <Modal title="订单详情" open={detailVisible} onCancel={() => setDetailVisible(false)} footer={null} width={720}>
        <Card loading={detailLoading} bordered={false}>
          {currentOrder && (
            <Descriptions bordered column={{ xs: 1, sm: 2 }} size="small">
              <Descriptions.Item label="ID">{currentOrder.id}</Descriptions.Item>
              <Descriptions.Item label="订单号">{currentOrder.orderNo || '-'}</Descriptions.Item>
              <Descriptions.Item label="用户 ID">{currentOrder.userId || '-'}</Descriptions.Item>
              <Descriptions.Item label="自习室">{currentOrder.studyRoomName || currentOrder.roomName || '-'}</Descriptions.Item>
              <Descriptions.Item label="座位">{currentOrder.seatNumber || currentOrder.seatPosition || '-'}</Descriptions.Item>
              <Descriptions.Item label="开始时间">{formatDateTime(currentOrder.startTime || currentOrder.planStartTime)}</Descriptions.Item>
              <Descriptions.Item label="结束时间">{formatDateTime(currentOrder.endTime || currentOrder.planEndTime)}</Descriptions.Item>
              <Descriptions.Item label="签到时间">{formatDateTime(currentOrder.checkinTime || currentOrder.actualStartTime)}</Descriptions.Item>
              <Descriptions.Item label="签退时间">{formatDateTime(currentOrder.checkoutTime || currentOrder.actualEndTime)}</Descriptions.Item>
              <Descriptions.Item label="总金额">{formatMoney(currentOrder.totalPrice || currentOrder.totalAmount)}</Descriptions.Item>
              <Descriptions.Item label="实际金额">{formatMoney(currentOrder.actualPrice)}</Descriptions.Item>
              <Descriptions.Item label="状态">
                <Tag color={statusColor(currentOrder.status)}>{statusLabel(currentOrder.status)}</Tag>
              </Descriptions.Item>
              <Descriptions.Item label="创建时间">{formatDateTime(currentOrder.createdAt || currentOrder.createTime)}</Descriptions.Item>
              <Descriptions.Item label="更新时间">{formatDateTime(currentOrder.updatedAt)}</Descriptions.Item>
            </Descriptions>
          )}
        </Card>
      </Modal>
    </div>
  )
}
