import type { ApiResponse, Reservation, CreateReservationParams, ReservationListParams, PaginatedResponse, ReservationRules } from '../typings/api';
import request from '../utils/request';
import store from '../utils/store';
import mockManager from '../utils/mock';

export const reservationApi = {
  async createReservation(params: CreateReservationParams): Promise<ApiResponse<Reservation>> {
    if (mockManager.isEnabled()) {
      const response = await mockManager.request<Reservation>({
        url: '/reservations',
        method: 'POST',
        data: params
      });
      if (response.code === 200 && response.data) {
        store.addReservation(response.data);
      }
      return response;
    }

    const response = await request.post<Reservation>('/reservations', params);
    if (response.code === 200 && response.data) {
      store.addReservation(response.data);
    }
    return response;
  },

  async getMyReservations(params?: ReservationListParams, forceRefresh = false): Promise<ApiResponse<PaginatedResponse<Reservation>>> {
    if (!forceRefresh) {
      const cachedReservations = store.getMyReservations();
      if (cachedReservations.length > 0) {
        let filteredList = cachedReservations;
        if (params && params.status) {
          filteredList = cachedReservations.filter(r => r.status === params.status);
        }
        return {
          code: 200,
          message: 'success',
          data: {
            list: filteredList,
            total: filteredList.length,
            page: 1,
            pageSize: filteredList.length
          },
          timestamp: Date.now()
        };
      }
    }

    if (mockManager.isEnabled()) {
      const response = await mockManager.request<PaginatedResponse<Reservation>>({
        url: '/reservations/my',
        method: 'GET',
        data: params
      });
      if (response.code === 200 && response.data && response.data.list) {
        store.setMyReservations(response.data.list);
      }
      return response;
    }

    const response = await request.get<PaginatedResponse<Reservation>>('/reservations/my', params);
    if (response.code === 200 && response.data && response.data.list) {
      store.setMyReservations(response.data.list);
    }
    return response;
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
      const response = await mockManager.request<null>({
        url: `/reservations/${id}/cancel`,
        method: 'POST'
      });
      if (response.code === 200) {
        store.removeReservation(id);
      }
      return response;
    }

    const response = await request.post<null>(`/reservations/${id}/cancel`);
    if (response.code === 200) {
      store.removeReservation(id);
    }
    return response;
  },

  async getReservationRules(forceRefresh = false): Promise<ApiResponse<ReservationRules>> {
    if (!forceRefresh) {
      const cachedRules = store.getReservationRules();
      if (cachedRules) {
        return {
          code: 200,
          message: 'success',
          data: cachedRules,
          timestamp: Date.now()
        };
      }
    }

    if (mockManager.isEnabled()) {
      const response = await mockManager.request<ReservationRules>({
        url: '/reservations/rules',
        method: 'GET'
      });
      if (response.code === 200 && response.data) {
        store.setReservationRules(response.data);
      }
      return response;
    }

    const response = await request.get<ReservationRules>('/reservations/rules');
    if (response.code === 200 && response.data) {
      store.setReservationRules(response.data);
    }
    return response;
  }
};
