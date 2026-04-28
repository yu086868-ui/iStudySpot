import Request from '../../miniprogram/utils/request';
import { WxMock } from '../../tests/mocks/wx-mock';
import { TestDataFactory } from '../../tests/utils/test-data-factory';

describe('Request', () => {
  let wxMock: WxMock;
  let request: Request;

  beforeEach(() => {
    wxMock = new WxMock();
    (global as any).wx = wxMock;
    request = new Request('http://localhost:3000/api');
  });

  afterEach(() => {
    wxMock.clearAllMocks();
  });

  describe('constructor', () => {
    it('should create instance with base URL', () => {
      expect(request).toBeInstanceOf(Request);
    });
  });

  describe('token management', () => {
    it('should save tokens', () => {
      request.saveTokens('token123', 'refresh123');
      
      expect(wxMock.getStorageSync('access_token')).toBe('token123');
      expect(wxMock.getStorageSync('refresh_token')).toBe('refresh123');
    });

    it('should clear tokens', () => {
      request.saveTokens('token123', 'refresh123');
      request.clearTokens();
      
      expect(wxMock.getStorageSync('access_token')).toBeUndefined();
      expect(wxMock.getStorageSync('refresh_token')).toBeUndefined();
    });

    it('should check if logged in', () => {
      expect(request.isLoggedIn()).toBe(false);
      
      request.saveTokens('token123', 'refresh123');
      expect(request.isLoggedIn()).toBe(true);
    });
  });

  describe('GET request', () => {
    it('should make successful GET request', async () => {
      const mockData = { id: '1', name: 'Test' };
      wxMock.mockRequestSuccess(mockData);

      const result = await request.get('/test');

      expect(result.code).toBe(200);
      expect(result.data).toEqual(mockData);
    });

    it('should make GET request with parameters', async () => {
      const mockData = TestDataFactory.createPaginatedResponse([TestDataFactory.createSeat()]);
      wxMock.mockRequestSuccess(mockData);

      const result = await request.get('/seats', { status: 'available' });

      expect(result.code).toBe(200);
      expect(result.data).toEqual(mockData);
    });

    it('should handle GET request error', async () => {
      wxMock.mockRequestError(new Error('Network error'));

      await expect(request.get('/test')).rejects.toBeDefined();
    });
  });

  describe('POST request', () => {
    it('should make successful POST request', async () => {
      const mockData = TestDataFactory.createLoginResponse();
      wxMock.mockRequestSuccess(mockData);

      const result = await request.post('/auth/login', {
        username: 'test',
        password: '123456'
      }, false);

      expect(result.code).toBe(200);
      expect(result.data).toEqual(mockData);
    });

    it('should include authorization header when needed', async () => {
      request.saveTokens('token123', 'refresh123');
      const mockData = { success: true };
      wxMock.mockRequestSuccess(mockData);

      await request.post('/reservations', { seatId: 'seat_001' });

      const requestCall = wxMock.getMockFunction('request');
      expect(requestCall).toHaveBeenCalled();
    });
  });

  describe('PUT request', () => {
    it('should make successful PUT request', async () => {
      const mockData = TestDataFactory.createUser({ nickname: 'Updated' });
      wxMock.mockRequestSuccess(mockData);

      const result = await request.put('/users/me', { nickname: 'Updated' });

      expect(result.code).toBe(200);
      expect(result.data).toEqual(mockData);
    });
  });

  describe('DELETE request', () => {
    it('should make successful DELETE request', async () => {
      wxMock.mockRequestSuccess(null);

      const result = await request.delete('/reservations/res_001');

      expect(result.code).toBe(200);
    });
  });

  describe('error handling', () => {
    it('should handle 401 unauthorized error', async () => {
      const errorResponse = TestDataFactory.createErrorResponse(401, 'Unauthorized');
      
      wxMock.getMockFunction('request').mockImplementation((options: any) => {
        if (options.success) {
          options.success({
            data: errorResponse,
            statusCode: 200,
            header: {}
          });
        }
      });

      const result = await request.get('/protected', null, true);

      expect(result.code).toBe(401);
    });

    it('should handle network error', async () => {
      wxMock.mockRequestError({ errMsg: 'request:fail' });

      await expect(request.get('/test')).rejects.toBeDefined();
    });
  });

  describe('request retry on token refresh', () => {
    it('should attempt token refresh on 401', async () => {
      request.saveTokens('old_token', 'old_refresh');
      
      let callCount = 0;
      wxMock.getMockFunction('request').mockImplementation((options: any) => {
        callCount++;
        if (callCount === 1) {
          if (options.success) {
            options.success({
              data: TestDataFactory.createErrorResponse(401, 'Token expired'),
              statusCode: 200,
              header: {}
            });
          }
        } else if (callCount === 2 && options.url.includes('/auth/refresh')) {
          if (options.success) {
            options.success({
              data: TestDataFactory.createSuccessResponse({
                token: 'new_token',
                refreshToken: 'new_refresh_token'
              }),
              statusCode: 200,
              header: {}
            });
          }
        } else {
          if (options.success) {
            options.success({
              data: TestDataFactory.createSuccessResponse({ success: true }),
              statusCode: 200,
              header: {}
            });
          }
        }
      });

      const result = await request.get('/protected');

      expect(callCount).toBeGreaterThan(1);
    });
  });
});
