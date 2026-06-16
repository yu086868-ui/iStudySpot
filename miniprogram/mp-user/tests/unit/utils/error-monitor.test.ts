jest.mock('../../../miniprogram/utils/logger', () => ({
  __esModule: true,
  default: {
    debug: jest.fn(),
    info: jest.fn(),
    warn: jest.fn(),
    error: jest.fn()
  }
}));

import errorMonitor from '../../../miniprogram/utils/error-monitor';

beforeEach(() => {
  errorMonitor.clearErrors();
  jest.clearAllMocks();
});

describe('ErrorMonitor', () => {
  describe('handleJsError', () => {
    it('正确记录 JS 错误', () => {
      errorMonitor.handleJsError('TypeError: Cannot read property');

      const errors = errorMonitor.getErrors();
      expect(errors).toHaveLength(1);
      expect(errors[0].type).toBe('js');
      expect(errors[0].message).toBe('TypeError: Cannot read property');
      expect(errors[0].count).toBe(1);
    });
  });

  describe('handlePromiseRejection', () => {
    it('正确记录 Promise 拒绝', () => {
      errorMonitor.handlePromiseRejection({ reason: 'unhandled promise' } as any);

      const errors = errorMonitor.getErrors();
      expect(errors).toHaveLength(1);
      expect(errors[0].type).toBe('promise');
      expect(errors[0].message).toBe('unhandled promise');
      expect(errors[0].count).toBe(1);
    });

    it('处理对象类型的 reason', () => {
      errorMonitor.handlePromiseRejection({ reason: { code: 500, msg: 'fail' } } as any);

      const errors = errorMonitor.getErrors();
      expect(errors).toHaveLength(1);
      expect(errors[0].type).toBe('promise');
    });
  });

  describe('handleRuntimeError', () => {
    it('处理 Error 对象', () => {
      const err = new Error('runtime error');
      errorMonitor.handleRuntimeError('TestContext', err);

      const errors = errorMonitor.getErrors();
      expect(errors).toHaveLength(1);
      expect(errors[0].type).toBe('runtime');
      expect(errors[0].message).toContain('TestContext');
      expect(errors[0].message).toContain('runtime error');
      expect(errors[0].stack).toBe(err.stack);
    });

    it('处理字符串错误', () => {
      errorMonitor.handleRuntimeError('StringContext', 'string error message');

      const errors = errorMonitor.getErrors();
      expect(errors).toHaveLength(1);
      expect(errors[0].type).toBe('runtime');
      expect(errors[0].message).toContain('StringContext');
      expect(errors[0].message).toContain('string error message');
      expect(errors[0].stack).toBe('');
    });

    it('处理其他类型错误', () => {
      errorMonitor.handleRuntimeError('NumberContext', 42);

      const errors = errorMonitor.getErrors();
      expect(errors).toHaveLength(1);
      expect(errors[0].type).toBe('runtime');
      expect(errors[0].message).toContain('NumberContext');
      expect(errors[0].message).toContain('42');
    });
  });

  describe('相同错误合并计数', () => {
    it('相同 type 和 message 的错误合并计数', () => {
      errorMonitor.handleJsError('Same error');
      errorMonitor.handleJsError('Same error');
      errorMonitor.handleJsError('Same error');

      const errors = errorMonitor.getErrors();
      expect(errors).toHaveLength(1);
      expect(errors[0].count).toBe(3);
    });

    it('不同 message 的错误不合并', () => {
      errorMonitor.handleJsError('Error A');
      errorMonitor.handleJsError('Error B');

      const errors = errorMonitor.getErrors();
      expect(errors).toHaveLength(2);
    });

    it('不同 type 的相同 message 不合并', () => {
      errorMonitor.handleJsError('Same message');
      errorMonitor.handlePromiseRejection({ reason: 'Same message' } as any);

      const errors = errorMonitor.getErrors();
      expect(errors).toHaveLength(2);
    });
  });

  describe('错误记录超过 MAX_ERROR_RECORDS 时截断', () => {
    it('超过 50 条时自动截断', () => {
      for (let i = 0; i < 60; i++) {
        errorMonitor.handleJsError(`unique_error_${i}`);
      }

      const errors = errorMonitor.getErrors();
      expect(errors).toHaveLength(50);
    });
  });

  describe('getErrors 按类型过滤', () => {
    it('不传类型返回所有错误', () => {
      errorMonitor.handleJsError('js error');
      errorMonitor.handlePromiseRejection({ reason: 'promise error' } as any);

      const errors = errorMonitor.getErrors();
      expect(errors).toHaveLength(2);
    });

    it('传入类型只返回对应错误', () => {
      errorMonitor.handleJsError('js error');
      errorMonitor.handlePromiseRejection({ reason: 'promise error' } as any);
      errorMonitor.handleRuntimeError('ctx', 'runtime error');

      const jsErrors = errorMonitor.getErrors('js');
      expect(jsErrors).toHaveLength(1);
      expect(jsErrors[0].type).toBe('js');

      const promiseErrors = errorMonitor.getErrors('promise');
      expect(promiseErrors).toHaveLength(1);
      expect(promiseErrors[0].type).toBe('promise');

      const runtimeErrors = errorMonitor.getErrors('runtime');
      expect(runtimeErrors).toHaveLength(1);
      expect(runtimeErrors[0].type).toBe('runtime');
    });
  });

  describe('getErrorSummary 返回正确统计', () => {
    it('返回正确的错误统计', () => {
      errorMonitor.handleJsError('js error 1');
      errorMonitor.handleJsError('js error 1');
      errorMonitor.handlePromiseRejection({ reason: 'promise error' } as any);
      errorMonitor.handleRuntimeError('ctx', new Error('runtime error'));

      const summary = errorMonitor.getErrorSummary();
      expect(summary.js).toBe(2);
      expect(summary.promise).toBe(1);
      expect(summary.runtime).toBe(1);
      expect(summary.total).toBe(4);
      expect(summary.uniqueCount).toBe(3);
    });

    it('无错误时返回全零', () => {
      const summary = errorMonitor.getErrorSummary();
      expect(summary.total).toBe(0);
      expect(summary.js).toBe(0);
      expect(summary.promise).toBe(0);
      expect(summary.runtime).toBe(0);
      expect(summary.uniqueCount).toBe(0);
    });
  });

  describe('clearErrors 清除所有错误', () => {
    it('清除所有错误记录', () => {
      errorMonitor.handleJsError('error');
      errorMonitor.handlePromiseRejection({ reason: 'rejection' } as any);

      errorMonitor.clearErrors();

      expect(errorMonitor.getErrors()).toHaveLength(0);
      expect(wx.removeStorageSync).toHaveBeenCalledWith('istudyspot_errors');
    });
  });

  describe('loadErrors', () => {
    it('从 storage 加载错误记录', () => {
      const storedErrors = JSON.stringify([
        { type: 'js', message: 'stored error', stack: '', timestamp: 1000, pagePath: '', count: 2 }
      ]);
      (wx.getStorageSync as jest.Mock).mockImplementation((key: string) => {
        if (key === 'istudyspot_errors') return storedErrors;
        return '';
      });

      errorMonitor.loadErrors();

      const errors = errorMonitor.getErrors();
      expect(errors).toHaveLength(1);
      expect(errors[0].message).toBe('stored error');
      expect(errors[0].count).toBe(2);
    });

    it('处理无效数据', () => {
      (wx.getStorageSync as jest.Mock).mockImplementation((key: string) => {
        if (key === 'istudyspot_errors') return 'invalid json{{{';
        return '';
      });

      errorMonitor.loadErrors();

      expect(errorMonitor.getErrors()).toHaveLength(0);
    });
  });
});
