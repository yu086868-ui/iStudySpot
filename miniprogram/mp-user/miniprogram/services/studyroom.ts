import type { ApiResponse, StudyRoom, StudyRoomDetail, StudyRoomListParams, PaginatedResponse } from '../typings/api';
import request from '../utils/request';
import mockManager from '../utils/mock';

export const studyRoomApi = {
  async getStudyRooms(params?: StudyRoomListParams): Promise<ApiResponse<PaginatedResponse<StudyRoom>>> {
    if (mockManager.isEnabled()) {
      return await mockManager.request<PaginatedResponse<StudyRoom>>({
        url: '/studyrooms',
        method: 'GET',
        data: params
      });
    }

    return await request.get<PaginatedResponse<StudyRoom>>('/studyrooms', params);
  },

  async getStudyRoomDetail(id: string): Promise<ApiResponse<StudyRoomDetail>> {
    if (mockManager.isEnabled()) {
      return await mockManager.request<StudyRoomDetail>({
        url: `/studyrooms/${id}`,
        method: 'GET'
      });
    }

    return await request.get<StudyRoomDetail>(`/studyrooms/${id}`);
  }
};
