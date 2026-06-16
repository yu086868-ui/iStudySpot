import { useEffect, useState } from 'react'
import { Button, Card, Col, Descriptions, message, Progress, Row, Spin, Tag, Typography } from 'antd'
import { CheckCircleOutlined, CloseCircleOutlined, QuestionCircleOutlined, ReloadOutlined } from '@ant-design/icons'
import { getHealth, getReadiness } from '../../api/health'
import { formatDateTime } from '../../utils'

const { Title } = Typography

const healthyStatuses = ['up', 'healthy', 'ready', 'connected', 'available', 'sufficient']

function isHealthy(status) {
  if (status === true || status === 1) return true
  if (typeof status === 'string') return healthyStatuses.includes(status.toLowerCase())
  return false
}

function StatusIcon({ status }) {
  if (isHealthy(status)) return <CheckCircleOutlined style={{ color: '#52c41a', fontSize: 18 }} />
  if (status === false || status === 0 || status === 'DOWN' || status === 'unhealthy') {
    return <CloseCircleOutlined style={{ color: '#f5222d', fontSize: 18 }} />
  }
  return <QuestionCircleOutlined style={{ color: '#faad14', fontSize: 18 }} />
}

function StatusTag({ status }) {
  const healthy = isHealthy(status)
  const label = typeof status === 'boolean' ? (status ? '正常' : '异常') : String(status)
  return (
    <Tag color={healthy ? 'green' : 'red'} icon={healthy ? <CheckCircleOutlined /> : <CloseCircleOutlined />}>
      {label}
    </Tag>
  )
}

export default function HealthCheck() {
  const [loading, setLoading] = useState(false)
  const [health, setHealth] = useState(null)
  const [readiness, setReadiness] = useState(null)

  useEffect(() => {
    loadHealth()
  }, [])

  const loadHealth = async () => {
    setLoading(true)
    try {
      const [healthRes, readinessRes] = await Promise.allSettled([getHealth(), getReadiness()])
      if (healthRes.status === 'fulfilled') setHealth(healthRes.value.data)
      if (readinessRes.status === 'fulfilled') setReadiness(readinessRes.value.data)
    } catch (err) {
      message.error(err.message || '获取健康状态失败')
    } finally {
      setLoading(false)
    }
  }

  const renderCheck = (name, status) => (
    <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 8 }}>
      <StatusIcon status={status} />
      <span>{name}:</span>
      <StatusTag status={status} />
    </div>
  )

  return (
    <div>
      <Title level={4} style={{ marginBottom: 24 }}>系统健康监控</Title>
      <Spin spinning={loading}>
        <Row gutter={[16, 16]}>
          <Col xs={24} lg={12}>
            <Card title="健康状态" extra={<Button icon={<ReloadOutlined />} onClick={loadHealth} loading={loading}>刷新</Button>}>
              {health ? (
                <Descriptions bordered column={1} size="small">
                  <Descriptions.Item label="状态"><StatusTag status={health.status} /></Descriptions.Item>
                  <Descriptions.Item label="版本">{health.version || '-'}</Descriptions.Item>
                  <Descriptions.Item label="应用名">{health.appName || '-'}</Descriptions.Item>
                  <Descriptions.Item label="端口">{health.serverPort || '-'}</Descriptions.Item>
                  <Descriptions.Item label="服务">{health.service || '-'}</Descriptions.Item>
                  <Descriptions.Item label="环境">{health.environment || '-'}</Descriptions.Item>
                  <Descriptions.Item label="时间戳">{formatDateTime(health.timestamp)}</Descriptions.Item>
                </Descriptions>
              ) : (
                <div style={{ textAlign: 'center', padding: 40, color: '#999' }}>暂无数据</div>
              )}
            </Card>
          </Col>
          <Col xs={24} lg={12}>
            <Card title="就绪检查">
              {readiness ? (
                <div>
                  {renderCheck('数据库', readiness.checks?.database)}
                  {renderCheck('服务', readiness.checks?.services)}
                  {renderCheck('内存', readiness.checks?.memory)}
                  {readiness.checks?.memoryUsage !== undefined && (
                    <div style={{ marginTop: 16 }}>
                      <Progress
                        percent={Math.round(readiness.checks.memoryUsage)}
                        status={readiness.checks.memoryUsage > 90 ? 'exception' : readiness.checks.memoryUsage > 70 ? 'active' : 'normal'}
                      />
                      <span style={{ color: '#999', fontSize: 12 }}>内存使用率</span>
                    </div>
                  )}
                  <Descriptions bordered column={1} size="small" style={{ marginTop: 16 }}>
                    <Descriptions.Item label="状态"><StatusTag status={readiness.status} /></Descriptions.Item>
                    <Descriptions.Item label="时间戳">{formatDateTime(readiness.timestamp)}</Descriptions.Item>
                  </Descriptions>
                </div>
              ) : (
                <div style={{ textAlign: 'center', padding: 40, color: '#999' }}>暂无数据</div>
              )}
            </Card>
          </Col>
        </Row>
      </Spin>
    </div>
  )
}
