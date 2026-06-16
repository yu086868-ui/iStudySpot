import { useEffect, useState } from 'react'
import { Button, Card, Descriptions, Empty, message, Modal, Space, Table, Typography } from 'antd'
import { EyeOutlined } from '@ant-design/icons'
import { getStudyRoomGuideDetail, getStudyRoomGuides } from '../../api/guides'

const { Title, Paragraph } = Typography

function TextBlock({ value }) {
  if (!value) return '-'
  return <Paragraph style={{ whiteSpace: 'pre-wrap', marginBottom: 0 }}>{value}</Paragraph>
}

export default function StudyRoomGuideList() {
  const [loading, setLoading] = useState(false)
  const [data, setData] = useState([])
  const [detailLoading, setDetailLoading] = useState(false)
  const [detailVisible, setDetailVisible] = useState(false)
  const [currentGuide, setCurrentGuide] = useState(null)

  useEffect(() => {
    loadData()
  }, [])

  const loadData = async () => {
    setLoading(true)
    try {
      const res = await getStudyRoomGuides()
      setData(res.data || [])
    } catch (err) {
      message.error(err.message || '获取场馆导览列表失败')
    } finally {
      setLoading(false)
    }
  }

  const openDetail = async (studyRoomId) => {
    setDetailLoading(true)
    setDetailVisible(true)
    try {
      const res = await getStudyRoomGuideDetail(studyRoomId)
      setCurrentGuide(res.data)
    } catch (err) {
      message.error(err.message || '获取场馆导览详情失败')
      setCurrentGuide(null)
    } finally {
      setDetailLoading(false)
    }
  }

  const columns = [
    { title: '场馆ID', dataIndex: 'studyRoomId', key: 'studyRoomId', width: 100 },
    { title: '场馆名称', dataIndex: 'studyRoomName', key: 'studyRoomName', ellipsis: true },
    { title: '地址', dataIndex: 'address', key: 'address', ellipsis: true },
    {
      title: '开放时间',
      key: 'openTime',
      render: (_, record) => `${record.openTime || '-'} ~ ${record.closeTime || '-'}`,
    },
    { title: '简介', dataIndex: 'description', key: 'description', ellipsis: true },
    {
      title: '操作',
      key: 'action',
      width: 120,
      render: (_, record) => (
        <Button type="link" icon={<EyeOutlined />} onClick={() => openDetail(record.studyRoomId)}>
          详情
        </Button>
      ),
    },
  ]

  return (
    <div>
      <Title level={4} style={{ marginBottom: 24 }}>场馆导览</Title>
      <Card>
        <Space style={{ marginBottom: 16 }}>
          <Button type="primary" onClick={loadData}>刷新</Button>
        </Space>
        <Table
          rowKey="studyRoomId"
          loading={loading}
          columns={columns}
          dataSource={data}
          locale={{ emptyText: <Empty description="暂无场馆导览数据" /> }}
          pagination={{ pageSize: 20, showTotal: (total) => `共 ${total} 条` }}
        />
      </Card>

      <Modal
        title="场馆导览详情"
        open={detailVisible}
        onCancel={() => setDetailVisible(false)}
        footer={null}
        width={760}
      >
        <Card loading={detailLoading} bordered={false}>
          {currentGuide ? (
            <Descriptions bordered column={1} size="small">
              <Descriptions.Item label="场馆ID">{currentGuide.studyRoomId}</Descriptions.Item>
              <Descriptions.Item label="场馆名称">{currentGuide.studyRoomName}</Descriptions.Item>
              <Descriptions.Item label="地址">{currentGuide.address || '-'}</Descriptions.Item>
              <Descriptions.Item label="开放时间">
                {currentGuide.openTime || '-'} ~ {currentGuide.closeTime || '-'}
              </Descriptions.Item>
              <Descriptions.Item label="场馆描述"><TextBlock value={currentGuide.description} /></Descriptions.Item>
              <Descriptions.Item label="联系方式"><TextBlock value={currentGuide.contactInfo} /></Descriptions.Item>
              <Descriptions.Item label="学习区域"><TextBlock value={currentGuide.learningAreas} /></Descriptions.Item>
              <Descriptions.Item label="便利设施"><TextBlock value={currentGuide.convenienceFacilities} /></Descriptions.Item>
              <Descriptions.Item label="交通指南"><TextBlock value={currentGuide.transportationGuide} /></Descriptions.Item>
            </Descriptions>
          ) : (
            <Empty description="暂无详情" />
          )}
        </Card>
      </Modal>
    </div>
  )
}
