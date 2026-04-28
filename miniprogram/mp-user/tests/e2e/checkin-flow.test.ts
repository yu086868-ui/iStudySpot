import { checkInApi } from '../miniprogram/services/checkin';
import { reservationApi } from '../miniprogram/services/reservation';
import mockManager from '../miniprogram/utils/mock';

jest.mock('../miniprogram/utils/mock');

describe('Check-in Flow E2E Tests', () => {
  const mockRequest = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
    (mockManager.isEnabled as jest.Mock).mockReturnValue(true);
    (mockManager.request as jest.Mock) = mockRequest;
  });

  afterEach(() => {
    mockRequest.mockReset();
  });

  describe('Complete check-in flow', () => {
    it('should check in successfully with reservation', async () => {
      const checkInResponse = {
        checkInRecordId: 'checkin_001',
        checkInTime: new Date().toISOString(),
        reservationId: 'res_001',
        seatId: 'seat_001'
      };

      mockRequest.mockResolvedValueOnce({
        code: 200,
        message: '签到成功',
        data: checkInResponse,
        timestamp: Date.now()
      });

      const result = await checkInApi.checkIn({
        reservationId: 'res_001',
        seatId: 'seat_001'
      });

      expect(result.code).toBe(200);
      expect(result.data.checkInRecordId).toBeDefined();
      expect(result.data.checkInTime).toBeDefined();
    });

    it('should handle already checked in error', async () => {
      mockRequest.mockResolvedValueOnce({
        code: 50002,
        message: '已经签到，无需重复签到',
        data: null,
        timestamp: Date.now()
      });

      const result = await checkInApi.checkIn({
        reservationId: 'res_001',
        seatId: 'seat_001'
      });

      expect(result.code).toBe(50002);
      expect(result.message).toBe('已经签到，无需重复签到');
    });
  });

  describe('Check-out flow', () => {
    it('should check out successfully', async () => {
      const checkOutResponse = {
        checkOutTime: new Date().toISOString(),
        duration: 120
      };

      mockRequest.mockResolvedValueOnce({
        code: 200,
        message: '签退成功',
        data: checkOutResponse,
        timestamp: Date.now()
      });

      const result = await checkInApi.checkOut({
        checkInRecordId: 'checkin_001'
      });

      expect(result.code).toBe(200);
      expect(result.data.checkOutTime).toBeDefined();
      expect(result.data.duration).toBe(120);
    });

    it('should handle already checked out error', async () => {
      mockRequest.mockResolvedValueOnce({
        code: 50004,
        message: '已经签退',
        data: null,
        timestamp: Date.now()
      });

      const result = await checkInApi.checkOut({
        checkInRecordId: 'checkin_001'
      });

      expect(result.code).toBe(50004);
      expect(result.message).toBe('已经签退');
    });

    it('should handle check-in record not found', async () => {
      mockRequest.mockResolvedValueOnce({
        code: 50003,
        message: '签到记录不存在',
        data: null,
        timestamp: Date.now()
      });

      const result = await checkInApi.checkOut({
        checkInRecordId: 'nonexistent'
      });

      expect(result.code).toBe(50003);
      expect(result.message).toBe('签到记录不存在');
    });
  });

  describe('Check-in status', () => {
    it('should get current check-in status when active', async () => {
      const checkInRecord = {
        id: 'checkin_001',
        userId: 'user_001',
        reservationId: 'res_001',
        studyRoomId: 'room_001',
        seatId: 'seat_001',
        checkInTime: new Date().toISOString(),
        checkOutTime: null,
        duration: 0,
        status: 'active' as const
      };
      const statusResponse = {
        isCheckedIn: true,
        checkInRecord
      };

      mockRequest.mockResolvedValueOnce({
        code: 200,
        message: 'success',
        data: statusResponse,
        timestamp: Date.now()
      });

      const result = await checkInApi.getCurrentCheckInStatus();

      expect(result.code).toBe(200);
      expect(result.data.isCheckedIn).toBe(true);
      expect(result.data.checkInRecord).toBeDefined();
    });

    it('should get current check-in status when not checked in', async () => {
      const statusResponse = {
        isCheckedIn: false,
        checkInRecord: null
      };

      mockRequest.mockResolvedValueOnce({
        code: 200,
        message: 'success',
        data: statusResponse,
        timestamp: Date.now()
      });

      const result = await checkInApi.getCurrentCheckInStatus();

      expect(result.code).toBe(200);
      expect(result.data.isCheckedIn).toBe(false);
      expect(result.data.checkInRecord).toBeNull();
    });
  });

  describe('Check-in records history', () => {
    it('should get check-in records history', async () => {
      const records = [
        {
          id: 'checkin_001',
          userId: 'user_001',
          reservationId: 'res_001',
          studyRoomId: 'room_001',
          seatId: 'seat_001',
          checkInTime: new Date().toISOString(),
          checkOutTime: null,
          duration: 0,
          status: 'completed' as const
        },
        {
          id: 'checkin_002',
          userId: 'user_001',
          reservationId: 'res_002',
          studyRoomId: 'room_002',
          seatId: 'seat_002',
          checkInTime: new Date(Date.now() - 86400000).toISOString(),
          checkOutTime: new Date(Date.now() - 82800000).toISOString(),
          duration: 60,
          status: 'completed' as const
        }
      ];

      mockRequest.mockResolvedValueOnce({
        code: 200,
        message: 'success',
        data: {
          list: records,
          total: 2,
          page: 1,
          pageSize: 20
        },
        timestamp: Date.now()
      });

      const result = await checkInApi.getMyCheckInRecords();

      expect(result.code).toBe(200);
      expect(result.data.list).toHaveLength(2);
    });

    it('should filter check-in records by date', async () => {
      const records = [
        {
          id: 'checkin_001',
          userId: 'user_001',
          reservationId: 'res_001',
          studyRoomId: 'room_001',
          seatId: 'seat_001',
          checkInTime: '2024-06-15T10:00:00Z',
          checkOutTime: '2024-06-15T12:00:00Z',
          duration: 120,
          status: 'completed' as const
        }
      ];

      mockRequest.mockResolvedValueOnce({
        code: 200,
        message: 'success',
        data: {
          list: records,
          total: 1,
          page: 1,
          pageSize: 20
        },
        timestamp: Date.now()
      });

      const result = await checkInApi.getMyCheckInRecords({
        startDate: '2024-06-01',
        endDate: '2024-06-30'
      });

      expect(result.code).toBe(200);
      expect(result.data.list).toHaveLength(1);
    });
  });

  describe('Complete study session flow', () => {
    it('should complete full study session from reservation to check-out', async () => {
      const reservation = {
        id: 'res_new_001',
        userId: 'user_001',
        studyRoomId: 'room_001',
        seatId: 'seat_001',
        startTime: new Date().toISOString(),
        endTime: new Date(Date.now() + 2 * 60 * 60 * 1000).toISOString(),
        status: 'confirmed' as const,
        checkInTime: null,
        checkOutTime: null,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString()
      };
      const checkInResponse = {
        checkInRecordId: 'checkin_new_001',
        checkInTime: new Date().toISOString(),
        reservationId: reservation.id,
        seatId: 'seat_001'
      };
      const checkOutResponse = {
        checkOutTime: new Date(Date.now() + 2 * 60 * 60 * 1000).toISOString(),
        duration: 120
      };

      mockRequest
        .mockResolvedValueOnce({
          code: 200,
          message: '预约成功',
          data: reservation,
          timestamp: Date.now()
        })
        .mockResolvedValueOnce({
          code: 200,
          message: '签到成功',
          data: checkInResponse,
          timestamp: Date.now()
        })
        .mockResolvedValueOnce({
          code: 200,
          message: '签退成功',
          data: checkOutResponse,
          timestamp: Date.now()
        });

      const reservationResult = await reservationApi.createReservation({
        studyRoomId: 'room_001',
        seatId: 'seat_001',
        startTime: new Date().toISOString(),
        endTime: new Date(Date.now() + 2 * 60 * 60 * 1000).toISOString()
      });
      expect(reservationResult.code).toBe(200);

      const checkInResult = await checkInApi.checkIn({
        reservationId: reservation.id,
        seatId: 'seat_001'
      });
      expect(checkInResult.code).toBe(200);

      const checkOutResult = await checkInApi.checkOut({
        checkInRecordId: checkInResponse.checkInRecordId
      });
      expect(checkOutResult.code).toBe(200);
      expect(checkOutResult.data.duration).toBe(120);
    });
  });
});
