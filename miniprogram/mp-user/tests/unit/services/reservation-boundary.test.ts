import { reservationApi } from '../miniprogram/services/reservation';
import mockManager from '../miniprogram/utils/mock';

jest.mock('../miniprogram/utils/mock');

describe('reservationApi - Boundary & Error Tests', () => {
  const mockRequest = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
    (mockManager.isEnabled as jest.Mock).mockReturnValue(true);
    (mockManager.request as jest.Mock) = mockRequest;
  });

  describe('Create Reservation - Boundary Tests', () => {
    it('should reject reservation with past start time', async () => {
      mockRequest.mockResolvedValueOnce({
        code: 40002,
        message: '预约时间不能早于当前时间',
        data: null,
        timestamp: Date.now()
      });

      const result = await reservationApi.createReservation({
        studyRoomId: 'room_001',
        seatId: 'seat_001',
        startTime: '2020-01-01T00:00:00Z',
        endTime: '2020-01-01T02:00:00Z'
      });

      expect(result.code).toBe(40002);
    });

    it('should reject reservation with end time before start time', async () => {
      mockRequest.mockResolvedValueOnce({
        code: 40003,
        message: '结束时间不能早于开始时间',
        data: null,
        timestamp: Date.now()
      });

      const now = Date.now();
      const result = await reservationApi.createReservation({
        studyRoomId: 'room_001',
        seatId: 'seat_001',
        startTime: new Date(now + 4 * 60 * 60 * 1000).toISOString(),
        endTime: new Date(now + 2 * 60 * 60 * 1000).toISOString()
      });

      expect(result.code).toBe(40003);
    });

    it('should reject reservation exceeding max duration', async () => {
      mockRequest.mockResolvedValueOnce({
        code: 40005,
        message: '预约时长超过最大限制',
        data: null,
        timestamp: Date.now()
      });

      const now = Date.now();
      const result = await reservationApi.createReservation({
        studyRoomId: 'room_001',
        seatId: 'seat_001',
        startTime: new Date(now).toISOString(),
        endTime: new Date(now + 10 * 60 * 60 * 1000).toISOString()
      });

      expect(result.code).toBe(40005);
    });

    it('should reject reservation with minimum duration', async () => {
      mockRequest.mockResolvedValueOnce({
        code: 40006,
        message: '预约时长不足最短时间要求',
        data: null,
        timestamp: Date.now()
      });

      const now = Date.now();
      const result = await reservationApi.createReservation({
        studyRoomId: 'room_001',
        seatId: 'seat_001',
        startTime: new Date(now).toISOString(),
        endTime: new Date(now + 10 * 60 * 1000).toISOString()
      });

      expect(result.code).toBe(40006);
    });

    it('should reject reservation for non-existent study room', async () => {
      mockRequest.mockResolvedValueOnce({
        code: 20001,
        message: '自习室不存在',
        data: null,
        timestamp: Date.now()
      });

      const result = await reservationApi.createReservation({
        studyRoomId: 'non_existent_room',
        seatId: 'seat_001',
        startTime: new Date().toISOString(),
        endTime: new Date(Date.now() + 2 * 60 * 60 * 1000).toISOString()
      });

      expect(result.code).toBe(20001);
    });

    it('should reject reservation for non-existent seat', async () => {
      mockRequest.mockResolvedValueOnce({
        code: 30001,
        message: '座位不存在',
        data: null,
        timestamp: Date.now()
      });

      const result = await reservationApi.createReservation({
        studyRoomId: 'room_001',
        seatId: 'non_existent_seat',
        startTime: new Date().toISOString(),
        endTime: new Date(Date.now() + 2 * 60 * 60 * 1000).toISOString()
      });

      expect(result.code).toBe(30001);
    });

    it('should reject reservation with empty studyRoomId', async () => {
      mockRequest.mockResolvedValueOnce({
        code: 10003,
        message: '参数错误',
        data: null,
        timestamp: Date.now()
      });

      const result = await reservationApi.createReservation({
        studyRoomId: '',
        seatId: 'seat_001',
        startTime: new Date().toISOString(),
        endTime: new Date(Date.now() + 2 * 60 * 60 * 1000).toISOString()
      });

      expect(result.code).toBe(10003);
    });

    it('should reject reservation with empty seatId', async () => {
      mockRequest.mockResolvedValueOnce({
        code: 10003,
        message: '参数错误',
        data: null,
        timestamp: Date.now()
      });

      const result = await reservationApi.createReservation({
        studyRoomId: 'room_001',
        seatId: '',
        startTime: new Date().toISOString(),
        endTime: new Date(Date.now() + 2 * 60 * 60 * 1000).toISOString()
      });

      expect(result.code).toBe(10003);
    });
  });

  describe('Cancel Reservation - Error Tests', () => {
    it('should reject cancellation of non-existent reservation', async () => {
      mockRequest.mockResolvedValueOnce({
        code: 40004,
        message: '预约不存在',
        data: null,
        timestamp: Date.now()
      });

      const result = await reservationApi.cancelReservation('non_existent_res');

      expect(result.code).toBe(40004);
    });

    it('should reject cancellation of already cancelled reservation', async () => {
      mockRequest.mockResolvedValueOnce({
        code: 40008,
        message: '预约已取消',
        data: null,
        timestamp: Date.now()
      });

      const result = await reservationApi.cancelReservation('already_cancelled_res');

      expect(result.code).toBe(40008);
    });

    it('should reject cancellation of completed reservation', async () => {
      mockRequest.mockResolvedValueOnce({
        code: 40009,
        message: '预约已完成，无法取消',
        data: null,
        timestamp: Date.now()
      });

      const result = await reservationApi.cancelReservation('completed_res');

      expect(result.code).toBe(40009);
    });
  });

  describe('Get Reservations - Boundary Tests', () => {
    it('should handle large page number', async () => {
      mockRequest.mockResolvedValueOnce({
        code: 200,
        message: 'success',
        data: { list: [], total: 10, page: 1000, pageSize: 20 },
        timestamp: Date.now()
      });

      const result = await reservationApi.getMyReservations({ page: 1000, pageSize: 20 });

      expect(result.code).toBe(200);
      expect(result.data.list).toHaveLength(0);
    });

    it('should handle invalid date range filter', async () => {
      mockRequest.mockResolvedValueOnce({
        code: 200,
        message: 'success',
        data: { list: [], total: 0, page: 1, pageSize: 20 },
        timestamp: Date.now()
      });

      const result = await reservationApi.getMyReservations({
        startDate: '2024-12-31',
        endDate: '2024-01-01'
      });

      expect(result.code).toBe(200);
      expect(result.data.list).toHaveLength(0);
    });
  });
});
