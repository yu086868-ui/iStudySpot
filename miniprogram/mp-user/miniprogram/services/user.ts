import type { ApiResponse, User, UpdateUserParams, ChangePasswordParams } from '../typings/api';
import request from '../utils/request';
import mockManager from '../utils/mock';

export const userApi = {
  async getCurrentUser(): Promise<ApiResponse<User>> {
    if (mockManager.isEnabled()) {
      return await mockManager.request<User>({
        url: '/users/me',
        method: 'GET'
      });
    }

    return await request.get<User>('/users/me');
  },

  async updateUser(params: UpdateUserParams): Promise<ApiResponse<User>> {
    if (mockManager.isEnabled()) {
      return await mockManager.request<User>({
        url: '/users/me',
        method: 'PUT',
        data: params
      });
    }

    return await request.put<User>('/users/me', params);
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
