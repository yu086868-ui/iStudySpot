import logger from '../utils/logger';
import request from '../utils/request';

type HealthStatus = 'healthy' | 'degraded' | 'unhealthy';

interface HealthCheckResult {
  name: string;
  status: HealthStatus;
  message: string;
  timestamp: number;
  duration: number;
}

interface HealthReport {
  overallStatus: HealthStatus;
  checks: HealthCheckResult[];
  timestamp: number;
}

class HealthCheckService {
  private lastReport: HealthReport | null;

  constructor() {
    this.lastReport = null;
  }

  async runAllChecks(): Promise<HealthReport> {
    logger.info('HealthCheck', '开始运行健康检查');

    var checks: HealthCheckResult[] = [];
    checks.push(await this.checkNetworkStatus());
    checks.push(await this.checkLoginStatus());
    checks.push(await this.checkLocalStorage());
    checks.push(await this.checkServerAccessibility());

    var overallStatus = this.calculateOverallStatus(checks);

    var report: HealthReport = {
      overallStatus: overallStatus,
      checks: checks,
      timestamp: Date.now()
    };

    this.lastReport = report;

    logger.info('HealthCheck', '健康检查完成', {
      overallStatus: overallStatus,
      checkCount: checks.length,
      unhealthyCount: checks.filter(function(c) { return c.status === 'unhealthy'; }).length
    });

    return report;
  }

  private async checkNetworkStatus(): Promise<HealthCheckResult> {
    var start = Date.now();

    return new Promise(function(resolve) {
      wx.getNetworkType({
        success: function(res) {
          var networkType = res.networkType;
          var isOffline = networkType === 'none';
          var status: HealthStatus = isOffline ? 'unhealthy' : 'healthy';
          var message = isOffline
            ? '网络不可用，当前无网络连接'
            : '网络正常，类型: ' + networkType;

          logger.info('HealthCheck', '网络状态检查: ' + message);

          resolve({
            name: 'network',
            status: status,
            message: message,
            timestamp: Date.now(),
            duration: Date.now() - start
          });
        },
        fail: function() {
          logger.warn('HealthCheck', '网络状态检查失败');

          resolve({
            name: 'network',
            status: 'degraded',
            message: '无法获取网络状态',
            timestamp: Date.now(),
            duration: Date.now() - start
          });
        }
      });
    });
  }

  private async checkLoginStatus(): Promise<HealthCheckResult> {
    var start = Date.now();
    var token = wx.getStorageSync('access_token') || '';
    var isLoggedIn = !!token;

    var status: HealthStatus = isLoggedIn ? 'healthy' : 'degraded';
    var message = isLoggedIn
      ? '用户已登录'
      : '用户未登录，部分功能不可用';

    logger.info('HealthCheck', '登录状态检查: ' + message);

    return {
      name: 'login',
      status: status,
      message: message,
      timestamp: Date.now(),
      duration: Date.now() - start
    };
  }

  private async checkLocalStorage(): Promise<HealthCheckResult> {
    var start = Date.now();

    try {
      var storageInfo = wx.getStorageInfoSync();
      var usedPercent = storageInfo.keys.length / 100;
      var status: HealthStatus;

      if (usedPercent > 0.9) {
        status = 'unhealthy';
      } else if (usedPercent > 0.7) {
        status = 'degraded';
      } else {
        status = 'healthy';
      }

      var message = '存储使用: '
        + storageInfo.keys.length + ' 个键, '
        + '当前占用: ' + storageInfo.currentSize + 'KB, '
        + '限制: ' + storageInfo.limitSize + 'KB';

      logger.info('HealthCheck', '本地存储检查: ' + message);

      return {
        name: 'storage',
        status: status,
        message: message,
        timestamp: Date.now(),
        duration: Date.now() - start
      };
    } catch (e) {
      logger.error('HealthCheck', '本地存储检查异常', e);

      return {
        name: 'storage',
        status: 'unhealthy',
        message: '本地存储检查失败',
        timestamp: Date.now(),
        duration: Date.now() - start
      };
    }
  }

  private async checkServerAccessibility(): Promise<HealthCheckResult> {
    var start = Date.now();

    try {
      var response = await request.get<unknown>('/health', undefined, false);
      var duration = Date.now() - start;

      if (response && response.code === 200) {
        logger.info('HealthCheck', '服务器可达性检查: 正常, 耗时 ' + duration + 'ms');
        return {
          name: 'server',
          status: 'healthy',
          message: '服务器可达, 耗时: ' + duration + 'ms',
          timestamp: Date.now(),
          duration: duration
        };
      }

      logger.warn('HealthCheck', '服务器可达性检查: 响应异常', response);
      return {
        name: 'server',
        status: 'degraded',
        message: '服务器响应异常, code: ' + (response && response.code),
        timestamp: Date.now(),
        duration: duration
      };
    } catch (e) {
      var duration = Date.now() - start;
      logger.error('HealthCheck', '服务器可达性检查: 无法连接', e);

      return {
        name: 'server',
        status: 'unhealthy',
        message: '服务器不可达, 耗时: ' + duration + 'ms',
        timestamp: Date.now(),
        duration: duration
      };
    }
  }

  private calculateOverallStatus(checks: HealthCheckResult[]): HealthStatus {
    var hasUnhealthy = false;
    var hasDegraded = false;

    for (var i = 0; i < checks.length; i++) {
      if (checks[i].status === 'unhealthy') {
        hasUnhealthy = true;
      } else if (checks[i].status === 'degraded') {
        hasDegraded = true;
      }
    }

    if (hasUnhealthy) return 'unhealthy';
    if (hasDegraded) return 'degraded';
    return 'healthy';
  }

  getLastReport(): HealthReport | null {
    return this.lastReport;
  }

  getQuickNetworkStatus(): Promise<string> {
    return new Promise(function(resolve) {
      wx.getNetworkType({
        success: function(res) {
          resolve(res.networkType);
        },
        fail: function() {
          resolve('unknown');
        }
      });
    });
  }
}

var healthCheck = new HealthCheckService();

export default healthCheck;
export type { HealthStatus, HealthCheckResult, HealthReport };
