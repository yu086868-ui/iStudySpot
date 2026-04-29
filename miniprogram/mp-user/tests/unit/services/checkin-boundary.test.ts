import { checkInApi } from '../miniprogram/services/checkin';
import mockManager from '../miniprogram/utils/mock';

jest.mock('../miniprogram/utils/mock');

describe('checkInApi - Boundary & Error Tests', () => {
  const mockRequest = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
    (mockManager.isEnabled as jest.Mock).mockReturnValue(true);
    (mockManager.request as jest.Mock) = mockRequest;
  });

  describe('Check-in - Boundary Tests', () => {
    it('should reject check-in without reservation', async () => {
      mockRequest.mockResolvedValueOnce({
        code: 50005,
        message: '未找到预约记录',
        data: null,
        timestamp: Date.now()
      });

      const result = await checkInApi.checkIn({
        reservationId: '',
        seatId: 'seat_001'
      });

      expect(result.code).toBe(50005);
    });

    it('should reject check-in for non-existent reservation', async () => {
      mockRequest.mockResolvedValueOnce({
        code: 40004,
        message: '预约不存在',
        data: null,
        timestamp: Date.now()
      });

      const result = await checkInApi.checkIn({
        reservationId: 'non_existent_res',
        seatId: 'seat_001'
      });

      expect(result.code).toBe(40004);
    });

    it('should reject check-in for cancelled reservation', async () => {
      mockRequest.mockResolvedValueOnce({
        code: 50006,
        message: '预约已取消，无法签到',
        data: null,
        timestamp: Date.now()
      });

      const result = await checkInApi.checkIn({
        reservationId: 'cancelled_res',
        seatId: 'seat_001'
      });

      expect(result.code).toBe(50006);
    });

    it('should reject check-in for expired reservation', async () => {
      mockRequest.mockResolvedValueOnce({
        code: 50007,
        message: '预约已过期',
        data: null,
        timestamp: Date.now()
      });

      const result = await checkInApi.checkIn({
        reservationId: 'expired_res',
        seatId: 'seat_001'
      });

      expect(result.code).toBe(50007);
    });

    it('should reject check-in outside allowed time window', async () => {
      mockRequest.mockResolvedValueOnce({
        code: 50008,
        message: '未到签到时间',
        data: null,
        timestamp: Date.now()
      });

      const result = await checkInApi.checkIn({
        reservationId: 'future_res',
        seatId: 'seat_001'
      });

      expect(result.code).toBe(50008);
    });

    it('should reject check-in with wrong seat', async () => {
      mockRequest.mockResolvedValueOnce({
        code: 50009,
        message: '座位与预约不符',
        data: null,
        timestamp: Date.now()
      });

      const result = await checkInApi.checkIn({
        reservationId: 'res_001',
        seatId: 'wrong_seat'
      });

      expect(result.code).toBe(50009);
    });

    it('should reject check-in with empty parameters', async () => {
      mockRequest.mockResolvedValueOnce({
        code: 10003,
        message: '参数错误',
        data: null,
        timestamp: Date.now()
      });

      const result = await checkInApi.checkIn({
        reservationId: '',
        seatId: ''
      });

      expect(result.code).toBe(10003);
    });
  });

  describe('Check-out - Boundary Tests', () => {
    it('should reject check-out with non-existent record', async () => {
      mockRequest.mockResolvedValueOnce({
        code: 50003,
        message: '签到记录不存在',
        data: null,
        timestamp: Date.now()
      });

      const result = await checkInApi.checkOut({
        checkInRecordId: 'non_existent_checkin'
      });

      expect(result.code).toBe(50003);
    });

    it('should reject check-out with empty record ID', async () => {
      mockRequest.mockResolvedValueOnce({
        code: 10003,
        message: '参数错误',
        data: null,
        timestamp: Date.now()
      });

      const result = await checkInApi.checkOut({
        checkInRecordId: ''
      });

      expect(result.code).toBe(10003);
    });

    it('should reject double check-out', async () => {
      mockRequest.mockResolvedValueOnce({
        code: 50004,
        message: '已经签退',
        data: null,
        timestamp: Date.now()
      });

      const result = await checkInApi.checkOut({
        checkInRecordId: 'already_checked_out'
      });

      expect(result.code).toBe(50004);
    });
  });

  describe('Get Check-in Records - Boundary Tests', () => {
    it('should handle empty result', async () => {
      mockRequest.mockResolvedValueOnce({
        code: 200,
        message: 'success',
        data: { list: [], total: 0, page: 1, pageSize: 20 },
        timestamp: Date.now()
      });

      const result = await checkInApi.getMyCheckInRecords();

      expect(result.code).toBe(200);
      expect(result.data.list).toHaveLength(0);
    });

    it('should handle invalid date range', async () => {
      mockRequest.mockResolvedValueOnce({
        code: 200,
        message: 'success',
        data: { list: [], total: 0, page: 1, pageSize: 20 },
        timestamp: Date.now()
      });

      const result = await checkInApi.getMyCheckInRecords({
        startDate: '2024-12-31',
        endDate: '2024-01-01'
      });

      expect(result.code).toBe(200);
      expect(result.data.list).toHaveLength(0);
    });

    it('should handle very large page number', async () => {
      mockRequest.mockResolvedValueOnce({
        code: 200,
        message: 'success',
        data: { list: [], total: 5, page: 10000, pageSize: 20 },
        timestamp: Date.now()
      });

      const result = await checkInApi.getMyCheckInRecords({ page: 10000 });

      expect(result.code).toBe(200);
      expect(result.data.list).toHaveLength(0);
    });
  });

  describe('Get Current Status - Boundary Tests', () => {
    it('should return not checked in when no active session', async () => {
      mockRequest.mockResolvedValueOnce({
        code: 200,
        message: 'success',
        data: { isCheckedIn: false, checkInRecord: null },
        timestamp: Date.now()
      });

      const result = await checkInApi.getCurrentCheckInStatus();

      expect(result.code).toBe(200);
      expect(result.data.isCheckedIn).toBe(false);
      expect(result.data.checkInRecord).toBeNull();
    });
  });
});
