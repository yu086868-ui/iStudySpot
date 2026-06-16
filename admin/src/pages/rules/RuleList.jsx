import { useState, useEffect } from 'react'
import { Table, Card, Tag, Button, Typography, Select, Space, message } from 'antd'
import { getRuleList } from '../../api/rules'

const { Title } = Typography

const categoryMap = { booking: '预约', usage: '使用', penalty: '处罚', general: '通用' }

export default function RuleList() {
  const [loading, setLoading] = useState(false)
  const [data, setData] = useState([])
  const [categoryFilter, setCategoryFilter] = useState(undefined)

  useEffect(() => {
    loadData()
  }, [categoryFilter])

  const loadData = async () => {
    setLoading(true)
    try {
      const params = {}
      if (categoryFilter) params.category = categoryFilter
      const res = await getRuleList(params)
      setData(res.data || [])
    } catch (err) {
      message.error(err.message || '获取规则列表失败')
    } finally {
      setLoading(false)
    }
  }

  const columns = [
    { title: 'ID', dataIndex: 'id', key: 'id', width: 80 },
    { title: '标题', dataIndex: 'title', key: 'title' },
    {
      title: '分类',
      dataIndex: 'category',
      key: 'category',
      render: (v, r) => <Tag color="blue">{r.categoryLabel || categoryMap[v] || v}</Tag>,
    },
    { title: '内容', dataIndex: 'content', key: 'content', ellipsis: true },
    { title: '优先级', dataIndex: 'priority', key: 'priority', width: 80 },
    {
      title: '类型',
      dataIndex: 'type',
      key: 'type',
      render: (v) => <Tag>{v === 'faq' ? '常见问题' : '规则'}</Tag>,
    },
  ]

  return (
    <div>
      <Title level={4} style={{ marginBottom: 24 }}>规则管理</Title>
      <Card>
        <Space style={{ marginBottom: 16 }} wrap>
          <Select
            placeholder="分类筛选"
            value={categoryFilter}
            onChange={v => setCategoryFilter(v)}
            allowClear
            style={{ width: 120 }}
            options={Object.entries(categoryMap).map(([k, v]) => ({ label: v, value: k }))}
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
