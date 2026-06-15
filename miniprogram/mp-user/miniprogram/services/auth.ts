import type { ApiResponse, WxLoginParams, WxLoginResponse } from '../typings/api';
import request from '../utils/request';
import store from '../utils/store';
import mockManager from '../utils/mock';

export const authApi = {
  async wxLogin(params: WxLoginParams): Promise<ApiResponse<WxLoginResponse>> {
    if (mockManager.isEnabled()) {
      const response = await mockManager.request<WxLoginResponse>({
        url: '/user/login',
        method: 'POST',
        data: params
      });

      if (response.code === 200 && response.data && response.data.user) {
        store.setUser(response.data.user);
      }

      return response;
    }

    const response = await request.post<WxLoginResponse>('/user/login', params);

    if (response.code === 200 && response.data && response.data.user) {
      store.setUser(response.data.user);
    }

    return response;
  },

  async loginWithWx(): Promise<ApiResponse<WxLoginResponse>> {
    return new Promise((resolve) => {
      wx.login({
        success: async (res) => {
          if (res.code) {
            const result = await this.wxLogin({ code: res.code });
            resolve(result);
          } else {
            resolve({
              code: 10001,
              message: '微信登录失败',
              data: null as any,
              timestamp: Date.now()
            });
          }
        },
        fail: () => {
          resolve({
            code: 10001,
            message: '微信登录失败',
            data: null as any,
            timestamp: Date.now()
          });
        }
      });
    });
  }
};
