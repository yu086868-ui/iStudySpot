import type { ApiResponse } from '../typings/api';
import logger from './logger';
import metrics from '../services/metrics';

const BASE_URL = 'https://192.168.21.3:8080/api/wx';

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
}

class Request {
  private baseURL: string;

  constructor(baseURL: string) {
    this.baseURL = baseURL;
  }

  async request<T = unknown>(config: RequestConfig): Promise<ApiResponse<T>> {
    const {
      url,
      method = 'GET',
      data,
      header = {}
    } = config;

    const requestHeader: WechatMiniprogram.IAnyObject = {
      'Content-Type': 'application/json',
      ...header
    };

    var traceId = metrics.startRequest(url, method || 'GET');
    var requestStartTime = Date.now();

    logger.info('Request', '发送请求 [' + traceId + '] ' + (method || 'GET') + ' ' + url);

    return new Promise((resolve, reject) => {
      wx.request({
        url: `${this.baseURL}${url}`,
        method,
        data: data as WechatMiniprogram.IAnyObject,
        header: requestHeader,
        success: (res: WechatMiniprogram.RequestSuccessCallbackResult) => {
          const response = res.data as ApiResponse<T>;
          var isSuccess = response.code >= 200 && response.code < 300;
          metrics.endRequest(traceId, url, method || 'GET', isSuccess, response.code, isSuccess ? '' : 'code: ' + response.code, requestStartTime);
          resolve(response);
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

  get<T = unknown>(url: string, data?: unknown): Promise<ApiResponse<T>> {
    return this.request<T>({
      url,
      method: 'GET',
      data
    });
  }

  post<T = unknown>(url: string, data?: unknown): Promise<ApiResponse<T>> {
    return this.request<T>({
      url,
      method: 'POST',
      data
    });
  }

  put<T = unknown>(url: string, data?: unknown): Promise<ApiResponse<T>> {
    return this.request<T>({
      url,
      method: 'PUT',
      data
    });
  }

  delete<T = unknown>(url: string, data?: unknown): Promise<ApiResponse<T>> {
    return this.request<T>({
      url,
      method: 'DELETE',
      data
    });
  }

  requestSSE(config: SSEConfig): WechatMiniprogram.RequestTask {
    var requestHeader: WechatMiniprogram.IAnyObject = {
      'Content-Type': 'application/json',
      'Accept': 'text/event-stream'
    };

    var sseBuffer = '';

    logger.info('Request', 'SSE 请求 ' + config.url);

    var requestOption: WechatMiniprogram.IAnyObject = {
      url: this.baseURL + config.url,
      method: 'POST',
      data: config.data as WechatMiniprogram.IAnyObject,
      header: requestHeader,
      enableChunked: true,
      responseType: 'text',
      success: function () {
        if (sseBuffer.trim()) {
          var parsed = parseSSEEvent(sseBuffer);
          if (parsed) {
            try {
              var data = JSON.parse(parsed.eventData);
              config.onEvent(parsed.eventName, data);
            } catch (e) {
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
          }
        }
      }
    });

    return requestTask;
  }

  getBaseURL(): string {
    return this.baseURL;
  }
}

const request = new Request(BASE_URL);

export default request;
