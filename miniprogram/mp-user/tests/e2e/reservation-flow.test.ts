import { reservationApi } from '../miniprogram/services/reservation';
import { seatApi } from '../miniprogram/services/seat';
import { studyRoomApi } from '../miniprogram/services/studyroom';
import mockManager from '../miniprogram/utils/mock';

jest.mock('../miniprogram/utils/mock');

describe('Seat Reservation Flow E2E Tests', () => {
  const mockRequest = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
    (mockManager.isEnabled as jest.Mock).mockReturnValue(true);
    (mockManager.request as jest.Mock) = mockRequest;
  });

  afterEach(() => {
    mockRequest.mockReset();
  });

  describe('Complete reservation flow', () => {
    it('should complete seat reservation successfully', async () => {
      const studyRooms = [
        {
          id: 'room_001',
          name: '图书馆一楼自习室',
          description: '安静舒适的学习环境',
          location: '图书馆一楼东侧',
          floor: 1,
          capacity: 50,
          openTime: '08:00',
          closeTime: '22:00',
          facilities: ['WiFi', '空调', '插座'],
          image: 'https://example.com/room1.jpg',
          status: 'open' as const
        }
      ];
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
        }
      ];
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

      mockRequest
        .mockResolvedValueOnce({
          code: 200,
          message: 'success',
          data: { list: studyRooms, total: 1, page: 1, pageSize: 20 },
          timestamp: Date.now()
        })
        .mockResolvedValueOnce({
          code: 200,
          message: 'success',
          data: seats,
          timestamp: Date.now()
        })
        .mockResolvedValueOnce({
          code: 200,
          message: '预约成功',
          data: reservation,
          timestamp: Date.now()
        });

      const roomsResult = await studyRoomApi.getStudyRooms();
      expect(roomsResult.code).toBe(200);
      expect(roomsResult.data.list).toHaveLength(1);

      const seatsResult = await seatApi.getSeats('room_001');
      expect(seatsResult.code).toBe(200);
      expect(seatsResult.data.length).toBeGreaterThan(0);

      const reservationResult = await reservationApi.createReservation({
        studyRoomId: 'room_001',
        seatId: 'seat_001',
        startTime: new Date().toISOString(),
        endTime: new Date(Date.now() + 2 * 60 * 60 * 1000).toISOString()
      });
      expect(reservationResult.code).toBe(200);
      expect(reservationResult.data.status).toBe('confirmed');
    });

    it('should handle reservation conflict', async () => {
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

    it('should handle already has active reservation', async () => {
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

  describe('View and manage reservations', () => {
    it('should list user reservations', async () => {
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

      const result = await reservationApi.getMyReservations();

      expect(result.code).toBe(200);
      expect(result.data.list).toHaveLength(2);
    });

    it('should get reservation detail', async () => {
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

    it('should cancel reservation', async () => {
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
  });

  describe('Seat selection and filtering', () => {
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
      expect(result.data.length).toBeGreaterThan(0);
    });

    it('should filter seats by type', async () => {
      const seats = [
        {
          id: 'seat_001',
          studyRoomId: 'room_001',
          row: 1,
          col: 1,
          seatNumber: 'A1',
          type: 'vip' as const,
          status: 'available' as const,
          facilities: ['插座', '台灯', '人体工学椅']
        },
        {
          id: 'seat_002',
          studyRoomId: 'room_001',
          row: 1,
          col: 2,
          seatNumber: 'A2',
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

      const result = await seatApi.getSeats('room_001', { type: 'vip' });

      expect(result.code).toBe(200);
    });

    it('should get seat detail with facilities', async () => {
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
    });
  });

  describe('Reservation rules', () => {
    it('should get reservation rules', async () => {
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
