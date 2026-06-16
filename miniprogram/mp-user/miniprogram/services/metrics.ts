import logger from '../utils/logger';

interface RequestMetric {
  url: string;
  method: string;
  startTime: number;
  endTime: number;
  duration: number;
  success: boolean;
  statusCode: number;
  errorMessage: string;
}

interface PageLoadMetric {
  pagePath: string;
  startTime: number;
  endTime: number;
  duration: number;
}

interface MetricsSummary {
  requestCount: number;
  successCount: number;
  failCount: number;
  successRate: string;
  avgDuration: string;
  maxDuration: number;
  minDuration: number;
  pageLoadCount: number;
  avgPageLoadDuration: string;
}

var MAX_REQUEST_METRICS = 100;
var MAX_PAGE_METRICS = 50;
var METRICS_STORAGE_KEY = 'istudyspot_metrics';

class MetricsService {
  private requestMetrics: RequestMetric[];
  private pageLoadMetrics: PageLoadMetric[];
  private pendingPageLoads: Map<string, number>;

  constructor() {
    this.requestMetrics = [];
    this.pageLoadMetrics = [];
    this.pendingPageLoads = new Map();
  }

  startRequest(url: string, method: string): string {
    var traceId = 'req_' + Date.now() + '_' + Math.random().toString(36).substring(2, 8);
    logger.debug('Metrics', '请求开始 [' + traceId + '] ' + method + ' ' + url);
    return traceId;
  }

  endRequest(traceId: string, url: string, method: string, success: boolean, statusCode: number, errorMessage: string, startTime: number): void {
    var endTime = Date.now();
    var duration = endTime - startTime;

    var metric: RequestMetric = {
      url: url,
      method: method,
      startTime: startTime,
      endTime: endTime,
      duration: duration,
      success: success,
      statusCode: statusCode,
      errorMessage: errorMessage
    };

    this.requestMetrics.push(metric);

    if (this.requestMetrics.length > MAX_REQUEST_METRICS) {
      this.requestMetrics = this.requestMetrics.slice(-MAX_REQUEST_METRICS);
    }

    if (success) {
      logger.debug('Metrics', '请求成功 [' + traceId + '] 耗时: ' + duration + 'ms');
    } else {
      logger.warn('Metrics', '请求失败 [' + traceId + '] 耗时: ' + duration + 'ms, 错误: ' + errorMessage);
    }

    this.persistMetrics();
  }

  startPageLoad(pagePath: string): void {
    this.pendingPageLoads.set(pagePath, Date.now());
    logger.debug('Metrics', '页面加载开始: ' + pagePath);
  }

  endPageLoad(pagePath: string): void {
    var startTime = this.pendingPageLoads.get(pagePath);

    if (!startTime) {
      logger.warn('Metrics', '页面加载结束但未找到开始记录: ' + pagePath);
      return;
    }

    var endTime = Date.now();
    var duration = endTime - startTime;

    var metric: PageLoadMetric = {
      pagePath: pagePath,
      startTime: startTime,
      endTime: endTime,
      duration: duration
    };

    this.pageLoadMetrics.push(metric);

    if (this.pageLoadMetrics.length > MAX_PAGE_METRICS) {
      this.pageLoadMetrics = this.pageLoadMetrics.slice(-MAX_PAGE_METRICS);
    }

    this.pendingPageLoads.delete(pagePath);

    logger.info('Metrics', '页面加载完成: ' + pagePath + ', 耗时: ' + duration + 'ms');

    this.persistMetrics();
  }

  getSummary(): MetricsSummary {
    var total = this.requestMetrics.length;
    var successCount = 0;
    var failCount = 0;
    var totalDuration = 0;
    var maxDuration = 0;
    var minDuration = Infinity;

    for (var i = 0; i < this.requestMetrics.length; i++) {
      var m = this.requestMetrics[i];
      if (m.success) {
        successCount++;
      } else {
        failCount++;
      }
      totalDuration += m.duration;
      if (m.duration > maxDuration) maxDuration = m.duration;
      if (m.duration < minDuration) minDuration = m.duration;
    }

    var pageTotalDuration = 0;
    for (var j = 0; j < this.pageLoadMetrics.length; j++) {
      pageTotalDuration += this.pageLoadMetrics[j].duration;
    }

    return {
      requestCount: total,
      successCount: successCount,
      failCount: failCount,
      successRate: total > 0 ? (successCount / total * 100).toFixed(1) + '%' : 'N/A',
      avgDuration: total > 0 ? (totalDuration / total).toFixed(0) + 'ms' : 'N/A',
      maxDuration: maxDuration,
      minDuration: minDuration === Infinity ? 0 : minDuration,
      pageLoadCount: this.pageLoadMetrics.length,
      avgPageLoadDuration: this.pageLoadMetrics.length > 0
        ? (pageTotalDuration / this.pageLoadMetrics.length).toFixed(0) + 'ms'
        : 'N/A'
    };
  }

  getRequestMetrics(): RequestMetric[] {
    return this.requestMetrics.slice();
  }

  getPageLoadMetrics(): PageLoadMetric[] {
    return this.pageLoadMetrics.slice();
  }

  getFailedRequests(): RequestMetric[] {
    return this.requestMetrics.filter(function(m) {
      return !m.success;
    });
  }

  getSlowRequests(thresholdMs: number): RequestMetric[] {
    return this.requestMetrics.filter(function(m) {
      return m.duration > thresholdMs;
    });
  }

  clearMetrics(): void {
    this.requestMetrics = [];
    this.pageLoadMetrics = [];
    this.pendingPageLoads.clear();
    try {
      wx.removeStorageSync(METRICS_STORAGE_KEY);
    } catch (e) {
      // ignore
    }
    logger.info('Metrics', '指标数据已清除');
  }

  private persistMetrics(): void {
    try {
      var data = {
        requests: this.requestMetrics,
        pageLoads: this.pageLoadMetrics
      };
      wx.setStorageSync(METRICS_STORAGE_KEY, JSON.stringify(data));
    } catch (e) {
      // storage write failed
    }
  }

  loadMetrics(): void {
    try {
      var stored = wx.getStorageSync(METRICS_STORAGE_KEY);
      if (stored) {
        var data = JSON.parse(stored);
        if (data && data.requests) {
          this.requestMetrics = data.requests;
        }
        if (data && data.pageLoads) {
          this.pageLoadMetrics = data.pageLoads;
        }
      }
    } catch (e) {
      this.requestMetrics = [];
      this.pageLoadMetrics = [];
    }
    logger.info('Metrics', '指标数据已加载, 请求数: ' + this.requestMetrics.length + ', 页面加载数: ' + this.pageLoadMetrics.length);
  }

  logSummary(): void {
    var summary = this.getSummary();
    logger.info('Metrics', '指标摘要', summary);
  }
}

var metrics = new MetricsService();

export default metrics;
export type { RequestMetric, PageLoadMetric, MetricsSummary };
