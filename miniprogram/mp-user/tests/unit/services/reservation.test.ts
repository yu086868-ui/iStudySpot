import { reservationApi } from '../../miniprogram/services/reservation';
import { TestDataFactory } from '../../tests/utils/test-data-factory';
import { WxMock } from '../../tests/mocks/wx-mock';

describe('reservationApi', () => {
  let wxMock: WxMock;

  beforeEach(() => {
    wxMock = new WxMock();
    (global as any).wx = wxMock;
    jest.clearAllMocks();
  });

  afterEach(() => {
    wxMock.clearAllMocks();
  });

  describe('createReservation', () => {
    it('should create reservation successfully', async () => {
      const reservation = TestDataFactory.createReservation();
      const mockResponse = TestDataFactory.createSuccessResponse(reservation);
      
      wxMock.getMockFunction('request').mockImplementation((options: any) => {
        if (options.success) {
          options.success({
            data: mockResponse,
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

      expect(result.code).toBe(200);
      expect(result.data.id).toBeDefined();
      expect(result.data.status).toBe('confirmed');
    });

    it('should handle reservation creation failure', async () => {
      const errorResponse = TestDataFactory.createErrorResponse(30002, '该座位已被占用或预约');
      
      wxMock.getMockFunction('request').mockImplementation((options: any) => {
        if (options.success) {
          options.success({
            data: errorResponse,
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

    it('should handle already has active reservation error', async () => {
      const errorResponse = TestDataFactory.createErrorResponse(40001, '您已有进行中的预约，请先取消或完成');
      
      wxMock.getMockFunction('request').mockImplementation((options: any) => {
        if (options.success) {
          options.success({
            data: errorResponse,
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

  describe('getMyReservations', () => {
    it('should get user reservations successfully', async () => {
      const reservations = [
        TestDataFactory.createReservation({ id: 'res_001' }),
        TestDataFactory.createReservation({ id: 'res_002' })
      ];
      const paginatedData = TestDataFactory.createPaginatedResponse(reservations);
      const mockResponse = TestDataFactory.createSuccessResponse(paginatedData);
      
      wxMock.getMockFunction('request').mockImplementation((options: any) => {
        if (options.success) {
          options.success({
            data: mockResponse,
            statusCode: 200,
            header: {}
          });
        }
      });

      const result = await reservationApi.getMyReservations({ status: 'confirmed' });

      expect(result.code).toBe(200);
      expect(result.data.list).toHaveLength(2);
    });

    it('should filter reservations by status', async () => {
      const reservations = [
        TestDataFactory.createReservation({ id: 'res_001', status: 'confirmed' })
      ];
      const paginatedData = TestDataFactory.createPaginatedResponse(reservations);
      const mockResponse = TestDataFactory.createSuccessResponse(paginatedData);
      
      wxMock.getMockFunction('request').mockImplementation((options: any) => {
        if (options.success) {
          options.success({
            data: mockResponse,
            statusCode: 200,
            header: {}
          });
        }
      });

      const result = await reservationApi.getMyReservations({ status: 'confirmed' });

      expect(result.code).toBe(200);
      expect(result.data.list).toHaveLength(1);
    });

    it('should return empty list when no reservations', async () => {
      const paginatedData = TestDataFactory.createPaginatedResponse([]);
      const mockResponse = TestDataFactory.createSuccessResponse(paginatedData);
      
      wxMock.getMockFunction('request').mockImplementation((options: any) => {
        if (options.success) {
          options.success({
            data: mockResponse,
            statusCode: 200,
            header: {}
          });
        }
      });

      const result = await reservationApi.getMyReservations();

      expect(result.code).toBe(200);
      expect(result.data.list).toHaveLength(0);
    });
  });

  describe('getReservationDetail', () => {
    it('should get reservation detail successfully', async () => {
      const reservation = TestDataFactory.createReservation({ id: 'res_001' });
      const mockResponse = TestDataFactory.createSuccessResponse(reservation);
      
      wxMock.getMockFunction('request').mockImplementation((options: any) => {
        if (options.success) {
          options.success({
            data: mockResponse,
            statusCode: 200,
            header: {}
          });
        }
      });

      const result = await reservationApi.getReservationDetail('res_001');

      expect(result.code).toBe(200);
      expect(result.data.id).toBe('res_001');
    });

    it('should handle reservation not found', async () => {
      const errorResponse = TestDataFactory.createErrorResponse(40004, '预约不存在');
      
      wxMock.getMockFunction('request').mockImplementation((options: any) => {
        if (options.success) {
          options.success({
            data: errorResponse,
            statusCode: 200,
            header: {}
          });
        }
      });

      const result = await reservationApi.getReservationDetail('nonexistent');

      expect(result.code).toBe(40004);
      expect(result.message).toBe('预约不存在');
    });
  });

  describe('cancelReservation', () => {
    it('should cancel reservation successfully', async () => {
      const mockResponse = TestDataFactory.createSuccessResponse(null, '预约已取消');
      
      wxMock.getMockFunction('request').mockImplementation((options: any) => {
        if (options.success) {
          options.success({
            data: mockResponse,
            statusCode: 200,
            header: {}
          });
        }
      });

      const result = await reservationApi.cancelReservation('res_001');

      expect(result.code).toBe(200);
      expect(result.message).toBe('预约已取消');
    });

    it('should handle cancel checked-in reservation error', async () => {
      const errorResponse = TestDataFactory.createErrorResponse(40007, '预约已签到，无法取消');
      
      wxMock.getMockFunction('request').mockImplementation((options: any) => {
        if (options.success) {
          options.success({
            data: errorResponse,
            statusCode: 200,
            header: {}
          });
        }
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
      const mockResponse = TestDataFactory.createSuccessResponse(rules);
      
      wxMock.getMockFunction('request').mockImplementation((options: any) => {
        if (options.success) {
          options.success({
            data: mockResponse,
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
