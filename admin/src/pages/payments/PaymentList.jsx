import { useState, useEffect } from 'react'
import { Table, Card, Tag, Button, Typography, Select, Space, Modal, Descriptions, Input, InputNumber, message } from 'antd'
import { getPaymentStatus, createPayment, paymentCallback } from '../../api/payments'
import { formatDateTime, statusColor, statusLabel, formatMoney } from '../../utils'

const { Title } = Typography

export default function PaymentList() {
  const [loading, setLoading] = useState(false)
  const [detailVisible, setDetailVisible] = useState(false)
  const [currentPayment, setCurrentPayment] = useState(null)
  const [searchId, setSearchId] = useState('')
  const [payments, setPayments] = useState([])
  const [createVisible, setCreateVisible] = useState(false)
  const [createForm, setCreateForm] = useState({ orderId: '', amount: '', paymentMethod: 'wechat' })

  const handleSearch = async () => {
    if (!searchId) {
      message.warning('请输入支付ID')
      return
    }
    setLoading(true)
    try {
      const res = await getPaymentStatus(searchId)
      setCurrentPayment(res.data)
      setDetailVisible(true)
    } catch (err) {
      message.error(err.message || '查询失败')
    } finally {
      setLoading(false)
    }
  }

  const handleCreate = async () => {
    try {
      await createPayment({
        orderId: Number(createForm.orderId),
        amount: Number(createForm.amount),
        paymentMethod: createForm.paymentMethod,
      })
      message.success('支付订单创建成功')
      setCreateVisible(false)
    } catch (err) {
      message.error(err.message || '创建失败')
    }
  }

  const handleCallback = async (paymentNo, success) => {
    try {
      await paymentCallback({ paymentNo, success })
      message.success('回调处理成功')
      if (currentPayment) {
        const res = await getPaymentStatus(currentPayment.id)
        setCurrentPayment(res.data)
      }
    } catch (err) {
      message.error(err.message || '回调处理失败')
    }
  }

  return (
    <div>
      <Title level={4} style={{ marginBottom: 24 }}>支付管理</Title>
      <Card>
        <Space style={{ marginBottom: 16 }} wrap>
          <Input
            placeholder="输入支付ID查询"
            value={searchId}
            onChange={e => setSearchId(e.target.value)}
            style={{ width: 200 }}
            onPressEnter={handleSearch}
          />
          <Button type="primary" onClick={handleSearch} loading={loading}>查询</Button>
          <Button onClick={() => setCreateVisible(true)}>创建支付</Button>
        </Space>

        <div style={{ padding: '40px 0', textAlign: 'center', color: '#999' }}>
          输入支付ID查询支付详情，或创建新的支付订单
        </div>
      </Card>

      <Modal title="支付详情" open={detailVisible} onCancel={() => setDetailVisible(false)} footer={null} width={600}>
        {currentPayment && (
          <Descriptions bordered column={{ xs: 1, sm: 2 }} size="small">
            <Descriptions.Item label="ID">{currentPayment.id}</Descriptions.Item>
            <Descriptions.Item label="支付编号">{currentPayment.paymentNo}</Descriptions.Item>
            <Descriptions.Item label="订单ID">{currentPayment.orderId}</Descriptions.Item>
            <Descriptions.Item label="用户ID">{currentPayment.userId}</Descriptions.Item>
            <Descriptions.Item label="金额">{formatMoney(currentPayment.amount)}</Descriptions.Item>
            <Descriptions.Item label="支付方式">{currentPayment.paymentMethod === 'wechat' ? '微信' : currentPayment.paymentMethod === 'alipay' ? '支付宝' : currentPayment.paymentMethod === 'balance' ? '余额' : currentPayment.paymentMethod}</Descriptions.Item>
            <Descriptions.Item label="状态"><Tag color={statusColor(currentPayment.status)}>{statusLabel(currentPayment.status)}</Tag></Descriptions.Item>
            <Descriptions.Item label="支付时间">{formatDateTime(currentPayment.payTime)}</Descriptions.Item>
            <Descriptions.Item label="创建时间">{formatDateTime(currentPayment.createdAt || currentPayment.createTime)}</Descriptions.Item>
            {currentPayment.paymentUrl && <Descriptions.Item label="支付链接" span={2}><a href={currentPayment.paymentUrl} target="_blank" rel="noreferrer">{currentPayment.paymentUrl}</a></Descriptions.Item>}
          </Descriptions>
        )}
      </Modal>

      <Modal title="创建支付" open={createVisible} onOk={handleCreate} onCancel={() => setCreateVisible(false)}>
        <div style={{ display: 'flex', flexDirection: 'column', gap: 12, padding: '16px 0' }}>
          <InputNumber placeholder="订单ID" value={createForm.orderId} onChange={v => setCreateForm({ ...createForm, orderId: v })} style={{ width: '100%' }} />
          <InputNumber placeholder="金额" value={createForm.amount} onChange={v => setCreateForm({ ...createForm, amount: v })} style={{ width: '100%' }} min={0} precision={2} />
          <Select value={createForm.paymentMethod} onChange={v => setCreateForm({ ...createForm, paymentMethod: v })} options={[
            { label: '微信', value: 'wechat' },
            { label: '支付宝', value: 'alipay' },
            { label: '余额', value: 'balance' },
          ]} />
        </div>
      </Modal>
    </div>
  )
}
