import type { ApiResponse, CheckInRecord, CheckInParams, CheckOutParams, CheckInRecordListParams, PaginatedResponse } from '../typings/api';
import request from '../utils/request';
import mockManager from '../utils/mock';

export const checkInApi = {
  async checkIn(params: CheckInParams): Promise<ApiResponse<{ checkInRecordId: string; checkInTime: string; reservationId: string; seatId: string }>> {
    if (mockManager.isEnabled()) {
      return await mockManager.request<{ checkInRecordId: string; checkInTime: string; reservationId: string; seatId: string }>({
        url: '/checkin',
        method: 'POST',
        data: params
      });
    }

    return await request.post<{ checkInRecordId: string; checkInTime: string; reservationId: string; seatId: string }>('/checkin', params);
  },

  async checkOut(params: CheckOutParams): Promise<ApiResponse<{ checkOutTime: string; duration: number }>> {
    if (mockManager.isEnabled()) {
      return await mockManager.request<{ checkOutTime: string; duration: number }>({
        url: '/checkout',
        method: 'POST',
        data: params
      });
    }

    return await request.post<{ checkOutTime: string; duration: number }>('/checkout', params);
  },

  async getMyCheckInRecords(params?: CheckInRecordListParams): Promise<ApiResponse<PaginatedResponse<CheckInRecord>>> {
    if (mockManager.isEnabled()) {
      return await mockManager.request<PaginatedResponse<CheckInRecord>>({
        url: '/checkin/records',
        method: 'GET',
        data: params
      });
    }

    return await request.get<PaginatedResponse<CheckInRecord>>('/checkin/records', params);
  },

  async getCurrentCheckInStatus(): Promise<ApiResponse<{ isCheckedIn: boolean; checkInRecord: CheckInRecord | null }>> {
    if (mockManager.isEnabled()) {
      return await mockManager.request<{ isCheckedIn: boolean; checkInRecord: CheckInRecord | null }>({
        url: '/checkin/current',
        method: 'GET'
      });
    }

    return await request.get<{ isCheckedIn: boolean; checkInRecord: CheckInRecord | null }>('/checkin/current');
  }
};
