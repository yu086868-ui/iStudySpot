import { seatApi } from '../miniprogram/services/seat';
import mockManager from '../miniprogram/utils/mock';

jest.mock('../miniprogram/utils/mock');

describe('seatApi', () => {
  const mockRequest = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
    (mockManager.isEnabled as jest.Mock).mockReturnValue(true);
    (mockManager.request as jest.Mock) = mockRequest;
  });

  afterEach(() => {
    mockRequest.mockReset();
  });

  describe('getSeats', () => {
    it('should get seats for study room successfully', async () => {
      const seats = Array.from({ length: 10 }, (_, i) => ({
        id: `seat_${i + 1}`,
        studyRoomId: 'room_001',
        row: Math.floor(i / 5) + 1,
        col: (i % 5) + 1,
        seatNumber: `${String.fromCharCode(65 + Math.floor(i / 5))}${(i % 5) + 1}`,
        type: 'normal' as const,
        status: 'available' as const,
        facilities: ['插座']
      }));

      mockRequest.mockResolvedValueOnce({
        code: 200,
        message: 'success',
        data: seats,
        timestamp: Date.now()
      });

      const result = await seatApi.getSeats('room_001');

      expect(result.code).toBe(200);
      expect(result.data).toHaveLength(10);
    });

    it('should filter seats by status', async () => {
      const seats = [
        {
          id: 'seat_001',
          studyRoomId: 'room_001',
          row: 1,
          col: 1,
          seatNumber: 'A1',
          type: 'normal' as const,
          status: 'available' as const,
          facilities: ['插座']
        },
        {
          id: 'seat_002',
          studyRoomId: 'room_001',
          row: 1,
          col: 2,
          seatNumber: 'A2',
          type: 'normal' as const,
          status: 'occupied' as const,
          facilities: ['插座']
        },
        {
          id: 'seat_003',
          studyRoomId: 'room_001',
          row: 1,
          col: 3,
          seatNumber: 'A3',
          type: 'normal' as const,
          status: 'available' as const,
          facilities: ['插座']
        }
      ];

      mockRequest.mockResolvedValueOnce({
        code: 200,
        message: 'success',
        data: seats,
        timestamp: Date.now()
      });

      const result = await seatApi.getSeats('room_001', { status: 'available' });

      expect(result.code).toBe(200);
      expect(result.data).toHaveLength(3);
    });

    it('should filter seats by type', async () => {
      const seats = [
        {
          id: 'seat_001',
          studyRoomId: 'room_001',
          row: 1,
          col: 1,
          seatNumber: 'A1',
          type: 'normal' as const,
          status: 'available' as const,
          facilities: ['插座']
        },
        {
          id: 'seat_002',
          studyRoomId: 'room_001',
          row: 1,
          col: 2,
          seatNumber: 'A2',
          type: 'vip' as const,
          status: 'available' as const,
          facilities: ['插座', '台灯', '人体工学椅']
        },
        {
          id: 'seat_003',
          studyRoomId: 'room_001',
          row: 1,
          col: 3,
          seatNumber: 'A3',
          type: 'normal' as const,
          status: 'available' as const,
          facilities: ['插座']
        }
      ];

      mockRequest.mockResolvedValueOnce({
        code: 200,
        message: 'success',
        data: seats,
        timestamp: Date.now()
      });

      const result = await seatApi.getSeats('room_001', { type: 'normal' });

      expect(result.code).toBe(200);
      expect(result.data).toHaveLength(3);
    });

    it('should return empty array when no seats found', async () => {
      mockRequest.mockResolvedValueOnce({
        code: 200,
        message: 'success',
        data: [],
        timestamp: Date.now()
      });

      const result = await seatApi.getSeats('room_999');

      expect(result.code).toBe(200);
      expect(result.data).toHaveLength(0);
    });
  });

  describe('getSeatDetail', () => {
    it('should get seat detail successfully', async () => {
      const seat = {
        id: 'seat_001',
        studyRoomId: 'room_001',
        row: 1,
        col: 1,
        seatNumber: 'A1',
        type: 'normal' as const,
        status: 'available' as const,
        facilities: ['插座']
      };

      mockRequest.mockResolvedValueOnce({
        code: 200,
        message: 'success',
        data: seat,
        timestamp: Date.now()
      });

      const result = await seatApi.getSeatDetail('seat_001');

      expect(result.code).toBe(200);
      expect(result.data.id).toBe('seat_001');
      expect(result.data.seatNumber).toBe('A1');
    });

    it('should handle seat not found', async () => {
      mockRequest.mockResolvedValueOnce({
        code: 30001,
        message: '座位不存在',
        data: null,
        timestamp: Date.now()
      });

      const result = await seatApi.getSeatDetail('nonexistent');

      expect(result.code).toBe(30001);
      expect(result.message).toBe('座位不存在');
    });

    it('should return seat with facilities', async () => {
      const seat = {
        id: 'seat_001',
        studyRoomId: 'room_001',
        row: 1,
        col: 1,
        seatNumber: 'A1',
        type: 'vip' as const,
        status: 'available' as const,
        facilities: ['插座', '台灯', 'WiFi']
      };

      mockRequest.mockResolvedValueOnce({
        code: 200,
        message: 'success',
        data: seat,
        timestamp: Date.now()
      });

      const result = await seatApi.getSeatDetail('seat_001');

      expect(result.code).toBe(200);
      expect(result.data.facilities).toHaveLength(3);
      expect(result.data.facilities).toContain('插座');
    });

    it('should return seat with correct status', async () => {
      const seat = {
        id: 'seat_001',
        studyRoomId: 'room_001',
        row: 1,
        col: 1,
        seatNumber: 'A1',
        type: 'normal' as const,
        status: 'available' as const,
        facilities: ['插座']
      };

      mockRequest.mockResolvedValueOnce({
        code: 200,
        message: 'success',
        data: seat,
        timestamp: Date.now()
      });

      const result = await seatApi.getSeatDetail('seat_001');

      expect(result.code).toBe(200);
      expect(result.data.status).toBe('available');
    });

    it('should return seat with correct type', async () => {
      const seat = {
        id: 'seat_001',
        studyRoomId: 'room_001',
        row: 1,
        col: 1,
        seatNumber: 'A1',
        type: 'vip' as const,
        status: 'available' as const,
        facilities: ['插座', '台灯', '人体工学椅']
      };

      mockRequest.mockResolvedValueOnce({
        code: 200,
        message: 'success',
        data: seat,
        timestamp: Date.now()
      });

      const result = await seatApi.getSeatDetail('seat_001');

      expect(result.code).toBe(200);
      expect(result.data.type).toBe('vip');
    });
  });
});
