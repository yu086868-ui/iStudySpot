import type { ApiResponse, CheckInRecord, CheckInParams, CheckOutParams, CheckInRecordListParams, PaginatedResponse, Reservation } from '../typings/api';
import request from '../utils/request';
import store from '../utils/store';
import mockManager from '../utils/mock';

export const checkInApi = {
  async checkIn(params: CheckInParams): Promise<ApiResponse<{ checkInRecordId: string; checkInTime: string; reservationId: string; seatId: string }>> {
    const currentUser = store.getUser();
    const reservations = store.getMyReservations();
    const reservation = reservations.find(r => r.id === params.reservationId);

    if (mockManager.isEnabled()) {
      const response = await mockManager.request<{ checkInRecordId: string; checkInTime: string; reservationId: string; seatId: string }>({
        url: '/checkin',
        method: 'POST',
        data: params
      });
      if (response.code === 200 && response.data) {
        const checkInRecord: CheckInRecord = {
          id: response.data.checkInRecordId,
          userId: currentUser && currentUser.id ? currentUser.id : '',
          reservationId: response.data.reservationId,
          studyRoomId: reservation && reservation.studyRoomId ? reservation.studyRoomId : '',
          seatId: response.data.seatId,
          checkInTime: response.data.checkInTime,
          checkOutTime: null,
          duration: 0,
          status: 'active'
        };

        store.setCurrentCheckIn({
          isCheckedIn: true,
          checkInRecord
        });

        if (reservation) {
          store.updateReservation({
            ...reservation,
            status: 'checked_in',
            checkInTime: response.data.checkInTime,
            updatedAt: new Date().toISOString()
          });
        }
      }
      return response;
    }

    const response = await request.post<{ checkInRecordId: string; checkInTime: string; reservationId: string; seatId: string }>('/checkin', params);
    if (response.code === 200 && response.data) {
      const checkInRecord: CheckInRecord = {
        id: response.data.checkInRecordId,
        userId: currentUser && currentUser.id ? currentUser.id : '',
        reservationId: response.data.reservationId,
        studyRoomId: reservation && reservation.studyRoomId ? reservation.studyRoomId : '',
        seatId: response.data.seatId,
        checkInTime: response.data.checkInTime,
        checkOutTime: null,
        duration: 0,
        status: 'active'
      };

      store.setCurrentCheckIn({
        isCheckedIn: true,
        checkInRecord
      });

      if (reservation) {
        store.updateReservation({
          ...reservation,
          status: 'checked_in',
          checkInTime: response.data.checkInTime,
          updatedAt: new Date().toISOString()
        });
      }
    }
    return response;
  },

  async checkOut(params: CheckOutParams): Promise<ApiResponse<{ checkOutTime: string; duration: number }>> {
    const currentCheckIn = store.getCurrentCheckIn();
    const reservations = store.getMyReservations();

    if (mockManager.isEnabled()) {
      const response = await mockManager.request<{ checkOutTime: string; duration: number }>({
        url: '/checkout',
        method: 'POST',
        data: params
      });
      if (response.code === 200 && response.data) {
        store.setCurrentCheckIn({
          isCheckedIn: false,
          checkInRecord: null
        });

        if (currentCheckIn.checkInRecord) {
          const reservation = reservations.find(r => r.id === currentCheckIn.checkInRecord.reservationId);
          if (reservation) {
            store.updateReservation({
              ...reservation,
              status: 'completed',
              checkOutTime: response.data.checkOutTime,
              updatedAt: new Date().toISOString()
            });
          }
        }
      }
      return response;
    }

    const response = await request.post<{ checkOutTime: string; duration: number }>('/checkout', params);
    if (response.code === 200 && response.data) {
      store.setCurrentCheckIn({
        isCheckedIn: false,
        checkInRecord: null
      });

      if (currentCheckIn.checkInRecord) {
        const reservation = reservations.find(r => r.id === currentCheckIn.checkInRecord.reservationId);
        if (reservation) {
          store.updateReservation({
            ...reservation,
            status: 'completed',
            checkOutTime: response.data.checkOutTime,
            updatedAt: new Date().toISOString()
          });
        }
      }
    }
    return response;
  },

  async getMyCheckInRecords(params?: CheckInRecordListParams, forceRefresh = false): Promise<ApiResponse<PaginatedResponse<CheckInRecord>>> {
    if (!forceRefresh && !params) {
      const cachedRecords = store.getCheckInRecords();
      if (cachedRecords.length > 0) {
        return {
          code: 200,
          message: 'success',
          data: {
            list: cachedRecords,
            total: cachedRecords.length,
            page: 1,
            pageSize: cachedRecords.length
          },
          timestamp: Date.now()
        };
      }
    }

    if (mockManager.isEnabled()) {
      const response = await mockManager.request<PaginatedResponse<CheckInRecord>>({
        url: '/checkin/records',
        method: 'GET',
        data: params
      });
      if (response.code === 200 && response.data && response.data.list) {
        store.setCheckInRecords(response.data.list);
      }
      return response;
    }

    const response = await request.get<PaginatedResponse<CheckInRecord>>('/checkin/records', params);
    if (response.code === 200 && response.data && response.data.list) {
      store.setCheckInRecords(response.data.list);
    }
    return response;
  },

  async getCurrentCheckInStatus(forceRefresh = false): Promise<ApiResponse<{ isCheckedIn: boolean; checkInRecord: CheckInRecord | null }>> {
    if (!forceRefresh) {
      const cachedStatus = store.getCurrentCheckIn();
      if (cachedStatus.isCheckedIn && cachedStatus.checkInRecord) {
        return {
          code: 200,
          message: 'success',
          data: cachedStatus,
          timestamp: Date.now()
        };
      }
    }

    if (mockManager.isEnabled()) {
      const response = await mockManager.request<{ isCheckedIn: boolean; checkInRecord: CheckInRecord | null }>({
        url: '/checkin/current',
        method: 'GET'
      });
      if (response.code === 200 && response.data) {
        store.setCurrentCheckIn(response.data);
      }
      return response;
    }

    const response = await request.get<{ isCheckedIn: boolean; checkInRecord: CheckInRecord | null }>('/checkin/current');
    if (response.code === 200 && response.data) {
      store.setCurrentCheckIn(response.data);
    }
    return response;
  }
};
