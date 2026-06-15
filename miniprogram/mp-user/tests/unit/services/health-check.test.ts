jest.mock('../../../miniprogram/utils/logger', () => ({
  __esModule: true,
  default: {
    debug: jest.fn(),
    info: jest.fn(),
    warn: jest.fn(),
    error: jest.fn()
  }
}));

jest.mock('../../../miniprogram/utils/request', () => ({
  __esModule: true,
  default: {
    get: jest.fn(),
    post: jest.fn(),
    put: jest.fn(),
    delete: jest.fn()
  }
}));

jest.mock('../../../miniprogram/utils/store', () => ({
  __esModule: true,
  default: {
    isLoggedIn: jest.fn()
  }
}));

import healthCheck from '../../../miniprogram/services/health-check';
import request from '../../../miniprogram/utils/request';
import store from '../../../miniprogram/utils/store';
import type { HealthCheckResult } from '../../../miniprogram/services/health-check';

const mockedRequest = request as jest.Mocked<typeof request>;
const mockedStore = store as jest.Mocked<typeof store>;

function setupDefaultMocks() {
  (wx as any).getNetworkType = jest.fn().mockImplementation(({ success }: any) => {
    success({ networkType: 'wifi' });
  });
  mockedStore.isLoggedIn.mockReturnValue(true);
  (wx.getStorageInfoSync as jest.Mock).mockReturnValue({
    keys: new Array(10).fill('key'),
    currentSize: 100,
    limitSize: 10240
  });
  mockedRequest.get.mockResolvedValue({ code: 200, data: null });
}

beforeEach(() => {
  jest.clearAllMocks();
  setupDefaultMocks();
});

function findCheck(checks: HealthCheckResult[], name: string): HealthCheckResult {
  return checks.find(c => c.name === name)!;
}

describe('HealthCheckService', () => {
  describe('runAllChecks - 网络检查', () => {
    it('网络正常时返回 healthy', async () => {
      (wx as any).getNetworkType = jest.fn().mockImplementation(({ success }: any) => {
        success({ networkType: 'wifi' });
      });

      const report = await healthCheck.runAllChecks();
      const networkCheck = findCheck(report.checks, 'network');

      expect(networkCheck.status).toBe('healthy');
      expect(networkCheck.message).toContain('wifi');
    });

    it('网络不可用时返回 unhealthy', async () => {
      (wx as any).getNetworkType = jest.fn().mockImplementation(({ success }: any) => {
        success({ networkType: 'none' });
      });

      const report = await healthCheck.runAllChecks();
      const networkCheck = findCheck(report.checks, 'network');

      expect(networkCheck.status).toBe('unhealthy');
      expect(networkCheck.message).toContain('网络不可用');
    });

    it('getNetworkType 失败时返回 degraded', async () => {
      (wx as any).getNetworkType = jest.fn().mockImplementation(({ fail }: any) => {
        fail();
      });

      const report = await healthCheck.runAllChecks();
      const networkCheck = findCheck(report.checks, 'network');

      expect(networkCheck.status).toBe('degraded');
      expect(networkCheck.message).toContain('无法获取网络状态');
    });
  });

  describe('runAllChecks - 登录状态检查', () => {
    it('用户已登录时 login 状态为 healthy', async () => {
      mockedStore.isLoggedIn.mockReturnValue(true);

      const report = await healthCheck.runAllChecks();
      const loginCheck = findCheck(report.checks, 'login');

      expect(loginCheck.status).toBe('healthy');
      expect(loginCheck.message).toContain('用户已登录');
    });

    it('用户未登录时 login 状态为 degraded', async () => {
      mockedStore.isLoggedIn.mockReturnValue(false);

      const report = await healthCheck.runAllChecks();
      const loginCheck = findCheck(report.checks, 'login');

      expect(loginCheck.status).toBe('degraded');
      expect(loginCheck.message).toContain('用户未登录');
    });
  });

  describe('runAllChecks - 存储检查', () => {
    it('存储使用率低时为 healthy', async () => {
      (wx.getStorageInfoSync as jest.Mock).mockReturnValue({
        keys: new Array(10).fill('key'),
        currentSize: 100,
        limitSize: 10240
      });

      const report = await healthCheck.runAllChecks();
      const storageCheck = findCheck(report.checks, 'storage');

      expect(storageCheck.status).toBe('healthy');
    });

    it('存储使用率高时为 degraded', async () => {
      (wx.getStorageInfoSync as jest.Mock).mockReturnValue({
        keys: new Array(80).fill('key'),
        currentSize: 8000,
        limitSize: 10240
      });

      const report = await healthCheck.runAllChecks();
      const storageCheck = findCheck(report.checks, 'storage');

      expect(storageCheck.status).toBe('degraded');
    });

    it('存储使用率极高时为 unhealthy', async () => {
      (wx.getStorageInfoSync as jest.Mock).mockReturnValue({
        keys: new Array(95).fill('key'),
        currentSize: 10000,
        limitSize: 10240
      });

      const report = await healthCheck.runAllChecks();
      const storageCheck = findCheck(report.checks, 'storage');

      expect(storageCheck.status).toBe('unhealthy');
    });
  });

  describe('runAllChecks - 服务器可达性检查', () => {
    it('服务器可达时为 healthy', async () => {
      mockedRequest.get.mockResolvedValue({ code: 200, data: null });

      const report = await healthCheck.runAllChecks();
      const serverCheck = findCheck(report.checks, 'server');

      expect(serverCheck.status).toBe('healthy');
      expect(serverCheck.message).toContain('服务器可达');
    });

    it('服务器响应异常时为 degraded', async () => {
      mockedRequest.get.mockResolvedValue({ code: 500, data: null });

      const report = await healthCheck.runAllChecks();
      const serverCheck = findCheck(report.checks, 'server');

      expect(serverCheck.status).toBe('degraded');
      expect(serverCheck.message).toContain('服务器响应异常');
    });

    it('服务器不可达时为 unhealthy', async () => {
      mockedRequest.get.mockRejectedValue(new Error('Network error'));

      const report = await healthCheck.runAllChecks();
      const serverCheck = findCheck(report.checks, 'server');

      expect(serverCheck.status).toBe('unhealthy');
      expect(serverCheck.message).toContain('服务器不可达');
    });
  });

  describe('calculateOverallStatus', () => {
    it('有 unhealthy 时返回 unhealthy', () => {
      const checks: HealthCheckResult[] = [
        { name: 'a', status: 'healthy', message: '', timestamp: 0, duration: 0 },
        { name: 'b', status: 'unhealthy', message: '', timestamp: 0, duration: 0 },
        { name: 'c', status: 'degraded', message: '', timestamp: 0, duration: 0 }
      ];

      const result = (healthCheck as any).calculateOverallStatus(checks);
      expect(result).toBe('unhealthy');
    });

    it('只有 degraded 时返回 degraded', () => {
      const checks: HealthCheckResult[] = [
        { name: 'a', status: 'healthy', message: '', timestamp: 0, duration: 0 },
        { name: 'b', status: 'degraded', message: '', timestamp: 0, duration: 0 }
      ];

      const result = (healthCheck as any).calculateOverallStatus(checks);
      expect(result).toBe('degraded');
    });

    it('全部 healthy 时返回 healthy', () => {
      const checks: HealthCheckResult[] = [
        { name: 'a', status: 'healthy', message: '', timestamp: 0, duration: 0 },
        { name: 'b', status: 'healthy', message: '', timestamp: 0, duration: 0 }
      ];

      const result = (healthCheck as any).calculateOverallStatus(checks);
      expect(result).toBe('healthy');
    });
  });

  describe('getLastReport', () => {
    it('返回最近一次报告', async () => {
      const report = await healthCheck.runAllChecks();
      const lastReport = healthCheck.getLastReport();

      expect(lastReport).not.toBeNull();
      expect(lastReport!.overallStatus).toBe(report.overallStatus);
      expect(lastReport!.timestamp).toBe(report.timestamp);
    });
  });

  describe('getQuickNetworkStatus', () => {
    it('正常返回网络类型', async () => {
      (wx as any).getNetworkType = jest.fn().mockImplementation(({ success }: any) => {
        success({ networkType: '4g' });
      });

      const result = await healthCheck.getQuickNetworkStatus();
      expect(result).toBe('4g');
    });

    it('失败时返回 unknown', async () => {
      (wx as any).getNetworkType = jest.fn().mockImplementation(({ fail }: any) => {
        fail();
      });

      const result = await healthCheck.getQuickNetworkStatus();
      expect(result).toBe('unknown');
    });
  });
});
