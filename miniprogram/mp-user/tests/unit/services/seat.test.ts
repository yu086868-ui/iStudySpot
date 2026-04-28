import { seatApi } from '../../miniprogram/services/seat';
import { TestDataFactory } from '../../tests/utils/test-data-factory';
import { WxMock } from '../../tests/mocks/wx-mock';

describe('seatApi', () => {
  let wxMock: WxMock;

  beforeEach(() => {
    wxMock = new WxMock();
    (global as any).wx = wxMock;
    jest.clearAllMocks();
  });

  afterEach(() => {
    wxMock.clearAllMocks();
  });

  describe('getSeats', () => {
    it('should get seats for study room successfully', async () => {
      const seats = TestDataFactory.createSeats(10);
      const mockResponse = TestDataFactory.createSuccessResponse(seats);
      
      wxMock.getMockFunction('request').mockImplementation((options: any) => {
        if (options.success) {
          options.success({
            data: mockResponse,
            statusCode: 200,
            header: {}
          });
        }
      });

      const result = await seatApi.getSeats('room_001');

      expect(result.code).toBe(200);
      expect(result.data).toHaveLength(10);
    });

    it('should filter seats by status', async () => {
      const seats = [
        TestDataFactory.createSeat({ id: '1', status: 'available' }),
        TestDataFactory.createSeat({ id: '2', status: 'occupied' }),
        TestDataFactory.createSeat({ id: '3', status: 'available' })
      ];
      const mockResponse = TestDataFactory.createSuccessResponse(seats);
      
      wxMock.getMockFunction('request').mockImplementation((options: any) => {
        if (options.success) {
          options.success({
            data: mockResponse,
            statusCode: 200,
            header: {}
          });
        }
      });

      const result = await seatApi.getSeats('room_001', { status: 'available' });

      expect(result.code).toBe(200);
      expect(result.data).toHaveLength(3);
    });

    it('should filter seats by type', async () => {
      const seats = [
        TestDataFactory.createSeat({ id: '1', type: 'normal' }),
        TestDataFactory.createSeat({ id: '2', type: 'vip' }),
        TestDataFactory.createSeat({ id: '3', type: 'normal' })
      ];
      const mockResponse = TestDataFactory.createSuccessResponse(seats);
      
      wxMock.getMockFunction('request').mockImplementation((options: any) => {
        if (options.success) {
          options.success({
            data: mockResponse,
            statusCode: 200,
            header: {}
          });
        }
      });

      const result = await seatApi.getSeats('room_001', { type: 'normal' });

      expect(result.code).toBe(200);
      expect(result.data).toHaveLength(3);
    });

    it('should return empty array when no seats found', async () => {
      const mockResponse = TestDataFactory.createSuccessResponse([]);
      
      wxMock.getMockFunction('request').mockImplementation((options: any) => {
        if (options.success) {
          options.success({
            data: mockResponse,
            statusCode: 200,
            header: {}
          });
        }
      });

      const result = await seatApi.getSeats('room_999');

      expect(result.code).toBe(200);
      expect(result.data).toHaveLength(0);
    });
  });

  describe('getSeatDetail', () => {
    it('should get seat detail successfully', async () => {
      const seat = TestDataFactory.createSeat({ id: 'seat_001', seatNumber: 'A1' });
      const mockResponse = TestDataFactory.createSuccessResponse(seat);
      
      wxMock.getMockFunction('request').mockImplementation((options: any) => {
        if (options.success) {
          options.success({
            data: mockResponse,
            statusCode: 200,
            header: {}
          });
        }
      });

      const result = await seatApi.getSeatDetail('seat_001');

      expect(result.code).toBe(200);
      expect(result.data.id).toBe('seat_001');
      expect(result.data.seatNumber).toBe('A1');
    });

    it('should handle seat not found', async () => {
      const errorResponse = TestDataFactory.createErrorResponse(30001, '座位不存在');
      
      wxMock.getMockFunction('request').mockImplementation((options: any) => {
        if (options.success) {
          options.success({
            data: errorResponse,
            statusCode: 200,
            header: {}
          });
        }
      });

      const result = await seatApi.getSeatDetail('nonexistent');

      expect(result.code).toBe(30001);
      expect(result.message).toBe('座位不存在');
    });

    it('should return seat with facilities', async () => {
      const seat = TestDataFactory.createSeat({ 
        id: 'seat_001',
        facilities: ['插座', '台灯', 'WiFi']
      });
      const mockResponse = TestDataFactory.createSuccessResponse(seat);
      
      wxMock.getMockFunction('request').mockImplementation((options: any) => {
        if (options.success) {
          options.success({
            data: mockResponse,
            statusCode: 200,
            header: {}
          });
        }
      });

      const result = await seatApi.getSeatDetail('seat_001');

      expect(result.code).toBe(200);
      expect(result.data.facilities).toHaveLength(3);
      expect(result.data.facilities).toContain('插座');
    });

    it('should return seat with correct status', async () => {
      const seat = TestDataFactory.createSeat({ 
        id: 'seat_001',
        status: 'available'
      });
      const mockResponse = TestDataFactory.createSuccessResponse(seat);
      
      wxMock.getMockFunction('request').mockImplementation((options: any) => {
        if (options.success) {
          options.success({
            data: mockResponse,
            statusCode: 200,
            header: {}
          });
        }
      });

      const result = await seatApi.getSeatDetail('seat_001');

      expect(result.code).toBe(200);
      expect(result.data.status).toBe('available');
    });

    it('should return seat with correct type', async () => {
      const seat = TestDataFactory.createSeat({ 
        id: 'seat_001',
        type: 'vip'
      });
      const mockResponse = TestDataFactory.createSuccessResponse(seat);
      
      wxMock.getMockFunction('request').mockImplementation((options: any) => {
        if (options.success) {
          options.success({
            data: mockResponse,
            statusCode: 200,
            header: {}
          });
        }
      });

      const result = await seatApi.getSeatDetail('seat_001');

      expect(result.code).toBe(200);
      expect(result.data.type).toBe('vip');
    });
  });
});
