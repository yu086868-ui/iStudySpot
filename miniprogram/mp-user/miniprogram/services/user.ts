import type { ApiResponse, User, UpdateProfileParams, AvatarUploadResponse, UserHomeData } from '../typings/api';
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
        url: '/user/profile',
        method: 'GET'
      });
      if (response.code === 200 && response.data) {
        store.setUser(response.data);
      }
      return response;
    }

    const response = await request.get<User>('/user/profile');
    if (response.code === 200 && response.data) {
      store.setUser(response.data);
    }
    return response;
  },

  async updateProfile(params: UpdateProfileParams): Promise<ApiResponse<null>> {
    if (mockManager.isEnabled()) {
      const response = await mockManager.request<null>({
        url: '/user/profile',
        method: 'PUT',
        data: params
      });
      if (response.code === 200) {
        const currentUser = store.getUser();
        if (currentUser && params.nickname) {
          store.setUser({ ...currentUser, nickname: params.nickname });
        }
      }
      return response;
    }

    const response = await request.put<null>('/user/profile', params);
    if (response.code === 200) {
      const currentUser = store.getUser();
      if (currentUser && params.nickname) {
        store.setUser({ ...currentUser, nickname: params.nickname });
      }
    }
    return response;
  },

  async uploadAvatar(filePath: string): Promise<ApiResponse<AvatarUploadResponse>> {
    if (mockManager.isEnabled()) {
      const mockAvatarUrl = '/avatar/avatar_mock.jpg';
      const response: ApiResponse<AvatarUploadResponse> = {
        code: 200,
        message: '上传成功',
        data: { avatarUrl: mockAvatarUrl },
        timestamp: Date.now()
      };
      if (response.code === 200 && response.data) {
        const currentUser = store.getUser();
        if (currentUser) {
          store.setUser({ ...currentUser, avatarUrl: response.data.avatarUrl });
        }
      }
      return response;
    }

    return new Promise((resolve) => {
      wx.uploadFile({
        url: request.getBaseURL() + '/user/avatar',
        filePath,
        name: 'file',
        success: (uploadRes) => {
          try {
            const data = JSON.parse(uploadRes.data) as ApiResponse<AvatarUploadResponse>;
            if (data.code === 200 && data.data) {
              const currentUser = store.getUser();
              if (currentUser) {
                store.setUser({ ...currentUser, avatarUrl: data.data.avatarUrl });
              }
            }
            resolve(data);
          } catch (e) {
            resolve({
              code: 500,
              message: '上传失败',
              data: null as any,
              timestamp: Date.now()
            });
          }
        },
        fail: () => {
          resolve({
            code: 500,
            message: '上传失败',
            data: null as any,
            timestamp: Date.now()
          });
        }
      });
    });
  },

  async getUserHome(): Promise<ApiResponse<UserHomeData>> {
    if (mockManager.isEnabled()) {
      return await mockManager.request<UserHomeData>({
        url: '/user/home',
        method: 'GET'
      });
    }

    return await request.get<UserHomeData>('/user/home');
  }
};
