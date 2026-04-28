import { reservationApi } from '../../miniprogram/services/reservation';
import { seatApi } from '../../miniprogram/services/seat';
import { checkInApi } from '../../miniprogram/services/checkin';
import { studyRoomApi } from '../../miniprogram/services/studyroom';
import { TestDataFactory } from '../../tests/utils/test-data-factory';
import { WxMock } from '../../tests/mocks/wx-mock';

describe('Seat Reservation Flow E2E Tests', () => {
  let wxMock: WxMock;

  beforeEach(() => {
    wxMock = new WxMock();
    (global as any).wx = wxMock;
    wxMock.setStorageSync('access_token', 'test_token');
    jest.clearAllMocks();
  });

  afterEach(() => {
    wxMock.clearAllMocks();
  });

  describe('Complete reservation flow', () => {
    it('should complete seat reservation successfully', async () => {
      const studyRooms = [TestDataFactory.createStudyRoom()];
      const seats = TestDataFactory.createSeats(10);
      const reservation = TestDataFactory.createReservation();

      wxMock.getMockFunction('request').mockImplementation((options: any) => {
        if (options.url.includes('/studyrooms') && !options.url.includes('seats')) {
          if (options.success) {
            options.success({
              data: TestDataFactory.createSuccessResponse(
                TestDataFactory.createPaginatedResponse(studyRooms)
              ),
              statusCode: 200,
              header: {}
            });
          }
        } else if (options.url.includes('/seats')) {
          if (options.success) {
            options.success({
              data: TestDataFactory.createSuccessResponse(seats),
              statusCode: 200,
              header: {}
            });
          }
        } else if (options.url.includes('/reservations') && options.method === 'POST') {
          if (options.success) {
            options.success({
              data: TestDataFactory.createSuccessResponse(reservation),
              statusCode: 200,
              header: {}
            });
          }
        }
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
      wxMock.getMockFunction('request').mockImplementation((options: any) => {
        if (options.success) {
          options.success({
            data: TestDataFactory.createErrorResponse(30002, '该座位已被占用或预约'),
            statusCode: 200,
            header: {}
          });
        }
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
      wxMock.getMockFunction('request').mockImplementation((options: any) => {
        if (options.success) {
          options.success({
            data: TestDataFactory.createErrorResponse(40001, '您已有进行中的预约，请先取消或完成'),
            statusCode: 200,
            header: {}
          });
        }
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
        TestDataFactory.createReservation({ id: 'res_001' }),
        TestDataFactory.createReservation({ id: 'res_002' })
      ];

      wxMock.getMockFunction('request').mockImplementation((options: any) => {
        if (options.success) {
          options.success({
            data: TestDataFactory.createSuccessResponse(
              TestDataFactory.createPaginatedResponse(reservations)
            ),
            statusCode: 200,
            header: {}
          });
        }
      });

      const result = await reservationApi.getMyReservations();

      expect(result.code).toBe(200);
      expect(result.data.list).toHaveLength(2);
    });

    it('should get reservation detail', async () => {
      const reservation = TestDataFactory.createReservation({ id: 'res_001' });

      wxMock.getMockFunction('request').mockImplementation((options: any) => {
        if (options.success) {
          options.success({
            data: TestDataFactory.createSuccessResponse(reservation),
            statusCode: 200,
            header: {}
          });
        }
      });

      const result = await reservationApi.getReservationDetail('res_001');

      expect(result.code).toBe(200);
      expect(result.data.id).toBe('res_001');
    });

    it('should cancel reservation', async () => {
      wxMock.getMockFunction('request').mockImplementation((options: any) => {
        if (options.success) {
          options.success({
            data: TestDataFactory.createSuccessResponse(null, '预约已取消'),
            statusCode: 200,
            header: {}
          });
        }
      });

      const result = await reservationApi.cancelReservation('res_001');

      expect(result.code).toBe(200);
      expect(result.message).toBe('预约已取消');
    });
  });

  describe('Seat selection and filtering', () => {
    it('should filter seats by status', async () => {
      const seats = [
        TestDataFactory.createSeat({ id: '1', status: 'available' }),
        TestDataFactory.createSeat({ id: '2', status: 'occupied' }),
        TestDataFactory.createSeat({ id: '3', status: 'available' })
      ];

      wxMock.getMockFunction('request').mockImplementation((options: any) => {
        if (options.success) {
          options.success({
            data: TestDataFactory.createSuccessResponse(seats),
            statusCode: 200,
            header: {}
          });
        }
      });

      const result = await seatApi.getSeats('room_001', { status: 'available' });

      expect(result.code).toBe(200);
      expect(result.data.length).toBeGreaterThan(0);
    });

    it('should filter seats by type', async () => {
      const seats = [
        TestDataFactory.createSeat({ id: '1', type: 'vip' }),
        TestDataFactory.createSeat({ id: '2', type: 'normal' })
      ];

      wxMock.getMockFunction('request').mockImplementation((options: any) => {
        if (options.success) {
          options.success({
            data: TestDataFactory.createSuccessResponse(seats),
            statusCode: 200,
            header: {}
          });
        }
      });

      const result = await seatApi.getSeats('room_001', { type: 'vip' });

      expect(result.code).toBe(200);
    });

    it('should get seat detail with facilities', async () => {
      const seat = TestDataFactory.createSeat({
        id: 'seat_001',
        facilities: ['插座', '台灯', 'WiFi']
      });

      wxMock.getMockFunction('request').mockImplementation((options: any) => {
        if (options.success) {
          options.success({
            data: TestDataFactory.createSuccessResponse(seat),
            statusCode: 200,
            header: {}
          });
        }
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

      wxMock.getMockFunction('request').mockImplementation((options: any) => {
        if (options.success) {
          options.success({
            data: TestDataFactory.createSuccessResponse(rules),
            statusCode: 200,
            header: {}
          });
        }
      });

      const result = await reservationApi.getReservationRules();

      expect(result.code).toBe(200);
      expect(result.data.maxAdvanceDays).toBe(7);
      expect(result.data.maxDurationHours).toBe(4);
    });
  });
});
