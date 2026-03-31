import type { ApiResponse } from '../typings/api';
import type { LoginParams, LoginResponse, RegisterParams } from '../typings/api';
import request from '../utils/request';
import mockManager from '../utils/mock';

export const authApi = {
  async login(params: LoginParams): Promise<ApiResponse<LoginResponse>> {
    if (mockManager.isEnabled()) {
      const response = await mockManager.request<LoginResponse>({
        url: '/auth/login',
        method: 'POST',
        data: params
      });

      if (response.code === 200 && response.data) {
        request.saveTokens(response.data.token, response.data.refreshToken);
      }

      return response;
    }

    const response = await request.post<LoginResponse>('/auth/login', params, false);

    if (response.code === 200 && response.data) {
      request.saveTokens(response.data.token, response.data.refreshToken);
    }

    return response;
  },

  async register(params: RegisterParams): Promise<ApiResponse<{ userId: string }>> {
    if (mockManager.isEnabled()) {
      return await mockManager.request<{ userId: string }>({
        url: '/auth/register',
        method: 'POST',
        data: params
      });
    }

    return await request.post<{ userId: string }>('/auth/register', params, false);
  },

  async refreshToken(refreshToken: string): Promise<ApiResponse<{ token: string; refreshToken: string }>> {
    if (mockManager.isEnabled()) {
      const response = await mockManager.request<{ token: string; refreshToken: string }>({
        url: '/auth/refresh',
        method: 'POST',
        data: { refreshToken }
      });

      if (response.code === 200 && response.data) {
        request.saveTokens(response.data.token, response.data.refreshToken);
      }

      return response;
    }

    const response = await request.post<{ token: string; refreshToken: string }>('/auth/refresh', { refreshToken }, false);

    if (response.code === 200 && response.data) {
      request.saveTokens(response.data.token, response.data.refreshToken);
    }

    return response;
  },

  async logout(): Promise<ApiResponse<null>> {
    if (mockManager.isEnabled()) {
      const response = await mockManager.request<null>({
        url: '/auth/logout',
        method: 'POST'
      });

      request.clearTokens();
      return response;
    }

    const response = await request.post<null>('/auth/logout');
    request.clearTokens();
    return response;
  }
};
