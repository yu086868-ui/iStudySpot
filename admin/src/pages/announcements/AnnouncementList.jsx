import { useState, useEffect } from 'react'
import { Table, Card, Tag, Button, Typography, Select, Space, message } from 'antd'
import { getAnnouncementList, getAnnouncementDetail } from '../../api/announcements'
import { formatDateTime } from '../../utils'

const { Title } = Typography

const typeMap = { notice: '通知', maintenance: '维护', event: '活动', emergency: '紧急' }
const priorityMap = { low: '低', medium: '中', high: '高' }
const statusMap = { published: '已发布', draft: '草稿', archived: '已归档' }

export default function AnnouncementList() {
  const [loading, setLoading] = useState(false)
  const [data, setData] = useState([])
  const [total, setTotal] = useState(0)
  const [page, setPage] = useState(1)
  const [pageSize, setPageSize] = useState(20)
  const [typeFilter, setTypeFilter] = useState(undefined)
  const [priorityFilter, setPriorityFilter] = useState(undefined)

  useEffect(() => {
    loadData()
  }, [page, pageSize, typeFilter, priorityFilter])

  const loadData = async () => {
    setLoading(true)
    try {
      const params = { page, pageSize }
      if (typeFilter) params.type = typeFilter
      if (priorityFilter) params.priority = priorityFilter
      const res = await getAnnouncementList(params)
      const d = res.data
      setData(d?.list || [])
      setTotal(d?.total || 0)
    } catch (err) {
      message.error(err.message || '获取公告列表失败')
    } finally {
      setLoading(false)
    }
  }

  const columns = [
    { title: 'ID', dataIndex: 'id', key: 'id', width: 80 },
    { title: '标题', dataIndex: 'title', key: 'title', ellipsis: true },
    {
      title: '类型',
      dataIndex: 'type',
      key: 'type',
      render: (v) => <Tag>{typeMap[v] || v}</Tag>,
    },
    {
      title: '优先级',
      dataIndex: 'priority',
      key: 'priority',
      render: (v) => (
        <Tag color={v === 'high' ? 'red' : v === 'medium' ? 'orange' : 'default'}>
          {priorityMap[v] || v}
        </Tag>
      ),
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (v) => <Tag color={v === 'published' ? 'green' : v === 'draft' ? 'orange' : 'default'}>{statusMap[v] || v}</Tag>,
    },
    { title: '作者', dataIndex: 'author', key: 'author' },
    {
      title: '发布时间',
      dataIndex: 'publishTime',
      key: 'publishTime',
      render: (v) => formatDateTime(v),
    },
  ]

  return (
    <div>
      <Title level={4} style={{ marginBottom: 24 }}>公告管理</Title>
      <Card>
        <Space style={{ marginBottom: 16 }} wrap>
          <Select
            placeholder="类型筛选"
            value={typeFilter}
            onChange={v => { setTypeFilter(v); setPage(1) }}
            allowClear
            style={{ width: 120 }}
            options={Object.entries(typeMap).map(([k, v]) => ({ label: v, value: k }))}
          />
          <Select
            placeholder="优先级筛选"
            value={priorityFilter}
            onChange={v => { setPriorityFilter(v); setPage(1) }}
            allowClear
            style={{ width: 120 }}
            options={Object.entries(priorityMap).map(([k, v]) => ({ label: v, value: k }))}
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
    </div>
  )
}
