import type { ApiResponse, User, UpdateUserParams, ChangePasswordParams } from '../typings/api';
import request from '../utils/request';
import store from '../utils/store';
import mockManager from '../utils/mock';

export const userApi = {
  async getCurrentUser(forceRefresh = false): Promise<ApiResponse<User>> {
    if (!forceRefresh) {
      const cachedUser = store.getUser();
      if (cachedUser) {
        return {
          code: 200,
          message: 'success',
          data: cachedUser,
          timestamp: Date.now()
        };
      }
    }

    if (mockManager.isEnabled()) {
      const response = await mockManager.request<User>({
        url: '/users/me',
        method: 'GET'
      });
      if (response.code === 200 && response.data) {
        store.setUser(response.data);
      }
      return response;
    }

    const response = await request.get<User>('/users/me');
    if (response.code === 200 && response.data) {
      store.setUser(response.data);
    }
    return response;
  },

  async updateUser(params: UpdateUserParams): Promise<ApiResponse<User>> {
    if (mockManager.isEnabled()) {
      const response = await mockManager.request<User>({
        url: '/users/me',
        method: 'PUT',
        data: params
      });
      if (response.code === 200 && response.data) {
        store.setUser(response.data);
      }
      return response;
    }

    const response = await request.put<User>('/users/me', params);
    if (response.code === 200 && response.data) {
      store.setUser(response.data);
    }
    return response;
  },

  async changePassword(params: ChangePasswordParams): Promise<ApiResponse<null>> {
    if (mockManager.isEnabled()) {
      return await mockManager.request<null>({
        url: '/users/me/password',
        method: 'PUT',
        data: params
      });
    }

    return await request.put<null>('/users/me/password', params);
  }
};
