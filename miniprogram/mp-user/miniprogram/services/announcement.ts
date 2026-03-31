import type { ApiResponse, Announcement, AnnouncementListParams, PaginatedResponse } from '../typings/api';
import request from '../utils/request';
import mockManager from '../utils/mock';

export const announcementApi = {
  async getAnnouncements(params?: AnnouncementListParams): Promise<ApiResponse<PaginatedResponse<Announcement>>> {
    if (mockManager.isEnabled()) {
      return await mockManager.request<PaginatedResponse<Announcement>>({
        url: '/announcements',
        method: 'GET',
        data: params
      });
    }

    return await request.get<PaginatedResponse<Announcement>>('/announcements', params);
  },

  async getAnnouncementDetail(id: string): Promise<ApiResponse<Announcement>> {
    if (mockManager.isEnabled()) {
      return await mockManager.request<Announcement>({
        url: `/announcements/${id}`,
        method: 'GET'
      });
    }

    return await request.get<Announcement>(`/announcements/${id}`);
  }
};
