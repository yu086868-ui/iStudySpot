import logger from '../../../miniprogram/utils/logger';

let consoleLogSpy: jest.SpyInstance;
let consoleInfoSpy: jest.SpyInstance;
let consoleWarnSpy: jest.SpyInstance;
let consoleErrorSpy: jest.SpyInstance;

beforeEach(() => {
  logger.clearLogs();
  logger.setLevel('debug');
  logger.setStorageEnabled(false);
  jest.clearAllMocks();

  consoleLogSpy = jest.spyOn(console, 'log').mockImplementation();
  consoleInfoSpy = jest.spyOn(console, 'info').mockImplementation();
  consoleWarnSpy = jest.spyOn(console, 'warn').mockImplementation();
  consoleErrorSpy = jest.spyOn(console, 'error').mockImplementation();
});

afterEach(() => {
  consoleLogSpy.mockRestore();
  consoleInfoSpy.mockRestore();
  consoleWarnSpy.mockRestore();
  consoleErrorSpy.mockRestore();
});

describe('Logger', () => {
  describe('各级别日志正确记录', () => {
    it('debug 日志正确记录', () => {
      logger.debug('Module', 'debug message');
      const entries = logger.getEntries();
      expect(entries).toHaveLength(1);
      expect(entries[0].level).toBe('debug');
      expect(entries[0].module).toBe('Module');
      expect(entries[0].message).toBe('debug message');
      expect(consoleLogSpy).toHaveBeenCalled();
    });

    it('info 日志正确记录', () => {
      logger.info('Module', 'info message');
      const entries = logger.getEntries();
      expect(entries).toHaveLength(1);
      expect(entries[0].level).toBe('info');
      expect(consoleInfoSpy).toHaveBeenCalled();
    });

    it('warn 日志正确记录', () => {
      logger.warn('Module', 'warn message');
      const entries = logger.getEntries();
      expect(entries).toHaveLength(1);
      expect(entries[0].level).toBe('warn');
      expect(consoleWarnSpy).toHaveBeenCalled();
    });

    it('error 日志正确记录', () => {
      logger.error('Module', 'error message');
      const entries = logger.getEntries();
      expect(entries).toHaveLength(1);
      expect(entries[0].level).toBe('error');
      expect(consoleErrorSpy).toHaveBeenCalled();
    });

    it('带 data 的日志正确记录', () => {
      logger.info('Module', 'message with data', { key: 'value' });
      const entries = logger.getEntries();
      expect(entries[0].data).toEqual({ key: 'value' });
    });

    it('无 data 时 data 为 null', () => {
      logger.info('Module', 'message without data');
      const entries = logger.getEntries();
      expect(entries[0].data).toBeNull();
    });
  });

  describe('setLevel 过滤低级别日志', () => {
    it('设置 warn 级别后过滤 debug 和 info', () => {
      logger.setLevel('warn');

      logger.debug('Module', 'debug');
      logger.info('Module', 'info');
      logger.warn('Module', 'warn');
      logger.error('Module', 'error');

      const entries = logger.getEntries();
      expect(entries).toHaveLength(2);
      expect(entries[0].level).toBe('warn');
      expect(entries[1].level).toBe('error');
    });

    it('设置 error 级别后只记录 error', () => {
      logger.setLevel('error');

      logger.debug('Module', 'debug');
      logger.info('Module', 'info');
      logger.warn('Module', 'warn');
      logger.error('Module', 'error');

      const entries = logger.getEntries();
      expect(entries).toHaveLength(1);
      expect(entries[0].level).toBe('error');
    });
  });

  describe('日志条目超过 MAX_LOG_ENTRIES 时自动截断', () => {
    it('超过 200 条时自动截断', () => {
      for (let i = 0; i < 210; i++) {
        logger.info('Module', `message ${i}`);
      }

      const entries = logger.getEntries();
      expect(entries).toHaveLength(200);
    });
  });

  describe('getEntries 按级别过滤', () => {
    it('不传级别返回所有条目', () => {
      logger.info('Module', 'info');
      logger.warn('Module', 'warn');

      const entries = logger.getEntries();
      expect(entries).toHaveLength(2);
    });

    it('传入级别只返回对应条目', () => {
      logger.info('Module', 'info');
      logger.warn('Module', 'warn');
      logger.error('Module', 'error');

      const errorEntries = logger.getEntries('error');
      expect(errorEntries).toHaveLength(1);
      expect(errorEntries[0].level).toBe('error');
    });
  });

  describe('getRecentEntries 返回最近 N 条', () => {
    it('返回最近指定数量的条目', () => {
      for (let i = 0; i < 10; i++) {
        logger.info('Module', `message ${i}`);
      }

      const recent = logger.getRecentEntries(3);
      expect(recent).toHaveLength(3);
      expect(recent[0].message).toBe('message 7');
      expect(recent[1].message).toBe('message 8');
      expect(recent[2].message).toBe('message 9');
    });

    it('带级别过滤返回最近 N 条', () => {
      logger.info('Module', 'info1');
      logger.warn('Module', 'warn1');
      logger.info('Module', 'info2');
      logger.warn('Module', 'warn2');

      const recentWarn = logger.getRecentEntries(1, 'warn');
      expect(recentWarn).toHaveLength(1);
      expect(recentWarn[0].message).toBe('warn2');
    });
  });

  describe('getErrorEntries 只返回错误日志', () => {
    it('只返回 error 级别的条目', () => {
      logger.info('Module', 'info');
      logger.warn('Module', 'warn');
      logger.error('Module', 'error1');
      logger.error('Module', 'error2');

      const errors = logger.getErrorEntries();
      expect(errors).toHaveLength(2);
      expect(errors.every(e => e.level === 'error')).toBe(true);
    });
  });

  describe('clearLogs 清除所有日志', () => {
    it('清除所有日志条目', () => {
      logger.info('Module', 'message1');
      logger.info('Module', 'message2');

      logger.clearLogs();

      expect(logger.getEntries()).toHaveLength(0);
      expect(wx.removeStorageSync).toHaveBeenCalledWith('istudyspot_logs');
    });
  });

  describe('getLogSummary 返回正确的统计', () => {
    it('返回各级别的计数', () => {
      logger.debug('Module', 'd1');
      logger.debug('Module', 'd2');
      logger.info('Module', 'i1');
      logger.warn('Module', 'w1');
      logger.error('Module', 'e1');

      const summary = logger.getLogSummary();
      expect(summary.total).toBe(5);
      expect(summary.debug).toBe(2);
      expect(summary.info).toBe(1);
      expect(summary.warn).toBe(1);
      expect(summary.error).toBe(1);
    });

    it('无日志时返回全零', () => {
      const summary = logger.getLogSummary();
      expect(summary.total).toBe(0);
      expect(summary.debug).toBe(0);
      expect(summary.info).toBe(0);
      expect(summary.warn).toBe(0);
      expect(summary.error).toBe(0);
    });
  });

  describe('setStorageEnabled 控制是否持久化', () => {
    it('启用存储时调用 wx.setStorageSync', () => {
      logger.setStorageEnabled(true);
      (wx.setStorageSync as jest.Mock).mockClear();

      logger.info('Module', 'message');

      expect(wx.setStorageSync).toHaveBeenCalledWith(
        'istudyspot_logs',
        expect.any(String)
      );
    });

    it('禁用存储时不调用 wx.setStorageSync', () => {
      logger.setStorageEnabled(false);
      (wx.setStorageSync as jest.Mock).mockClear();

      logger.info('Module', 'message');

      expect(wx.setStorageSync).not.toHaveBeenCalled();
    });
  });

  describe('init 加载已存储的日志', () => {
    it('从 storage 加载日志条目', () => {
      const storedLogs = JSON.stringify([
        { level: 'info', module: 'Stored', message: 'stored message', data: null, timestamp: 1000 }
      ]);
      (wx.getStorageSync as jest.Mock).mockImplementation((key: string) => {
        if (key === 'istudyspot_logs') return storedLogs;
        return '';
      });

      logger.init();

      const entries = logger.getEntries();
      expect(entries.length).toBeGreaterThanOrEqual(1);
      const storedEntry = entries.find(e => e.module === 'Stored');
      expect(storedEntry).toBeDefined();
      expect(storedEntry!.message).toBe('stored message');
    });
  });
});
