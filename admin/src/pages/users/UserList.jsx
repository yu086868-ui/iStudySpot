import { useEffect, useState } from 'react'
import { Button, Card, Input, message, Select, Space, Table, Tag, Typography } from 'antd'
import { SearchOutlined } from '@ant-design/icons'
import { getAdminUsers } from '../../api/users'
import { formatDateTime, formatMoney } from '../../utils'

const { Title } = Typography
const { Search } = Input

function userStatusColor(status) {
  return Number(status) === 1 ? 'green' : 'red'
}

function userStatusLabel(status) {
  return Number(status) === 1 ? '正常' : '封禁'
}

export default function UserList() {
  const [loading, setLoading] = useState(false)
  const [data, setData] = useState([])
  const [total, setTotal] = useState(0)
  const [page, setPage] = useState(1)
  const [pageSize, setPageSize] = useState(20)
  const [keyword, setKeyword] = useState('')
  const [statusFilter, setStatusFilter] = useState(undefined)

  useEffect(() => {
    loadData()
  }, [page, pageSize, statusFilter])

  const loadData = async () => {
    setLoading(true)
    try {
      const params = { page, pageSize }
      if (keyword) params.keyword = keyword
      if (statusFilter !== undefined) params.status = statusFilter
      const res = await getAdminUsers(params)
      const payload = res.data || {}
      setData(payload.list || [])
      setTotal(payload.total || 0)
    } catch (err) {
      message.error(err.message || '获取用户列表失败')
    } finally {
      setLoading(false)
    }
  }

  const columns = [
    { title: 'ID', dataIndex: 'id', key: 'id', width: 80 },
    { title: '用户名', dataIndex: 'username', key: 'username' },
    { title: '昵称', dataIndex: 'nickname', key: 'nickname', render: (value) => value || '-' },
    { title: '手机号', dataIndex: 'phone', key: 'phone', render: (value) => value || '-' },
    { title: '邮箱', dataIndex: 'email', key: 'email', render: (value) => value || '-' },
    { title: '学号', dataIndex: 'studentId', key: 'studentId', render: (value) => value || '-' },
    {
      title: '信用分',
      dataIndex: 'creditScore',
      key: 'creditScore',
      render: (value) => <Tag color={(value ?? 100) >= 80 ? 'green' : (value ?? 100) >= 60 ? 'orange' : 'red'}>{value ?? 100}</Tag>,
    },
    { title: '余额', dataIndex: 'balance', key: 'balance', render: (value) => formatMoney(value) },
    { title: '积分', dataIndex: 'points', key: 'points', render: (value) => value ?? 0 },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (value) => <Tag color={userStatusColor(value)}>{userStatusLabel(value)}</Tag>,
    },
    { title: '违规次数', dataIndex: 'violationCount', key: 'violationCount', render: (value) => value ?? 0 },
    { title: '最近登录', dataIndex: 'lastLoginTime', key: 'lastLoginTime', render: formatDateTime },
  ]

  return (
    <div>
      <Title level={4} style={{ marginBottom: 24 }}>用户管理</Title>
      <Card>
        <Space style={{ marginBottom: 16 }} wrap>
          <Search
            placeholder="搜索用户名、昵称、手机号、邮箱或学号"
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
            style={{ width: 120 }}
            options={[
              { label: '正常', value: 1 },
              { label: '封禁', value: 0 },
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
    </div>
  )
}
