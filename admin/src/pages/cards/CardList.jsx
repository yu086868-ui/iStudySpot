import { useState } from 'react'
import { Card, Typography, Input, Button, Table, Tag, Space, message, Image } from 'antd'
import { getCardList, getCardDetail } from '../../api/cards'
import { formatDateTime, rarityColor } from '../../utils'

const { Title } = Typography

const rarityNameMap = { N: '白', R: '绿', SR: '蓝', SSR: '紫', UR: '金', LR: '红' }

export default function CardList() {
  const [loading, setLoading] = useState(false)
  const [data, setData] = useState([])
  const [userId, setUserId] = useState('')

  const handleSearch = async () => {
    if (!userId) {
      message.warning('请输入用户ID')
      return
    }
    setLoading(true)
    try {
      const res = await getCardList(userId)
      setData(res.list || res.data?.list || res.data || [])
    } catch (err) {
      message.error(err.message || '获取卡片列表失败')
    } finally {
      setLoading(false)
    }
  }

  const columns = [
    { title: 'UUID', dataIndex: 'uuid', key: 'uuid', ellipsis: true, width: 200 },
    {
      title: '稀有度',
      dataIndex: 'rarity',
      key: 'rarity',
      width: 80,
      render: (v) => <Tag color={rarityColor(v)}>{rarityNameMap[v] || v}</Tag>,
    },
    { title: '主题分类', dataIndex: 'themeCategory', key: 'themeCategory' },
    { title: '边框主题', dataIndex: 'borderTheme', key: 'borderTheme' },
    { title: '卡面主题', dataIndex: 'cardTheme', key: 'cardTheme' },
    { title: '学习时长(分)', dataIndex: 'studyDuration', key: 'studyDuration', width: 100 },
    {
      title: '创建时间',
      dataIndex: 'createTime',
      key: 'createTime',
      render: (v) => formatDateTime(v),
    },
    {
      title: '图片',
      dataIndex: 'imageURL',
      key: 'imageURL',
      width: 80,
      render: (v) => v ? <Image src={v} width={40} height={40} style={{ objectFit: 'cover', borderRadius: 4 }} /> : '-',
    },
    {
      title: 'Markdown',
      dataIndex: 'markdown',
      key: 'markdown',
      ellipsis: true,
      render: (v) => v ? v.substring(0, 50) + '...' : '-',
    },
  ]

  return (
    <div>
      <Title level={4} style={{ marginBottom: 24 }}>卡片管理</Title>
      <Card>
        <Space style={{ marginBottom: 16 }} wrap>
          <Input
            placeholder="输入用户ID查询卡片"
            value={userId}
            onChange={e => setUserId(e.target.value)}
            style={{ width: 250 }}
            onPressEnter={handleSearch}
          />
          <Button type="primary" onClick={handleSearch} loading={loading}>查询</Button>
        </Space>
        <Table
          rowKey="uuid"
          loading={loading}
          columns={columns}
          dataSource={data}
          pagination={{ pageSize: 20, showTotal: (t) => `共 ${t} 条` }}
          expandable={{
            expandedRowRender: (record) => (
              <div style={{ padding: 12 }}>
                <p><strong>完整 Markdown 内容：</strong></p>
                <pre style={{ whiteSpace: 'pre-wrap', background: '#f5f5f5', padding: 12, borderRadius: 4 }}>
                  {record.markdown || '-'}
                </pre>
              </div>
            ),
          }}
        />
      </Card>
    </div>
  )
}
