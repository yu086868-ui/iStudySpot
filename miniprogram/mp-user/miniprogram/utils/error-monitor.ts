import logger from './logger';

type ErrorType = 'js' | 'promise' | 'runtime';

interface ErrorRecord {
  type: ErrorType;
  message: string;
  stack: string;
  timestamp: number;
  pagePath: string;
  count: number;
}

var ERROR_STORAGE_KEY = 'istudyspot_errors';
var MAX_ERROR_RECORDS = 50;

class ErrorMonitor {
  private errors: ErrorRecord[];
  private originalOnError: Function | null;
  private originalOnUnhandledRejection: Function | null;

  constructor() {
    this.errors = [];
    this.originalOnError = null;
    this.originalOnUnhandledRejection = null;
  }

  install(): void {
    logger.info('ErrorMonitor', '全局错误监控已安装');

    var self = this;

    this.originalOnError = App.onError || null;
    App.onError = function(err: string) {
      self.handleJsError(err);
      if (self.originalOnError) {
        self.originalOnError.call(this, err);
      }
    };

    this.originalOnUnhandledRejection = App.onUnhandledRejection || null;
    App.onUnhandledRejection = function(rejection: WechatMiniprogram.OnUnhandledRejectionListenerResult) {
      self.handlePromiseRejection(rejection);
      if (self.originalOnUnhandledRejection) {
        self.originalOnUnhandledRejection.call(this, rejection);
      }
    };
  }

  handleJsError(err: string): void {
    logger.error('ErrorMonitor', 'JS 异常捕获', err);

    var record = this.createErrorRecord('js', err, '');
    this.addError(record);
  }

  handlePromiseRejection(rejection: WechatMiniprogram.OnUnhandledRejectionListenerResult): void {
    var reason = rejection.reason;
    var message = '';
    var stack = '';

    if (reason instanceof Error) {
      message = reason.message;
      stack = reason.stack || '';
    } else if (typeof reason === 'string') {
      message = reason;
    } else {
      message = JSON.stringify(reason);
    }

    logger.error('ErrorMonitor', 'Promise 未处理拒绝', { message: message, stack: stack });

    var record = this.createErrorRecord('promise', message, stack);
    this.addError(record);
  }

  handleRuntimeError(context: string, error: unknown): void {
    var message = '';
    var stack = '';

    if (error instanceof Error) {
      message = error.message;
      stack = error.stack || '';
    } else if (typeof error === 'string') {
      message = error;
    } else {
      message = String(error);
    }

    logger.error('ErrorMonitor', '运行时错误 [' + context + ']', { message: message, stack: stack });

    var record = this.createErrorRecord('runtime', context + ': ' + message, stack);
    this.addError(record);
  }

  private createErrorRecord(type: ErrorType, message: string, stack: string): ErrorRecord {
    var pagePath = '';
    try {
      var pages = getCurrentPages();
      if (pages && pages.length > 0) {
        pagePath = pages[pages.length - 1].route || '';
      }
    } catch (e) {
      pagePath = 'unknown';
    }

    return {
      type: type,
      message: message,
      stack: stack,
      timestamp: Date.now(),
      pagePath: pagePath,
      count: 1
    };
  }

  private addError(record: ErrorRecord): void {
    var existing = this.findSimilarError(record);
    if (existing) {
      existing.count++;
      existing.timestamp = record.timestamp;
    } else {
      this.errors.push(record);
    }

    if (this.errors.length > MAX_ERROR_RECORDS) {
      this.errors = this.errors.slice(-MAX_ERROR_RECORDS);
    }

    this.persistErrors();
  }

  private findSimilarError(record: ErrorRecord): ErrorRecord | null {
    for (var i = 0; i < this.errors.length; i++) {
      var existing = this.errors[i];
      if (existing.type === record.type && existing.message === record.message) {
        return existing;
      }
    }
    return null;
  }

  private persistErrors(): void {
    try {
      wx.setStorageSync(ERROR_STORAGE_KEY, JSON.stringify(this.errors));
    } catch (e) {
      // storage write failed
    }
  }

  loadErrors(): void {
    try {
      var stored = wx.getStorageSync(ERROR_STORAGE_KEY);
      if (stored) {
        this.errors = JSON.parse(stored);
      }
    } catch (e) {
      this.errors = [];
    }
    logger.info('ErrorMonitor', '错误记录已加载, 数量: ' + this.errors.length);
  }

  getErrors(type?: ErrorType): ErrorRecord[] {
    if (type) {
      return this.errors.filter(function(e) {
        return e.type === type;
      });
    }
    return this.errors.slice();
  }

  getErrorSummary(): { total: number; js: number; promise: number; runtime: number; uniqueCount: number } {
    var jsCount = 0;
    var promiseCount = 0;
    var runtimeCount = 0;

    for (var i = 0; i < this.errors.length; i++) {
      var e = this.errors[i];
      if (e.type === 'js') jsCount += e.count;
      else if (e.type === 'promise') promiseCount += e.count;
      else if (e.type === 'runtime') runtimeCount += e.count;
    }

    return {
      total: jsCount + promiseCount + runtimeCount,
      js: jsCount,
      promise: promiseCount,
      runtime: runtimeCount,
      uniqueCount: this.errors.length
    };
  }

  clearErrors(): void {
    this.errors = [];
    try {
      wx.removeStorageSync(ERROR_STORAGE_KEY);
    } catch (e) {
      // ignore
    }
    logger.info('ErrorMonitor', '错误记录已清除');
  }
}

var errorMonitor = new ErrorMonitor();

export default errorMonitor;
export type { ErrorType, ErrorRecord };
