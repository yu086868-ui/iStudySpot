const BASE_URL = 'http://localhost:3000/api';

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

  async request<T = unknown>(config: RequestConfig): Promise<T> {
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

    return new Promise((resolve, reject) => {
      wx.request({
        url: `${this.baseURL}${url}`,
        method,
        data,
        header: requestHeader,
        success: (res: WechatMiniprogram.RequestSuccessCallbackResult) => {
          if (res.statusCode >= 200 && res.statusCode < 300) {
            resolve(res.data as T);
          } else {
            reject(new Error(`Request failed with status ${res.statusCode}`));
          }
        },
        fail: (error: WechatMiniprogram.GeneralCallbackResult) => {
          wx.showToast({
            title: '网络请求失败',
            icon: 'none'
          });
          reject(error);
        }
      });
    });
  }

  get<T = unknown>(url: string, data?: unknown): Promise<T> {
    return this.request<T>({
      url,
      method: 'GET',
      data
    });
  }

  post<T = unknown>(url: string, data?: unknown): Promise<T> {
    return this.request<T>({
      url,
      method: 'POST',
      data
    });
  }
}

const request = new Request(BASE_URL);

export default request;
