import type { ApiResponse, Reservation, CreateReservationParams, ReservationListParams, PaginatedResponse, ReservationRules } from '../typings/api';
import request from '../utils/request';
import mockManager from '../utils/mock';

export const reservationApi = {
  async createReservation(params: CreateReservationParams): Promise<ApiResponse<Reservation>> {
    if (mockManager.isEnabled()) {
      return await mockManager.request<Reservation>({
        url: '/reservations',
        method: 'POST',
        data: params
      });
    }

    return await request.post<Reservation>('/reservations', params);
  },

  async getMyReservations(params?: ReservationListParams): Promise<ApiResponse<PaginatedResponse<Reservation>>> {
    if (mockManager.isEnabled()) {
      return await mockManager.request<PaginatedResponse<Reservation>>({
        url: '/reservations/my',
        method: 'GET',
        data: params
      });
    }

    return await request.get<PaginatedResponse<Reservation>>('/reservations/my', params);
  },

  async getReservationDetail(id: string): Promise<ApiResponse<Reservation>> {
    if (mockManager.isEnabled()) {
      return await mockManager.request<Reservation>({
        url: `/reservations/${id}`,
        method: 'GET'
      });
    }

    return await request.get<Reservation>(`/reservations/${id}`);
  },

  async cancelReservation(id: string): Promise<ApiResponse<null>> {
    if (mockManager.isEnabled()) {
      return await mockManager.request<null>({
        url: `/reservations/${id}/cancel`,
        method: 'POST'
      });
    }

    return await request.post<null>(`/reservations/${id}/cancel`);
  },

  async getReservationRules(): Promise<ApiResponse<ReservationRules>> {
    if (mockManager.isEnabled()) {
      return await mockManager.request<ReservationRules>({
        url: '/reservations/rules',
        method: 'GET'
      });
    }

    return await request.get<ReservationRules>('/reservations/rules');
  }
};
