type LogLevel = 'debug' | 'info' | 'warn' | 'error';

interface LogEntry {
  level: LogLevel;
  module: string;
  message: string;
  data: unknown;
  timestamp: number;
}

const LOG_LEVEL_PRIORITY: Record<LogLevel, number> = {
  debug: 0,
  info: 1,
  warn: 2,
  error: 3
};

const LOG_STORAGE_KEY = 'istudyspot_logs';
const MAX_LOG_ENTRIES = 200;

class Logger {
  private minLevel: LogLevel;
  private enableStorage: boolean;
  private entries: LogEntry[];

  constructor() {
    this.minLevel = 'debug';
    this.enableStorage = true;
    this.entries = [];
  }

  setLevel(level: LogLevel): void {
    this.minLevel = level;
  }

  setStorageEnabled(enabled: boolean): void {
    this.enableStorage = enabled;
  }

  debug(module: string, message: string, data?: unknown): void {
    this.log('debug', module, message, data);
  }

  info(module: string, message: string, data?: unknown): void {
    this.log('info', module, message, data);
  }

  warn(module: string, message: string, data?: unknown): void {
    this.log('warn', module, message, data);
  }

  error(module: string, message: string, data?: unknown): void {
    this.log('error', module, message, data);
  }

  private log(level: LogLevel, module: string, message: string, data?: unknown): void {
    if (LOG_LEVEL_PRIORITY[level] < LOG_LEVEL_PRIORITY[this.minLevel]) {
      return;
    }

    const entry: LogEntry = {
      level: level,
      module: module,
      message: message,
      data: data !== undefined ? data : null,
      timestamp: Date.now()
    };

    this.entries.push(entry);

    if (this.entries.length > MAX_LOG_ENTRIES) {
      this.entries = this.entries.slice(-MAX_LOG_ENTRIES);
    }

    if (this.enableStorage) {
      this.persistLogs();
    }

    this.consoleOutput(entry);
  }

  private consoleOutput(entry: LogEntry): void {
    const prefix = '[' + entry.level.toUpperCase() + '] [' + entry.module + ']';
    const timeStr = this.formatTimestamp(entry.timestamp);

    switch (entry.level) {
      case 'debug':
        console.log(prefix, timeStr, entry.message, entry.data !== null ? entry.data : '');
        break;
      case 'info':
        console.info(prefix, timeStr, entry.message, entry.data !== null ? entry.data : '');
        break;
      case 'warn':
        console.warn(prefix, timeStr, entry.message, entry.data !== null ? entry.data : '');
        break;
      case 'error':
        console.error(prefix, timeStr, entry.message, entry.data !== null ? entry.data : '');
        break;
    }
  }

  private formatTimestamp(ts: number): string {
    const date = new Date(ts);
    const h = date.getHours().toString().padStart(2, '0');
    const m = date.getMinutes().toString().padStart(2, '0');
    const s = date.getSeconds().toString().padStart(2, '0');
    const ms = date.getMilliseconds().toString().padStart(3, '0');
    return h + ':' + m + ':' + s + '.' + ms;
  }

  private persistLogs(): void {
    try {
      wx.setStorageSync(LOG_STORAGE_KEY, JSON.stringify(this.entries));
    } catch (e) {
      // storage write failed, ignore
    }
  }

  private loadLogs(): void {
    try {
      const stored = wx.getStorageSync(LOG_STORAGE_KEY);
      if (stored) {
        this.entries = JSON.parse(stored);
      }
    } catch (e) {
      this.entries = [];
    }
  }

  getEntries(level?: LogLevel): LogEntry[] {
    if (level) {
      return this.entries.filter(function(entry) {
        return entry.level === level;
      });
    }
    return this.entries.slice();
  }

  getRecentEntries(count: number, level?: LogLevel): LogEntry[] {
    var source = level ? this.getEntries(level) : this.entries;
    return source.slice(-count);
  }

  getErrorEntries(): LogEntry[] {
    return this.getEntries('error');
  }

  clearLogs(): void {
    this.entries = [];
    try {
      wx.removeStorageSync(LOG_STORAGE_KEY);
    } catch (e) {
      // ignore
    }
  }

  getLogSummary(): { total: number; debug: number; info: number; warn: number; error: number } {
    var summary = { total: 0, debug: 0, info: 0, warn: 0, error: 0 };
    for (var i = 0; i < this.entries.length; i++) {
      var entry = this.entries[i];
      summary.total++;
      summary[entry.level]++;
    }
    return summary;
  }

  init(): void {
    this.loadLogs();
    this.info('Logger', '日志系统已初始化');
  }
}

var logger = new Logger();

export default logger;
export type { LogLevel, LogEntry };
