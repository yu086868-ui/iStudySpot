import { useState, useEffect } from 'react'
import { Table, Card, Tag, Input, Button, Space, Typography, Select, message } from 'antd'
import { HomeOutlined, SearchOutlined, EyeOutlined } from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'
import { getStudyRoomList } from '../../api/studyRooms'
import { formatDateTime, statusColor, statusLabel } from '../../utils'

const { Title } = Typography
const { Search } = Input

const statusMap = { 1: 'open', 0: 'closed' }

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
  }, [page, pageSize])

  const loadData = async () => {
    setLoading(true)
    try {
      const params = { page, pageSize }
      if (keyword) params.keyword = keyword
      if (statusFilter !== undefined) params.status = statusFilter
      const res = await getStudyRoomList(params)
      const d = res.data
      setData(d?.list || [])
      setTotal(d?.total || 0)
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
    { title: '名称', dataIndex: 'name', key: 'name' },
    { title: '地址', dataIndex: 'address', key: 'address', ellipsis: true },
    {
      title: '开放时间',
      key: 'openTime',
      render: (_, r) => `${r.openTime || '-'} ~ ${r.closeTime || '-'}`,
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (v) => <Tag color={statusColor(statusMap[v] || 'closed')}>{statusMap[v] === 'open' ? '开放' : statusMap[v] === 'closed' ? '关闭' : v}</Tag>,
    },
    {
      title: '描述',
      dataIndex: 'description',
      key: 'description',
      ellipsis: true,
    },
    {
      title: '创建时间',
      dataIndex: 'createTime',
      key: 'createTime',
      render: (v) => formatDateTime(v),
    },
    {
      title: '操作',
      key: 'action',
      width: 200,
      render: (_, r) => (
        <Space>
          <Button type="link" size="small" icon={<EyeOutlined />} onClick={() => navigate(`/studyrooms/${r.id}`)}>
            详情
          </Button>
          <Button type="link" size="small" onClick={() => navigate(`/studyrooms/${r.id}/seats`)}>
            座位
          </Button>
        </Space>
      ),
    },
  ]

  return (
    <div>
      <Title level={4} style={{ marginBottom: 24 }}>自习室管理</Title>
      <Card>
        <Space style={{ marginBottom: 16 }} wrap>
          <Search
            placeholder="搜索自习室"
            value={keyword}
            onChange={e => setKeyword(e.target.value)}
            onSearch={handleSearch}
            style={{ width: 250 }}
            enterButton={<SearchOutlined />}
          />
          <Select
            placeholder="状态筛选"
            value={statusFilter}
            onChange={v => { setStatusFilter(v); setPage(1) }}
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
            showTotal: (t) => `共 ${t} 条`,
            onChange: (p, ps) => { setPage(p); setPageSize(ps) },
          }}
        />
      </Card>
    </div>
  )
}
