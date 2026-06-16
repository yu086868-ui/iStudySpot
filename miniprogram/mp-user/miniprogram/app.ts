import logger from './utils/logger';
import errorMonitor from './utils/error-monitor';
import metrics from './services/metrics';
import healthCheck from './services/health-check';
import { authApi } from './services/auth';
import store from './utils/store';

App<IAppOption>({
  globalData: {},
  onLaunch() {
    logger.init();
    logger.info('App', '应用启动');

    errorMonitor.init();
    logger.info('App', '错误监控已初始化');

    metrics.loadMetrics();
    logger.info('App', '指标收集已加载');

    var logs = wx.getStorageSync('logs') || [];
    logs.unshift(Date.now());
    wx.setStorageSync('logs', logs);

    // 自动微信登录
    if (!store.isLoggedIn()) {
      authApi.loginWithWx().then(function(res) {
        if (res.code === 200) {
          logger.info('App', '微信自动登录成功');
        } else {
          logger.warn('App', '微信自动登录失败: ' + res.message);
        }
      });
    }

    healthCheck.runAllChecks().then(function(report) {
      if (report.overallStatus === 'unhealthy') {
        logger.warn('App', '应用健康检查: 存在异常项', {
          unhealthyChecks: report.checks.filter(function(c) { return c.status === 'unhealthy'; }).map(function(c) { return c.name; })
        });
      } else {
        logger.info('App', '应用健康检查: 状态正常');
      }
    });
  },
  onError(err: string) {
    errorMonitor.handleJsError(err);
  },
  onUnhandledRejection(rejection: WechatMiniprogram.OnUnhandledRejectionCallbackResult) {
    errorMonitor.handlePromiseRejection(rejection);
  },
  onPageNotFound() {
    logger.warn('App', '页面未找到');
  }
});
