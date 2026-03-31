import type { ApiResponse, Seat, SeatListParams } from '../typings/api';
import request from '../utils/request';
import mockManager from '../utils/mock';

export const seatApi = {
  async getSeats(studyRoomId: string, params?: SeatListParams): Promise<ApiResponse<Seat[]>> {
    if (mockManager.isEnabled()) {
      return await mockManager.request<Seat[]>({
        url: `/studyrooms/${studyRoomId}/seats`,
        method: 'GET',
        data: params
      });
    }

    return await request.get<Seat[]>(`/studyrooms/${studyRoomId}/seats`, params);
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
