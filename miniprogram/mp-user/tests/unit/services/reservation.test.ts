import { reservationApi } from '../miniprogram/services/reservation';
import mockManager from '../miniprogram/utils/mock';

jest.mock('../miniprogram/utils/mock');

describe('reservationApi', () => {
  const mockRequest = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
    (mockManager.isEnabled as jest.Mock).mockReturnValue(true);
    (mockManager.request as jest.Mock) = mockRequest;
  });

  afterEach(() => {
    mockRequest.mockReset();
  });

  describe('createReservation', () => {
    it('should create reservation successfully', async () => {
      const reservation = {
        id: 'res_001',
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

      mockRequest.mockResolvedValueOnce({
        code: 200,
        message: '预约成功',
        data: reservation,
        timestamp: Date.now()
      });

      const result = await reservationApi.createReservation({
        studyRoomId: 'room_001',
        seatId: 'seat_001',
        startTime: new Date().toISOString(),
        endTime: new Date(Date.now() + 2 * 60 * 60 * 1000).toISOString()
      });

      expect(result.code).toBe(200);
      expect(result.data.id).toBeDefined();
      expect(result.data.status).toBe('confirmed');
    });

    it('should handle reservation creation failure', async () => {
      mockRequest.mockResolvedValueOnce({
        code: 30002,
        message: '该座位已被占用或预约',
        data: null,
        timestamp: Date.now()
      });

      const result = await reservationApi.createReservation({
        studyRoomId: 'room_001',
        seatId: 'seat_001',
        startTime: new Date().toISOString(),
        endTime: new Date(Date.now() + 2 * 60 * 60 * 1000).toISOString()
      });

      expect(result.code).toBe(30002);
      expect(result.message).toBe('该座位已被占用或预约');
    });

    it('should handle already has active reservation error', async () => {
      mockRequest.mockResolvedValueOnce({
        code: 40001,
        message: '您已有进行中的预约，请先取消或完成',
        data: null,
        timestamp: Date.now()
      });

      const result = await reservationApi.createReservation({
        studyRoomId: 'room_001',
        seatId: 'seat_001',
        startTime: new Date().toISOString(),
        endTime: new Date(Date.now() + 2 * 60 * 60 * 1000).toISOString()
      });

      expect(result.code).toBe(40001);
    });
  });

  describe('getMyReservations', () => {
    it('should get user reservations successfully', async () => {
      const reservations = [
        {
          id: 'res_001',
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
        },
        {
          id: 'res_002',
          userId: 'user_001',
          studyRoomId: 'room_002',
          seatId: 'seat_002',
          startTime: new Date(Date.now() + 86400000).toISOString(),
          endTime: new Date(Date.now() + 86400000 + 2 * 60 * 60 * 1000).toISOString(),
          status: 'confirmed' as const,
          checkInTime: null,
          checkOutTime: null,
          createdAt: new Date().toISOString(),
          updatedAt: new Date().toISOString()
        }
      ];

      mockRequest.mockResolvedValueOnce({
        code: 200,
        message: 'success',
        data: { list: reservations, total: 2, page: 1, pageSize: 20 },
        timestamp: Date.now()
      });

      const result = await reservationApi.getMyReservations({ status: 'confirmed' });

      expect(result.code).toBe(200);
      expect(result.data.list).toHaveLength(2);
    });

    it('should filter reservations by status', async () => {
      const reservations = [
        {
          id: 'res_001',
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
        }
      ];

      mockRequest.mockResolvedValueOnce({
        code: 200,
        message: 'success',
        data: { list: reservations, total: 1, page: 1, pageSize: 20 },
        timestamp: Date.now()
      });

      const result = await reservationApi.getMyReservations({ status: 'confirmed' });

      expect(result.code).toBe(200);
      expect(result.data.list).toHaveLength(1);
    });

    it('should return empty list when no reservations', async () => {
      mockRequest.mockResolvedValueOnce({
        code: 200,
        message: 'success',
        data: { list: [], total: 0, page: 1, pageSize: 20 },
        timestamp: Date.now()
      });

      const result = await reservationApi.getMyReservations();

      expect(result.code).toBe(200);
      expect(result.data.list).toHaveLength(0);
    });
  });

  describe('getReservationDetail', () => {
    it('should get reservation detail successfully', async () => {
      const reservation = {
        id: 'res_001',
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

      mockRequest.mockResolvedValueOnce({
        code: 200,
        message: 'success',
        data: reservation,
        timestamp: Date.now()
      });

      const result = await reservationApi.getReservationDetail('res_001');

      expect(result.code).toBe(200);
      expect(result.data.id).toBe('res_001');
    });

    it('should handle reservation not found', async () => {
      mockRequest.mockResolvedValueOnce({
        code: 40004,
        message: '预约不存在',
        data: null,
        timestamp: Date.now()
      });

      const result = await reservationApi.getReservationDetail('nonexistent');

      expect(result.code).toBe(40004);
      expect(result.message).toBe('预约不存在');
    });
  });

  describe('cancelReservation', () => {
    it('should cancel reservation successfully', async () => {
      mockRequest.mockResolvedValueOnce({
        code: 200,
        message: '预约已取消',
        data: null,
        timestamp: Date.now()
      });

      const result = await reservationApi.cancelReservation('res_001');

      expect(result.code).toBe(200);
      expect(result.message).toBe('预约已取消');
    });

    it('should handle cancel checked-in reservation error', async () => {
      mockRequest.mockResolvedValueOnce({
        code: 40007,
        message: '预约已签到，无法取消',
        data: null,
        timestamp: Date.now()
      });

      const result = await reservationApi.cancelReservation('res_001');

      expect(result.code).toBe(40007);
      expect(result.message).toBe('预约已签到，无法取消');
    });
  });

  describe('getReservationRules', () => {
    it('should get reservation rules successfully', async () => {
      const rules = {
        maxAdvanceDays: 7,
        maxDailyReservations: 2,
        maxDurationHours: 4,
        minDurationMinutes: 30,
        cancellationDeadlineMinutes: 15,
        noShowPenalty: 5
      };

      mockRequest.mockResolvedValueOnce({
        code: 200,
        message: 'success',
        data: rules,
        timestamp: Date.now()
      });

      const result = await reservationApi.getReservationRules();

      expect(result.code).toBe(200);
      expect(result.data.maxAdvanceDays).toBe(7);
      expect(result.data.maxDurationHours).toBe(4);
    });
  });
});
