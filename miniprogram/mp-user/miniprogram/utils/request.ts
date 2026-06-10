import type { ApiResponse } from '../typings/api';
import logger from './logger';
import metrics from '../services/metrics';

const BASE_URL = 'https://192.168.21.3:8080/api';
const TOKEN_KEY = 'access_token';
const REFRESH_TOKEN_KEY = 'refresh_token';

// ==================== SSE 相关类型与工具函数 ====================

interface SSEConfig {
  url: string;
  data?: unknown;
  onEvent: (eventName: string, eventData: unknown) => void;
  onError: (error: string) => void;
  onComplete: () => void;
}

function decodeArrayBuffer(buffer: ArrayBuffer): string {
  var uint8Array = new Uint8Array(buffer);
  var result = '';
  for (var i = 0; i < uint8Array.length; i++) {
    result += String.fromCharCode(uint8Array[i]);
  }
  try {
    return decodeURIComponent(escape(result));
  } catch (e) {
    return result;
  }
}

function parseSSEEvent(eventStr: string): { eventName: string; eventData: string } | null {
  var lines = eventStr.split('\n');
  var eventName = 'message';
  var eventData = '';

  for (var i = 0; i < lines.length; i++) {
    var line = lines[i];
    if (line.indexOf('event:') === 0) {
      eventName = line.substring(6).trim();
    } else if (line.indexOf('data:') === 0) {
      eventData = line.substring(5);
    }
  }

  if (!eventData) return null;
  return { eventName: eventName, eventData: eventData };
}

interface RequestConfig {
  url: string;
  method?: 'GET' | 'POST' | 'PUT' | 'DELETE';
  data?: unknown;
  header?: WechatMiniprogram.IAnyObject;
  needAuth?: boolean;
}

class Request {
  private baseURL: string;

  constructor(baseURL: string) {
    this.baseURL = baseURL;
  }

  private get token(): string {
    return wx.getStorageSync(TOKEN_KEY) || '';
  }

  private get refreshToken(): string {
    return wx.getStorageSync(REFRESH_TOKEN_KEY) || '';
  }

  private setToken(token: string): void {
    wx.setStorageSync(TOKEN_KEY, token);
  }

  private setRefreshToken(refreshToken: string): void {
    wx.setStorageSync(REFRESH_TOKEN_KEY, refreshToken);
  }

  private clearToken(): void {
    wx.removeStorageSync(TOKEN_KEY);
    wx.removeStorageSync(REFRESH_TOKEN_KEY);
  }

  private async refreshTokenRequest(): Promise<boolean> {
    try {
      const response = await this.request({
        url: '/auth/refresh',
        method: 'POST',
        data: { refreshToken: this.refreshToken },
        needAuth: false
      });

      if (response.code === 200 && response.data) {
        const { token, refreshToken } = response.data as { token: string; refreshToken: string };
        this.setToken(token);
        this.setRefreshToken(refreshToken);
        return true;
      }

      return false;
    } catch (error) {
      this.clearToken();
      return false;
    }
  }

  async request<T = unknown>(config: RequestConfig): Promise<ApiResponse<T>> {
    const {
      url,
      method = 'GET',
      data,
      header = {},
      needAuth = true
    } = config;

    const requestHeader: WechatMiniprogram.IAnyObject = {
      'Content-Type': 'application/json',
      ...header
    };

    if (needAuth && this.token) {
      requestHeader['Authorization'] = `Bearer ${this.token}`;
    }

    var traceId = metrics.startRequest(url, method || 'GET');
    var requestStartTime = Date.now();

    logger.info('Request', '发送请求 [' + traceId + '] ' + (method || 'GET') + ' ' + url);

    return new Promise((resolve, reject) => {
      wx.request({
        url: `${this.baseURL}${url}`,
        method,
        data: data as WechatMiniprogram.IAnyObject,
        header: requestHeader,
        success: async (res: WechatMiniprogram.RequestSuccessCallbackResult) => {
          const response = res.data as ApiResponse<T>;

          if (response.code === 401 && needAuth) {
            const refreshSuccess = await this.refreshTokenRequest();
            if (refreshSuccess) {
              try {
                const retryResponse = await this.request<T>(config);
                metrics.endRequest(traceId, url, method || 'GET', true, response.code, '', requestStartTime);
                resolve(retryResponse);
              } catch (error) {
                metrics.endRequest(traceId, url, method || 'GET', false, 401, 'Token refresh retry failed', requestStartTime);
                reject(error);
              }
            } else {
              metrics.endRequest(traceId, url, method || 'GET', false, 401, 'Token expired and refresh failed', requestStartTime);
              wx.showToast({
                title: '登录已过期，请重新登录',
                icon: 'none'
              });
              wx.reLaunch({
                url: '/pages/login/login'
              });
              reject(new Error('Token expired'));
            }
          } else {
            var isSuccess = response.code >= 200 && response.code < 300;
            metrics.endRequest(traceId, url, method || 'GET', isSuccess, response.code, isSuccess ? '' : 'code: ' + response.code, requestStartTime);
            resolve(response);
          }
        },
        fail: (error: WechatMiniprogram.GeneralCallbackResult) => {
          metrics.endRequest(traceId, url, method || 'GET', false, 0, error.errMsg || 'network error', requestStartTime);
          logger.error('Request', '请求失败 [' + traceId + '] ' + url, error);
          wx.showToast({
            title: '网络请求失败',
            icon: 'none'
          });
          reject(error);
        }
      });
    });
  }

  get<T = unknown>(url: string, data?: unknown, needAuth = true): Promise<ApiResponse<T>> {
    return this.request<T>({
      url,
      method: 'GET',
      data,
      needAuth
    });
  }

  post<T = unknown>(url: string, data?: unknown, needAuth = true): Promise<ApiResponse<T>> {
    return this.request<T>({
      url,
      method: 'POST',
      data,
      needAuth
    });
  }

  put<T = unknown>(url: string, data?: unknown, needAuth = true): Promise<ApiResponse<T>> {
    return this.request<T>({
      url,
      method: 'PUT',
      data,
      needAuth
    });
  }

  delete<T = unknown>(url: string, data?: unknown, needAuth = true): Promise<ApiResponse<T>> {
    return this.request<T>({
      url,
      method: 'DELETE',
      data,
      needAuth
    });
  }

  requestSSE(config: SSEConfig): WechatMiniprogram.RequestTask {
    var requestHeader: WechatMiniprogram.IAnyObject = {
      'Content-Type': 'application/json',
      'Accept': 'text/event-stream'
    };

    if (this.token) {
      requestHeader['Authorization'] = 'Bearer ' + this.token;
    }

    var sseBuffer = '';

    logger.info('Request', 'SSE 请求 ' + config.url);

    // enableChunked 和 onChunkReceived 是微信基础库 2.20.2+ 的 SSE 支持
    // 当前类型定义未包含，使用类型断言绕过
    var requestOption: WechatMiniprogram.IAnyObject = {
      url: this.baseURL + config.url,
      method: 'POST',
      data: config.data as WechatMiniprogram.IAnyObject,
      header: requestHeader,
      enableChunked: true,
      responseType: 'text',
      success: function () {
        // 处理缓冲区中剩余的数据
        if (sseBuffer.trim()) {
          var parsed = parseSSEEvent(sseBuffer);
          if (parsed) {
            try {
              var data = JSON.parse(parsed.eventData);
              config.onEvent(parsed.eventName, data);
            } catch (e) {
              // 忽略解析错误
            }
          }
        }
        config.onComplete();
      },
      fail: function (error: WechatMiniprogram.GeneralCallbackResult) {
        logger.error('Request', 'SSE 请求失败 ' + config.url, error);
        config.onError(error.errMsg || 'SSE request failed');
      }
    };

    var requestTask = wx.request(requestOption as WechatMiniprogram.RequestOption);

    (requestTask as WechatMiniprogram.IAnyObject).onChunkReceived(function (res: { data: ArrayBuffer }) {
      var chunk = decodeArrayBuffer(res.data);
      sseBuffer += chunk;

      // 处理完整的 SSE 事件（以 \n\n 分隔）
      while (sseBuffer.indexOf('\n\n') !== -1) {
        var idx = sseBuffer.indexOf('\n\n');
        var eventStr = sseBuffer.substring(0, idx);
        sseBuffer = sseBuffer.substring(idx + 2);

        var parsed = parseSSEEvent(eventStr);
        if (parsed) {
          try {
            var data = JSON.parse(parsed.eventData);
            config.onEvent(parsed.eventName, data);
          } catch (e) {
            // 忽略 JSON 解析错误
          }
        }
      }
    });

    return requestTask;
  }

  saveTokens(token: string, refreshToken: string): void {
    this.setToken(token);
    this.setRefreshToken(refreshToken);
  }

  clearTokens(): void {
    this.clearToken();
  }

  isLoggedIn(): boolean {
    return !!this.token;
  }
}

const request = new Request(BASE_URL);

export default request;
