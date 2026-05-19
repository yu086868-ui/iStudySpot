import type { ApiResponse, StudyRoom, StudyRoomDetail, StudyRoomListParams, PaginatedResponse } from '../typings/api';
import request from '../utils/request';
import store from '../utils/store';
import cache from '../utils/cache';
import mockManager from '../utils/mock';

export const studyRoomApi = {
  async getStudyRooms(params?: StudyRoomListParams, forceRefresh = false): Promise<ApiResponse<PaginatedResponse<StudyRoom>>> {
    if (!forceRefresh && !params) {
      const cachedRooms = store.getStudyRooms();
      if (cachedRooms.length > 0) {
        return {
          code: 200,
          message: 'success',
          data: {
            list: cachedRooms,
            total: cachedRooms.length,
            page: 1,
            pageSize: cachedRooms.length
          },
          timestamp: Date.now()
        };
      }
    }

    if (mockManager.isEnabled()) {
      const response = await mockManager.request<PaginatedResponse<StudyRoom>>({
        url: '/studyrooms',
        method: 'GET',
        data: params
      });
      if (response.code === 200 && response.data && response.data.list) {
        store.setStudyRooms(response.data.list);
      }
      return response;
    }

    const response = await request.get<PaginatedResponse<StudyRoom>>('/studyrooms', params);
    if (response.code === 200 && response.data && response.data.list) {
      store.setStudyRooms(response.data.list);
    }
    return response;
  },

  async getStudyRoomDetail(id: string, forceRefresh = false): Promise<ApiResponse<StudyRoomDetail>> {
    if (!forceRefresh) {
      const cachedRoom = store.getStudyRoomDetail(id);
      if (cachedRoom) {
        return {
          code: 200,
          message: 'success',
          data: cachedRoom,
          timestamp: Date.now()
        };
      }
    }

    if (mockManager.isEnabled()) {
      const response = await mockManager.request<StudyRoomDetail>({
        url: `/studyrooms/${id}`,
        method: 'GET'
      });
      if (response.code === 200 && response.data) {
        store.setStudyRoomDetail(response.data);
      }
      return response;
    }

    const response = await request.get<StudyRoomDetail>(`/studyrooms/${id}`);
    if (response.code === 200 && response.data) {
      store.setStudyRoomDetail(response.data);
    }
    return response;
  }
};
