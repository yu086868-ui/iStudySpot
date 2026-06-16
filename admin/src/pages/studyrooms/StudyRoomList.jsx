import { useEffect, useState } from 'react'
import { Table, Card, Tag, Input, Button, Space, Typography, Select, message } from 'antd'
import { SearchOutlined, EyeOutlined } from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'
import { getStudyRoomList } from '../../api/studyRooms'
import { formatDateTime, statusColor } from '../../utils'

const { Title } = Typography
const { Search } = Input

function roomStatusValue(status) {
  return Number(status) === 1 ? 'open' : 'closed'
}

function roomStatusLabel(status) {
  return roomStatusValue(status) === 'open' ? '开放' : '关闭'
}

export default function StudyRoomList() {
  const [loading, setLoading] = useState(false)
  const [data, setData] = useState([])
  const [total, setTotal] = useState(0)
  const [page, setPage] = useState(1)
  const [pageSize, setPageSize] = useState(20)
  const [keyword, setKeyword] = useState('')
  const [statusFilter, setStatusFilter] = useState(undefined)
  const navigate = useNavigate()

  useEffect(() => {
    loadData()
  }, [page, pageSize, statusFilter])

  const loadData = async () => {
    setLoading(true)
    try {
      const params = { page, pageSize }
      if (keyword) params.keyword = keyword
      if (statusFilter) params.status = statusFilter
      const res = await getStudyRoomList(params)
      const payload = res.data || {}
      setData(payload.list || [])
      setTotal(payload.total || 0)
    } catch (err) {
      message.error(err.message || '获取自习室列表失败')
    } finally {
      setLoading(false)
    }
  }

  const handleSearch = () => {
    setPage(1)
    loadData()
  }

  const columns = [
    { title: 'ID', dataIndex: 'id', key: 'id', width: 80 },
    { title: '名称', dataIndex: 'name', key: 'name', ellipsis: true },
    { title: '地址', dataIndex: 'address', key: 'address', ellipsis: true },
    {
      title: '开放时间',
      key: 'openTime',
      render: (_, record) => `${record.openTime || '-'} ~ ${record.closeTime || '-'}`,
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (value) => {
        const normalized = roomStatusValue(value)
        return <Tag color={statusColor(normalized)}>{roomStatusLabel(value)}</Tag>
      },
    },
    { title: '描述', dataIndex: 'description', key: 'description', ellipsis: true },
    { title: '创建时间', dataIndex: 'createTime', key: 'createTime', render: formatDateTime },
    {
      title: '操作',
      key: 'action',
      width: 180,
      render: (_, record) => (
        <Space>
          <Button type="link" size="small" icon={<EyeOutlined />} onClick={() => navigate(`/studyrooms/${record.id}`)}>
            详情
          </Button>
          <Button type="link" size="small" onClick={() => navigate(`/studyrooms/${record.id}/seats`)}>
            座位
          </Button>
        </Space>
      ),
    },
  ]

  return (
    <div>
      <Title level={4} style={{ marginBottom: 24 }}>自习室</Title>
      <Card>
        <Space style={{ marginBottom: 16 }} wrap>
          <Search
            placeholder="搜索自习室"
            value={keyword}
            onChange={(event) => setKeyword(event.target.value)}
            onSearch={handleSearch}
            style={{ width: 250 }}
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
            style={{ width: 120 }}
            options={[
              { label: '开放', value: 'open' },
              { label: '关闭', value: 'closed' },
            ]}
          />
          <Button type="primary" onClick={handleSearch}>刷新</Button>
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
    </div>
  )
}
