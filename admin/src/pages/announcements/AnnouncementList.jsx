import { useEffect, useState } from 'react'
import { Button, Card, Descriptions, message, Modal, Popconfirm, Select, Space, Table, Tag, Typography } from 'antd'
import { DeleteOutlined, EyeOutlined } from '@ant-design/icons'
import {
  deleteAdminAnnouncement,
  getAdminAnnouncementDetail,
  getAdminAnnouncementList,
} from '../../api/announcements'
import { formatDateTime, statusColor } from '../../utils'

const { Title } = Typography

const typeMap = {
  notice: '普通公告',
  maintenance: '维护公告',
  event: '活动公告',
  emergency: '紧急公告',
}

const priorityMap = {
  low: '低',
  medium: '中',
  high: '高',
}

const statusMap = {
  published: '已发布',
  draft: '草稿',
  archived: '已归档',
}

export default function AnnouncementList() {
  const [loading, setLoading] = useState(false)
  const [data, setData] = useState([])
  const [total, setTotal] = useState(0)
  const [page, setPage] = useState(1)
  const [pageSize, setPageSize] = useState(20)
  const [typeFilter, setTypeFilter] = useState(undefined)
  const [priorityFilter, setPriorityFilter] = useState(undefined)
  const [statusFilter, setStatusFilter] = useState(undefined)
  const [detailVisible, setDetailVisible] = useState(false)
  const [detailLoading, setDetailLoading] = useState(false)
  const [currentAnnouncement, setCurrentAnnouncement] = useState(null)

  const loadData = async () => {
    setLoading(true)
    try {
      const params = { page, pageSize }
      if (typeFilter) params.type = typeFilter
      if (priorityFilter) params.priority = priorityFilter
      if (statusFilter) params.status = statusFilter
      const res = await getAdminAnnouncementList(params)
      const payload = res.data || {}
      setData(payload.list || [])
      setTotal(payload.total || 0)
    } catch (err) {
      message.error(err.message || '获取公告列表失败')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadData()
  }, [page, pageSize, typeFilter, priorityFilter, statusFilter])

  const openDetail = async (id) => {
    setDetailVisible(true)
    setDetailLoading(true)
    try {
      const res = await getAdminAnnouncementDetail(id)
      setCurrentAnnouncement(res.data || null)
    } catch (err) {
      message.error(err.message || '获取公告详情失败')
      setCurrentAnnouncement(null)
    } finally {
      setDetailLoading(false)
    }
  }

  const handleDelete = async (id) => {
    try {
      await deleteAdminAnnouncement(id)
      message.success('公告已删除')
      if (currentAnnouncement?.id === id) {
        setDetailVisible(false)
        setCurrentAnnouncement(null)
      }
      if (data.length === 1 && page > 1) {
        setPage(page - 1)
      } else {
        loadData()
      }
    } catch (err) {
      message.error(err.message || '删除公告失败')
    }
  }

  const columns = [
    { title: 'ID', dataIndex: 'id', key: 'id', width: 80 },
    { title: '标题', dataIndex: 'title', key: 'title', ellipsis: true },
    {
      title: '类型',
      dataIndex: 'type',
      key: 'type',
      render: (value) => <Tag>{typeMap[value] || value}</Tag>,
    },
    {
      title: '优先级',
      dataIndex: 'priority',
      key: 'priority',
      render: (value) => (
        <Tag color={value === 'high' ? 'red' : value === 'medium' ? 'orange' : 'default'}>
          {priorityMap[value] || value}
        </Tag>
      ),
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (value) => <Tag color={statusColor(value)}>{statusMap[value] || value}</Tag>,
    },
    { title: '发布人', dataIndex: 'author', key: 'author', render: (value) => value || '-' },
    { title: '发布时间', dataIndex: 'publishTime', key: 'publishTime', render: formatDateTime },
    {
      title: '操作',
      key: 'action',
      width: 180,
      render: (_, record) => (
        <Space size="small">
          <Button type="link" icon={<EyeOutlined />} onClick={() => openDetail(record.id)}>
            详情
          </Button>
          <Popconfirm
            title="删除公告"
            description="删除后将无法恢复，确认继续吗？"
            okText="删除"
            cancelText="取消"
            okButtonProps={{ danger: true }}
            onConfirm={() => handleDelete(record.id)}
          >
            <Button type="link" danger icon={<DeleteOutlined />}>
              删除
            </Button>
          </Popconfirm>
        </Space>
      ),
    },
  ]

  return (
    <div>
      <Title level={4} style={{ marginBottom: 24 }}>公告管理</Title>
      <Card>
        <Space style={{ marginBottom: 16 }} wrap>
          <Select
            placeholder="按类型筛选"
            value={typeFilter}
            onChange={(value) => {
              setTypeFilter(value)
              setPage(1)
            }}
            allowClear
            style={{ width: 140 }}
            options={Object.entries(typeMap).map(([value, label]) => ({ value, label }))}
          />
          <Select
            placeholder="按优先级筛选"
            value={priorityFilter}
            onChange={(value) => {
              setPriorityFilter(value)
              setPage(1)
            }}
            allowClear
            style={{ width: 140 }}
            options={Object.entries(priorityMap).map(([value, label]) => ({ value, label }))}
          />
          <Select
            placeholder="按状态筛选"
            value={statusFilter}
            onChange={(value) => {
              setStatusFilter(value)
              setPage(1)
            }}
            allowClear
            style={{ width: 140 }}
            options={Object.entries(statusMap).map(([value, label]) => ({ value, label }))}
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

      <Modal title="公告详情" open={detailVisible} onCancel={() => setDetailVisible(false)} footer={null} width={760}>
        <Card loading={detailLoading} bordered={false}>
          {currentAnnouncement && (
            <Descriptions bordered column={1} size="small">
              <Descriptions.Item label="ID">{currentAnnouncement.id}</Descriptions.Item>
              <Descriptions.Item label="标题">{currentAnnouncement.title}</Descriptions.Item>
              <Descriptions.Item label="类型">{typeMap[currentAnnouncement.type] || currentAnnouncement.type}</Descriptions.Item>
              <Descriptions.Item label="优先级">
                {priorityMap[currentAnnouncement.priority] || currentAnnouncement.priority}
              </Descriptions.Item>
              <Descriptions.Item label="状态">
                <Tag color={statusColor(currentAnnouncement.status)}>
                  {statusMap[currentAnnouncement.status] || currentAnnouncement.status}
                </Tag>
              </Descriptions.Item>
              <Descriptions.Item label="发布人">{currentAnnouncement.author || '-'}</Descriptions.Item>
              <Descriptions.Item label="发布时间">{formatDateTime(currentAnnouncement.publishTime)}</Descriptions.Item>
              <Descriptions.Item label="过期时间">{formatDateTime(currentAnnouncement.expireTime)}</Descriptions.Item>
              <Descriptions.Item label="正文">
                <div style={{ whiteSpace: 'pre-wrap' }}>{currentAnnouncement.content}</div>
              </Descriptions.Item>
            </Descriptions>
          )}
        </Card>
      </Modal>
    </div>
  )
}
