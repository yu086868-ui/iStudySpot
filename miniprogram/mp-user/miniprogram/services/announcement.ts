import type { ApiResponse, Announcement, AnnouncementListParams, PaginatedResponse } from '../typings/api';
import request from '../utils/request';
import store from '../utils/store';
import mockManager from '../utils/mock';

export const announcementApi = {
  async getAnnouncements(params?: AnnouncementListParams, forceRefresh = false): Promise<ApiResponse<PaginatedResponse<Announcement>>> {
    if (!forceRefresh && !params) {
      const cachedAnnouncements = store.getAnnouncements();
      if (cachedAnnouncements.length > 0) {
        return {
          code: 200,
          message: 'success',
          data: {
            list: cachedAnnouncements,
            total: cachedAnnouncements.length,
            page: 1,
            pageSize: cachedAnnouncements.length
          },
          timestamp: Date.now()
        };
      }
    }

    if (mockManager.isEnabled()) {
      const response = await mockManager.request<PaginatedResponse<Announcement>>({
        url: '/announcements',
        method: 'GET',
        data: params
      });
      if (response.code === 200 && response.data && response.data.list) {
        store.setAnnouncements(response.data.list);
      }
      return response;
    }

    const response = await request.get<PaginatedResponse<Announcement>>('/announcements', params);
    if (response.code === 200 && response.data && response.data.list) {
      store.setAnnouncements(response.data.list);
    }
    return response;
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
