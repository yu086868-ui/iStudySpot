import type { ApiResponse } from '../typings/api';

const BASE_URL = 'http://localhost:3000/api';
const TOKEN_KEY = 'access_token';
const REFRESH_TOKEN_KEY = 'refresh_token';

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

    return new Promise((resolve, reject) => {
      wx.request({
        url: `${this.baseURL}${url}`,
        method,
        data,
        header: requestHeader,
        success: async (res: WechatMiniprogram.RequestSuccessCallbackResult) => {
          const response = res.data as ApiResponse<T>;

          if (response.code === 401 && needAuth) {
            const refreshSuccess = await this.refreshTokenRequest();
            if (refreshSuccess) {
              try {
                const retryResponse = await this.request<T>(config);
                resolve(retryResponse);
              } catch (error) {
                reject(error);
              }
            } else {
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
            resolve(response);
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
