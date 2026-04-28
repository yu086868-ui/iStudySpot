import { checkInApi } from '../miniprogram/services/checkin';
import { reservationApi } from '../miniprogram/services/reservation';
import { TestDataFactory } from '../tests/utils/test-data-factory';
import { WxMock } from '../tests/mocks/wx-mock';

describe('Check-in Flow E2E Tests', () => {
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

  describe('Complete check-in flow', () => {
    it('should check in successfully with reservation', async () => {
      const checkInResponse = {
        checkInRecordId: 'checkin_001',
        checkInTime: new Date().toISOString(),
        reservationId: 'res_001',
        seatId: 'seat_001'
      };

      wxMock.getMockFunction('request').mockImplementation((options: any) => {
        if (options.url.includes('/checkin') && options.method === 'POST') {
          if (options.success) {
            options.success({
              data: TestDataFactory.createSuccessResponse(checkInResponse),
              statusCode: 200,
              header: {}
            });
          }
        }
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
      wxMock.getMockFunction('request').mockImplementation((options: any) => {
        if (options.success) {
          options.success({
            data: TestDataFactory.createErrorResponse(50002, '已经签到，无需重复签到'),
            statusCode: 200,
            header: {}
          });
        }
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

      wxMock.getMockFunction('request').mockImplementation((options: any) => {
        if (options.url.includes('/checkout')) {
          if (options.success) {
            options.success({
              data: TestDataFactory.createSuccessResponse(checkOutResponse),
              statusCode: 200,
              header: {}
            });
          }
        }
      });

      const result = await checkInApi.checkOut({
        checkInRecordId: 'checkin_001'
      });

      expect(result.code).toBe(200);
      expect(result.data.checkOutTime).toBeDefined();
      expect(result.data.duration).toBe(120);
    });

    it('should handle already checked out error', async () => {
      wxMock.getMockFunction('request').mockImplementation((options: any) => {
        if (options.success) {
          options.success({
            data: TestDataFactory.createErrorResponse(50004, '已经签退'),
            statusCode: 200,
            header: {}
          });
        }
      });

      const result = await checkInApi.checkOut({
        checkInRecordId: 'checkin_001'
      });

      expect(result.code).toBe(50004);
      expect(result.message).toBe('已经签退');
    });

    it('should handle check-in record not found', async () => {
      wxMock.getMockFunction('request').mockImplementation((options: any) => {
        if (options.success) {
          options.success({
            data: TestDataFactory.createErrorResponse(50003, '签到记录不存在'),
            statusCode: 200,
            header: {}
          });
        }
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
      const checkInRecord = TestDataFactory.createCheckInRecord();
      const statusResponse = {
        isCheckedIn: true,
        checkInRecord
      };

      wxMock.getMockFunction('request').mockImplementation((options: any) => {
        if (options.url.includes('/checkin/current')) {
          if (options.success) {
            options.success({
              data: TestDataFactory.createSuccessResponse(statusResponse),
              statusCode: 200,
              header: {}
            });
          }
        }
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

      wxMock.getMockFunction('request').mockImplementation((options: any) => {
        if (options.success) {
          options.success({
            data: TestDataFactory.createSuccessResponse(statusResponse),
            statusCode: 200,
            header: {}
          });
        }
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
        TestDataFactory.createCheckInRecord({ id: 'checkin_001' }),
        TestDataFactory.createCheckInRecord({ id: 'checkin_002' })
      ];

      wxMock.getMockFunction('request').mockImplementation((options: any) => {
        if (options.success) {
          options.success({
            data: TestDataFactory.createSuccessResponse(
              TestDataFactory.createPaginatedResponse(records)
            ),
            statusCode: 200,
            header: {}
          });
        }
      });

      const result = await checkInApi.getMyCheckInRecords();

      expect(result.code).toBe(200);
      expect(result.data.list).toHaveLength(2);
    });

    it('should filter check-in records by date', async () => {
      const records = [TestDataFactory.createCheckInRecord()];

      wxMock.getMockFunction('request').mockImplementation((options: any) => {
        if (options.success) {
          options.success({
            data: TestDataFactory.createSuccessResponse(
              TestDataFactory.createPaginatedResponse(records)
            ),
            statusCode: 200,
            header: {}
          });
        }
      });

      const result = await checkInApi.getMyCheckInRecords({
        startDate: '2024-01-01',
        endDate: '2024-12-31'
      });

      expect(result.code).toBe(200);
      expect(result.data.list).toHaveLength(1);
    });
  });

  describe('Complete study session flow', () => {
    it('should complete full study session from reservation to check-out', async () => {
      const reservation = TestDataFactory.createReservation();
      const checkInResponse = {
        checkInRecordId: 'checkin_001',
        checkInTime: new Date().toISOString(),
        reservationId: reservation.id,
        seatId: 'seat_001'
      };
      const checkOutResponse = {
        checkOutTime: new Date(Date.now() + 2 * 60 * 60 * 1000).toISOString(),
        duration: 120
      };

      let requestCount = 0;
      wxMock.getMockFunction('request').mockImplementation((options: any) => {
        requestCount++;
        if (options.url.includes('/reservations') && options.method === 'POST') {
          if (options.success) {
            options.success({
              data: TestDataFactory.createSuccessResponse(reservation),
              statusCode: 200,
              header: {}
            });
          }
        } else if (options.url.includes('/checkin') && options.method === 'POST') {
          if (options.success) {
            options.success({
              data: TestDataFactory.createSuccessResponse(checkInResponse),
              statusCode: 200,
              header: {}
            });
          }
        } else if (options.url.includes('/checkout')) {
          if (options.success) {
            options.success({
              data: TestDataFactory.createSuccessResponse(checkOutResponse),
              statusCode: 200,
              header: {}
            });
          }
        }
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
