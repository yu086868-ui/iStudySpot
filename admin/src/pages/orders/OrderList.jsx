import { useState, useEffect } from 'react'
import { Table, Card, Tag, Input, Button, Space, Typography, Select, Modal, Descriptions, message, Popconfirm } from 'antd'
import { EyeOutlined } from '@ant-design/icons'
import { getMyReservations, getReservationDetail, cancelReservation, payReservation } from '../../api/orders'
import { formatDateTime, statusColor, statusLabel, formatMoney } from '../../utils'

const { Title } = Typography

export default function OrderList() {
  const [loading, setLoading] = useState(false)
  const [data, setData] = useState([])
  const [total, setTotal] = useState(0)
  const [page, setPage] = useState(1)
  const [pageSize, setPageSize] = useState(20)
  const [statusFilter, setStatusFilter] = useState(undefined)
  const [detailVisible, setDetailVisible] = useState(false)
  const [currentOrder, setCurrentOrder] = useState(null)

  useEffect(() => {
    loadData()
  }, [page, pageSize, statusFilter])

  const loadData = async () => {
    setLoading(true)
    try {
      const params = { page, pageSize }
      if (statusFilter) params.status = statusFilter
      const res = await getMyReservations(params)
      const d = res.data
      setData(d?.list || [])
      setTotal(d?.total || 0)
    } catch (err) {
      message.error(err.message || '获取订单列表失败')
    } finally {
      setLoading(false)
    }
  }

  const handleViewDetail = async (id) => {
    try {
      const res = await getReservationDetail(id)
      setCurrentOrder(res.data)
      setDetailVisible(true)
    } catch (err) {
      message.error(err.message || '获取订单详情失败')
    }
  }

  const handleCancel = async (id) => {
    try {
      await cancelReservation(id)
      message.success('取消成功')
      loadData()
    } catch (err) {
      message.error(err.message || '取消失败')
    }
  }

  const handlePay = async (id) => {
    try {
      await payReservation(id)
      message.success('支付成功')
      loadData()
    } catch (err) {
      message.error(err.message || '支付失败')
    }
  }

  const columns = [
    { title: 'ID', dataIndex: 'id', key: 'id', width: 80 },
    { title: '订单号', dataIndex: 'orderNo', key: 'orderNo', ellipsis: true },
    {
      title: '自习室',
      key: 'roomName',
      render: (_, r) => r.studyRoomName || r.roomName || '-',
    },
    { title: '座位', dataIndex: 'seatNumber', key: 'seatNumber' },
    {
      title: '开始时间',
      dataIndex: 'startTime',
      key: 'startTime',
      render: (v, r) => formatDateTime(r.planStartTime || v),
    },
    {
      title: '结束时间',
      dataIndex: 'endTime',
      key: 'endTime',
      render: (v, r) => formatDateTime(r.planEndTime || v),
    },
    {
      title: '金额',
      dataIndex: 'totalPrice',
      key: 'totalPrice',
      render: (v) => formatMoney(v),
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (v) => <Tag color={statusColor(v)}>{statusLabel(v)}</Tag>,
    },
    {
      title: '操作',
      key: 'action',
      width: 220,
      render: (_, r) => (
        <Space>
          <Button type="link" size="small" icon={<EyeOutlined />} onClick={() => handleViewDetail(r.id)}>详情</Button>
          {r.status === 'pending' && (
            <Popconfirm title="确认支付该订单？（将自动完成支付）" onConfirm={() => handlePay(r.id)}>
              <Button type="link" size="small" style={{ color: '#52c41a' }}>支付</Button>
            </Popconfirm>
          )}
          {(r.status === 'pending' || r.status === 'paid') && (
            <Popconfirm title="确认取消该订单？" onConfirm={() => handleCancel(r.id)}>
              <Button type="link" size="small" danger>取消</Button>
            </Popconfirm>
          )}
        </Space>
      ),
    },
  ]

  return (
    <div>
      <Title level={4} style={{ marginBottom: 24 }}>订单管理</Title>
      <Card>
        <Space style={{ marginBottom: 16 }} wrap>
          <Select
            placeholder="状态筛选"
            value={statusFilter}
            onChange={v => { setStatusFilter(v); setPage(1) }}
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
            showTotal: (t) => `共 ${t} 条`,
            onChange: (p, ps) => { setPage(p); setPageSize(ps) },
          }}
        />
      </Card>

      <Modal title="订单详情" open={detailVisible} onCancel={() => setDetailVisible(false)} footer={null} width={640}>
        {currentOrder && (
          <Descriptions bordered column={{ xs: 1, sm: 2 }} size="small">
            <Descriptions.Item label="ID">{currentOrder.id}</Descriptions.Item>
            <Descriptions.Item label="订单号">{currentOrder.orderNo}</Descriptions.Item>
            <Descriptions.Item label="自习室">{currentOrder.studyRoomName || currentOrder.roomName || '-'}</Descriptions.Item>
            <Descriptions.Item label="座位">{currentOrder.seatNumber || currentOrder.seatPosition || '-'}</Descriptions.Item>
            <Descriptions.Item label="开始时间">{formatDateTime(currentOrder.planStartTime || currentOrder.startTime)}</Descriptions.Item>
            <Descriptions.Item label="结束时间">{formatDateTime(currentOrder.planEndTime || currentOrder.endTime)}</Descriptions.Item>
            <Descriptions.Item label="签到时间">{formatDateTime(currentOrder.checkinTime || currentOrder.actualStartTime)}</Descriptions.Item>
            <Descriptions.Item label="签退时间">{formatDateTime(currentOrder.checkoutTime || currentOrder.actualEndTime)}</Descriptions.Item>
            <Descriptions.Item label="总金额">{formatMoney(currentOrder.totalPrice || currentOrder.totalAmount)}</Descriptions.Item>
            <Descriptions.Item label="实际金额">{formatMoney(currentOrder.actualPrice)}</Descriptions.Item>
            <Descriptions.Item label="状态"><Tag color={statusColor(currentOrder.status)}>{statusLabel(currentOrder.status)}</Tag></Descriptions.Item>
            <Descriptions.Item label="创建时间">{formatDateTime(currentOrder.createdAt || currentOrder.createTime)}</Descriptions.Item>
          </Descriptions>
        )}
      </Modal>
    </div>
  )
}
