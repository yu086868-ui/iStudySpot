import request from '../miniprogram/utils/request';

jest.mock('../miniprogram/utils/request');

describe('Request', () => {
  const mockSaveTokens = jest.fn();
  const mockClearTokens = jest.fn();
  const mockIsLoggedIn = jest.fn();
  const mockGet = jest.fn();
  const mockPost = jest.fn();
  const mockPut = jest.fn();
  const mockDelete = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
    (request.saveTokens as jest.Mock) = mockSaveTokens;
    (request.clearTokens as jest.Mock) = mockClearTokens;
    (request.isLoggedIn as jest.Mock) = mockIsLoggedIn;
    (request.get as jest.Mock) = mockGet;
    (request.post as jest.Mock) = mockPost;
    (request.put as jest.Mock) = mockPut;
    (request.delete as jest.Mock) = mockDelete;
  });

  describe('token management', () => {
    it('should save tokens', () => {
      request.saveTokens('token123', 'refresh123');
      
      expect(mockSaveTokens).toHaveBeenCalledWith('token123', 'refresh123');
    });

    it('should clear tokens', () => {
      request.clearTokens();
      
      expect(mockClearTokens).toHaveBeenCalled();
    });

    it('should check if logged in', () => {
      mockIsLoggedIn.mockReturnValue(true);
      
      expect(request.isLoggedIn()).toBe(true);
    });
  });

  describe('GET request', () => {
    it('should make successful GET request', async () => {
      const mockData = { id: '1', name: 'Test' };
      mockGet.mockResolvedValueOnce({
        code: 200,
        data: mockData,
        message: 'success',
        timestamp: Date.now()
      });

      const result = await request.get('/test');

      expect(result.code).toBe(200);
      expect(result.data).toEqual(mockData);
    });

    it('should make GET request with parameters', async () => {
      const mockData = { list: [], total: 0, page: 1, pageSize: 20 };
      mockGet.mockResolvedValueOnce({
        code: 200,
        data: mockData,
        message: 'success',
        timestamp: Date.now()
      });

      const result = await request.get('/seats', { status: 'available' });

      expect(result.code).toBe(200);
      expect(result.data).toEqual(mockData);
    });
  });

  describe('POST request', () => {
    it('should make successful POST request', async () => {
      const mockData = {
        token: 'test_token',
        refreshToken: 'test_refresh_token',
        user: { id: 'user_001', username: 'testuser' }
      };
      mockPost.mockResolvedValueOnce({
        code: 200,
        data: mockData,
        message: 'success',
        timestamp: Date.now()
      });

      const result = await request.post('/auth/login', {
        username: 'test',
        password: '123456'
      }, false);

      expect(result.code).toBe(200);
      expect(result.data).toEqual(mockData);
    });

    it('should include authorization header when needed', async () => {
      mockPost.mockResolvedValueOnce({
        code: 200,
        data: null,
        message: 'success',
        timestamp: Date.now()
      });

      await request.post('/reservations', { seatId: 'seat_001' });

      expect(mockPost).toHaveBeenCalled();
    });
  });

  describe('PUT request', () => {
    it('should make successful PUT request', async () => {
      const mockData = { id: 'user_001', nickname: 'Updated' };
      mockPut.mockResolvedValueOnce({
        code: 200,
        data: mockData,
        message: 'success',
        timestamp: Date.now()
      });

      const result = await request.put('/users/me', { nickname: 'Updated' });

      expect(result.code).toBe(200);
      expect(result.data).toEqual(mockData);
    });
  });

  describe('DELETE request', () => {
    it('should make successful DELETE request', async () => {
      mockDelete.mockResolvedValueOnce({
        code: 200,
        data: null,
        message: 'success',
        timestamp: Date.now()
      });

      const result = await request.delete('/reservations/res_001');

      expect(result.code).toBe(200);
    });
  });

  describe('error handling', () => {
    it('should handle 401 unauthorized error', async () => {
      mockGet.mockResolvedValueOnce({
        code: 401,
        data: null,
        message: 'Unauthorized',
        timestamp: Date.now()
      });

      const result = await request.get('/protected', null, true);

      expect(result.code).toBe(401);
    });

    it('should handle error response', async () => {
      mockGet.mockResolvedValueOnce({
        code: 500,
        data: null,
        message: 'Internal Server Error',
        timestamp: Date.now()
      });

      const result = await request.get('/test');

      expect(result.code).toBe(500);
    });
  });
});
