jest.mock('../../../miniprogram/utils/logger', () => ({
  __esModule: true,
  default: {
    debug: jest.fn(),
    info: jest.fn(),
    warn: jest.fn(),
    error: jest.fn()
  }
}));

import metrics from '../../../miniprogram/services/metrics';

beforeEach(() => {
  metrics.clearMetrics();
  jest.clearAllMocks();
});

describe('MetricsService', () => {
  describe('startRequest', () => {
    it('返回有效的 traceId', () => {
      const traceId = metrics.startRequest('/api/test', 'GET');

      expect(traceId).toMatch(/^req_\d+_[a-z0-9]+$/);
    });

    it('不同调用返回不同的 traceId', () => {
      const traceId1 = metrics.startRequest('/api/test1', 'GET');
      const traceId2 = metrics.startRequest('/api/test2', 'POST');

      expect(traceId1).not.toBe(traceId2);
    });
  });

  describe('endRequest', () => {
    it('记录请求指标', () => {
      const startTime = Date.now() - 100;
      const traceId = metrics.startRequest('/api/test', 'GET');
      metrics.endRequest(traceId, '/api/test', 'GET', true, 200, '', startTime);

      const requestMetrics = metrics.getRequestMetrics();
      expect(requestMetrics).toHaveLength(1);
      expect(requestMetrics[0].url).toBe('/api/test');
      expect(requestMetrics[0].method).toBe('GET');
      expect(requestMetrics[0].success).toBe(true);
      expect(requestMetrics[0].statusCode).toBe(200);
      expect(requestMetrics[0].errorMessage).toBe('');
      expect(requestMetrics[0].duration).toBeGreaterThanOrEqual(0);
    });

    it('记录失败请求指标', () => {
      const startTime = Date.now() - 200;
      const traceId = metrics.startRequest('/api/fail', 'POST');
      metrics.endRequest(traceId, '/api/fail', 'POST', false, 500, 'Server Error', startTime);

      const requestMetrics = metrics.getRequestMetrics();
      expect(requestMetrics).toHaveLength(1);
      expect(requestMetrics[0].success).toBe(false);
      expect(requestMetrics[0].statusCode).toBe(500);
      expect(requestMetrics[0].errorMessage).toBe('Server Error');
    });

    it('请求指标超过 MAX_REQUEST_METRICS 时自动截断', () => {
      for (let i = 0; i < 110; i++) {
        const traceId = metrics.startRequest(`/api/test/${i}`, 'GET');
        metrics.endRequest(traceId, `/api/test/${i}`, 'GET', true, 200, '', Date.now() - 100);
      }

      const requestMetrics = metrics.getRequestMetrics();
      expect(requestMetrics).toHaveLength(100);
    });
  });

  describe('startPageLoad 和 endPageLoad', () => {
    it('记录页面加载指标', () => {
      metrics.startPageLoad('/pages/test/test');
      metrics.endPageLoad('/pages/test/test');

      const pageLoadMetrics = metrics.getPageLoadMetrics();
      expect(pageLoadMetrics).toHaveLength(1);
      expect(pageLoadMetrics[0].pagePath).toBe('/pages/test/test');
      expect(pageLoadMetrics[0].duration).toBeGreaterThanOrEqual(0);
    });

    it('endPageLoad 未找到开始记录时不记录', () => {
      metrics.endPageLoad('/pages/not-started/not-started');

      const pageLoadMetrics = metrics.getPageLoadMetrics();
      expect(pageLoadMetrics).toHaveLength(0);
    });

    it('页面加载指标超过 MAX_PAGE_METRICS 时自动截断', () => {
      for (let i = 0; i < 60; i++) {
        const path = `/pages/test/${i}`;
        metrics.startPageLoad(path);
        metrics.endPageLoad(path);
      }

      const pageLoadMetrics = metrics.getPageLoadMetrics();
      expect(pageLoadMetrics).toHaveLength(50);
    });
  });

  describe('getSummary', () => {
    it('无指标时返回默认值', () => {
      const summary = metrics.getSummary();

      expect(summary.requestCount).toBe(0);
      expect(summary.successCount).toBe(0);
      expect(summary.failCount).toBe(0);
      expect(summary.successRate).toBe('N/A');
      expect(summary.avgDuration).toBe('N/A');
      expect(summary.maxDuration).toBe(0);
      expect(summary.minDuration).toBe(0);
      expect(summary.pageLoadCount).toBe(0);
      expect(summary.avgPageLoadDuration).toBe('N/A');
    });

    it('返回正确的统计数据', () => {
      const now = Date.now();
      metrics.endRequest('t1', '/api/ok1', 'GET', true, 200, '', now - 100);
      metrics.endRequest('t2', '/api/ok2', 'GET', true, 200, '', now - 50);
      metrics.endRequest('t3', '/api/fail', 'POST', false, 500, 'Error', now - 200);

      metrics.startPageLoad('/pages/test');
      metrics.endPageLoad('/pages/test');

      const summary = metrics.getSummary();

      expect(summary.requestCount).toBe(3);
      expect(summary.successCount).toBe(2);
      expect(summary.failCount).toBe(1);
      expect(summary.successRate).toBe('66.7%');
      expect(summary.pageLoadCount).toBe(1);
      expect(summary.avgPageLoadDuration).toMatch(/^\d+ms$/);
    });
  });

  describe('getFailedRequests', () => {
    it('只返回失败请求', () => {
      const now = Date.now();
      metrics.endRequest('t1', '/api/ok', 'GET', true, 200, '', now - 100);
      metrics.endRequest('t2', '/api/fail', 'POST', false, 500, 'Error', now - 200);

      const failed = metrics.getFailedRequests();
      expect(failed).toHaveLength(1);
      expect(failed[0].success).toBe(false);
      expect(failed[0].statusCode).toBe(500);
    });
  });

  describe('getSlowRequests', () => {
    it('只返回超过阈值的请求', () => {
      const now = Date.now();
      metrics.endRequest('t1', '/api/fast', 'GET', true, 200, '', now - 10);
      metrics.endRequest('t2', '/api/slow', 'GET', true, 200, '', now - 5000);

      const slow = metrics.getSlowRequests(1000);
      expect(slow).toHaveLength(1);
      expect(slow[0].url).toBe('/api/slow');
    });
  });

  describe('clearMetrics', () => {
    it('清除所有指标', () => {
      const now = Date.now();
      metrics.endRequest('t1', '/api/test', 'GET', true, 200, '', now - 100);
      metrics.startPageLoad('/pages/test');
      metrics.endPageLoad('/pages/test');

      metrics.clearMetrics();

      expect(metrics.getRequestMetrics()).toHaveLength(0);
      expect(metrics.getPageLoadMetrics()).toHaveLength(0);
      expect(wx.removeStorageSync).toHaveBeenCalledWith('istudyspot_metrics');
    });
  });

  describe('loadMetrics', () => {
    it('从 storage 加载指标', () => {
      const storedData = JSON.stringify({
        requests: [
          { url: '/api/loaded', method: 'GET', startTime: 1000, endTime: 2000, duration: 1000, success: true, statusCode: 200, errorMessage: '' }
        ],
        pageLoads: [
          { pagePath: '/pages/loaded', startTime: 1000, endTime: 2000, duration: 1000 }
        ]
      });
      (wx.getStorageSync as jest.Mock).mockImplementation((key: string) => {
        if (key === 'istudyspot_metrics') return storedData;
        return '';
      });

      metrics.loadMetrics();

      expect(metrics.getRequestMetrics()).toHaveLength(1);
      expect(metrics.getRequestMetrics()[0].url).toBe('/api/loaded');
      expect(metrics.getPageLoadMetrics()).toHaveLength(1);
      expect(metrics.getPageLoadMetrics()[0].pagePath).toBe('/pages/loaded');
    });

    it('处理无效数据', () => {
      (wx.getStorageSync as jest.Mock).mockImplementation((key: string) => {
        if (key === 'istudyspot_metrics') return 'invalid json{{{';
        return '';
      });

      metrics.loadMetrics();

      expect(metrics.getRequestMetrics()).toHaveLength(0);
      expect(metrics.getPageLoadMetrics()).toHaveLength(0);
    });
  });
});
