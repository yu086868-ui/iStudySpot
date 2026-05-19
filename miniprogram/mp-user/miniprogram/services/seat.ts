import type { ApiResponse, Seat, SeatListParams } from '../typings/api';
import request from '../utils/request';
import store from '../utils/store';
import mockManager from '../utils/mock';

export const seatApi = {
  async getSeats(studyRoomId: string, params?: SeatListParams, forceRefresh = false): Promise<ApiResponse<Seat[]>> {
    if (!forceRefresh && !params) {
      const cachedSeats = store.getSeats(studyRoomId);
      if (cachedSeats && cachedSeats.length > 0) {
        return {
          code: 200,
          message: 'success',
          data: cachedSeats,
          timestamp: Date.now()
        };
      }
    }

    if (mockManager.isEnabled()) {
      const response = await mockManager.request<Seat[]>({
        url: `/studyrooms/${studyRoomId}/seats`,
        method: 'GET',
        data: params
      });
      if (response.code === 200 && response.data) {
        store.setSeats(studyRoomId, response.data);
      }
      return response;
    }

    const response = await request.get<Seat[]>(`/studyrooms/${studyRoomId}/seats`, params);
    if (response.code === 200 && response.data) {
      store.setSeats(studyRoomId, response.data);
    }
    return response;
  },

  async getSeatDetail(id: string): Promise<ApiResponse<Seat>> {
    if (mockManager.isEnabled()) {
      return await mockManager.request<Seat>({
        url: `/seats/${id}`,
        method: 'GET'
      });
    }

    return await request.get<Seat>(`/seats/${id}`);
  }
};
