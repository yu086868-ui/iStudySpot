import { useEffect, useMemo, useState } from 'react'
import {
  Button,
  Card,
  Descriptions,
  Form,
  Input,
  InputNumber,
  message,
  Modal,
  Popconfirm,
  Select,
  Space,
  Table,
  Tag,
  Typography,
} from 'antd'
import { DeleteOutlined, EyeOutlined, PlusOutlined } from '@ant-design/icons'
import { getRuleList } from '../../api/rules'

const { Title } = Typography
const { TextArea } = Input

const CATEGORY_MAP = {
  booking: '预约',
  checkin: '签到',
  leave: '暂离',
  violation: '违规',
  civilized: '文明使用',
  faq: '常见问题',
  usage: '使用',
  penalty: '处罚',
  general: '通用',
}

const TYPE_MAP = {
  faq: '常见问题',
  rule: '规则',
}

function normalizeRule(rule) {
  const category = rule.category || 'general'
  const type = rule.type || (category === 'faq' ? 'faq' : 'rule')

  return {
    id: Number(rule.id),
    title: rule.title || '',
    content: rule.content || '',
    category,
    categoryLabel: rule.categoryLabel || CATEGORY_MAP[category] || category,
    priority: Number(rule.priority || 0),
    type,
  }
}

export default function RuleList() {
  const [loading, setLoading] = useState(false)
  const [rules, setRules] = useState([])
  const [categoryFilter, setCategoryFilter] = useState(undefined)
  const [detailVisible, setDetailVisible] = useState(false)
  const [createVisible, setCreateVisible] = useState(false)
  const [currentRule, setCurrentRule] = useState(null)
  const [form] = Form.useForm()

  useEffect(() => {
    loadData()
  }, [])

  const loadData = async () => {
    setLoading(true)
    try {
      const res = await getRuleList({})
      const list = Array.isArray(res.data) ? res.data.map(normalizeRule) : []
      setRules(list)
    } catch (err) {
      message.error(err.message || '获取规则列表失败')
    } finally {
      setLoading(false)
    }
  }

  const filteredRules = useMemo(() => {
    return rules.filter((rule) => !categoryFilter || rule.category === categoryFilter)
  }, [rules, categoryFilter])

  const handleOpenDetail = (rule) => {
    setCurrentRule(rule)
    setDetailVisible(true)
  }

  const handleDelete = (ruleId) => {
    setRules((prev) => prev.filter((item) => item.id !== ruleId))
    if (currentRule?.id === ruleId) {
      setCurrentRule(null)
      setDetailVisible(false)
    }
    message.success('规则已删除')
  }

  const handleCreate = async () => {
    try {
      const values = await form.validateFields()
      const nextId = rules.reduce((max, item) => Math.max(max, item.id), 0) + 1
      const nextRule = normalizeRule({
        id: nextId,
        title: values.title.trim(),
        content: values.content.trim(),
        category: values.category,
        categoryLabel: CATEGORY_MAP[values.category] || values.category,
        priority: values.priority,
        type: values.type,
      })

      setRules((prev) => [nextRule, ...prev])
      setCreateVisible(false)
      form.resetFields()
      message.success('规则已新增')
    } catch {
      // Form validation handles field errors.
    }
  }

  const columns = [
    { title: 'ID', dataIndex: 'id', key: 'id', width: 80 },
    { title: '标题', dataIndex: 'title', key: 'title' },
    {
      title: '分类',
      dataIndex: 'category',
      key: 'category',
      render: (_, record) => <Tag color="blue">{record.categoryLabel}</Tag>,
    },
    {
      title: '内容',
      dataIndex: 'content',
      key: 'content',
      ellipsis: true,
    },
    {
      title: '优先级',
      dataIndex: 'priority',
      key: 'priority',
      width: 90,
    },
    {
      title: '类型',
      dataIndex: 'type',
      key: 'type',
      render: (value) => <Tag>{TYPE_MAP[value] || value}</Tag>,
    },
    {
      title: '操作',
      key: 'action',
      width: 180,
      render: (_, record) => (
        <Space size="small">
          <Button type="link" icon={<EyeOutlined />} onClick={() => handleOpenDetail(record)}>
            详情
          </Button>
          <Popconfirm
            title="删除规则"
            description="确认继续吗？"
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
      <Title level={4} style={{ marginBottom: 24 }}>规则内容</Title>
      <Card>
        <Space style={{ marginBottom: 16 }} wrap>
          <Select
            placeholder="按分类筛选"
            value={categoryFilter}
            onChange={setCategoryFilter}
            allowClear
            style={{ width: 160 }}
            options={Object.entries(CATEGORY_MAP).map(([value, label]) => ({ value, label }))}
          />
          <Button type="primary" icon={<PlusOutlined />} onClick={() => setCreateVisible(true)}>
            新增规则
          </Button>
          <Button onClick={loadData}>重置为后端初始内容</Button>
        </Space>

        <Table
          rowKey="id"
          loading={loading}
          columns={columns}
          dataSource={filteredRules}
          pagination={{
            pageSize: 20,
            showTotal: (total) => `共 ${total} 条`,
          }}
        />
      </Card>

      <Modal
        title="规则详情"
        open={detailVisible}
        onCancel={() => setDetailVisible(false)}
        footer={null}
        width={760}
      >
        {currentRule && (
          <Descriptions bordered column={1} size="small">
            <Descriptions.Item label="ID">{currentRule.id}</Descriptions.Item>
            <Descriptions.Item label="标题">{currentRule.title}</Descriptions.Item>
            <Descriptions.Item label="分类">{currentRule.categoryLabel}</Descriptions.Item>
            <Descriptions.Item label="优先级">{currentRule.priority}</Descriptions.Item>
            <Descriptions.Item label="类型">{TYPE_MAP[currentRule.type] || currentRule.type}</Descriptions.Item>
            <Descriptions.Item label="正文">
              <div style={{ whiteSpace: 'pre-wrap' }}>{currentRule.content}</div>
            </Descriptions.Item>
          </Descriptions>
        )}
      </Modal>

      <Modal
        title="新增规则"
        open={createVisible}
        onCancel={() => {
          setCreateVisible(false)
          form.resetFields()
        }}
        onOk={handleCreate}
        okText="保存"
        cancelText="取消"
      >
        <Form
          form={form}
          layout="vertical"
          initialValues={{
            category: 'general',
            type: 'rule',
            priority: 1,
          }}
        >
          <Form.Item
            name="title"
            label="标题"
            rules={[{ required: true, message: '请输入标题' }]}
          >
            <Input maxLength={100} />
          </Form.Item>
          <Form.Item
            name="category"
            label="分类"
            rules={[{ required: true, message: '请选择分类' }]}
          >
            <Select options={Object.entries(CATEGORY_MAP).map(([value, label]) => ({ value, label }))} />
          </Form.Item>
          <Form.Item
            name="type"
            label="类型"
            rules={[{ required: true, message: '请选择类型' }]}
          >
            <Select
              options={[
                { value: 'rule', label: '规则' },
                { value: 'faq', label: '常见问题' },
              ]}
            />
          </Form.Item>
          <Form.Item
            name="priority"
            label="优先级"
            rules={[{ required: true, message: '请输入优先级' }]}
          >
            <InputNumber min={1} max={999} style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item
            name="content"
            label="正文"
            rules={[{ required: true, message: '请输入正文' }]}
          >
            <TextArea rows={6} maxLength={2000} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}
