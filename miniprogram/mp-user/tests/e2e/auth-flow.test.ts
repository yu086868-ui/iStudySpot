import { authApi } from '../miniprogram/services/auth';
import mockManager from '../miniprogram/utils/mock';
import request from '../miniprogram/utils/request';

jest.mock('../miniprogram/utils/mock');
jest.mock('../miniprogram/utils/request');

describe('User Authentication Flow E2E Tests', () => {
  const mockRequest = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
    (mockManager.isEnabled as jest.Mock).mockReturnValue(true);
    (mockManager.request as jest.Mock) = mockRequest;
    (request.saveTokens as jest.Mock) = jest.fn();
    (request.clearTokens as jest.Mock) = jest.fn();
  });

  afterEach(() => {
    mockRequest.mockReset();
  });

  describe('Complete login flow', () => {
    it('should complete user login successfully', async () => {
      const loginResponse = {
        token: 'test_token_123',
        refreshToken: 'test_refresh_token_123',
        user: {
          id: 'user_001',
          username: 'testuser',
          nickname: '测试用户',
          avatar: 'https://example.com/avatar.jpg'
        }
      };

      mockRequest.mockResolvedValueOnce({
        code: 200,
        message: '登录成功',
        data: loginResponse,
        timestamp: Date.now()
      });

      const result = await authApi.login({
        username: 'testuser',
        password: 'password123'
      });

      expect(result.code).toBe(200);
      expect(result.data.token).toBeDefined();
      expect(request.saveTokens).toHaveBeenCalledWith(loginResponse.token, loginResponse.refreshToken);
    });

    it('should handle login failure', async () => {
      mockRequest.mockResolvedValueOnce({
        code: 10001,
        message: '用户名或密码错误',
        data: null,
        timestamp: Date.now()
      });

      const result = await authApi.login({
        username: 'wronguser',
        password: 'wrongpass'
      });

      expect(result.code).toBe(10001);
      expect(result.message).toBe('用户名或密码错误');
    });
  });

  describe('Complete registration flow', () => {
    it('should register new user successfully', async () => {
      mockRequest.mockResolvedValueOnce({
        code: 200,
        message: '注册成功',
        data: { userId: 'user_001' },
        timestamp: Date.now()
      });

      const result = await authApi.register({
        username: 'newuser',
        password: 'password123',
        nickname: '新用户',
        phone: '13800138000',
        studentId: '2020001'
      });

      expect(result.code).toBe(200);
      expect(result.data.userId).toBeDefined();
    });
  });

  describe('Token refresh flow', () => {
    it('should refresh expired token', async () => {
      const newTokens = {
        token: 'new_token',
        refreshToken: 'new_refresh_token'
      };

      mockRequest.mockResolvedValueOnce({
        code: 200,
        message: '刷新成功',
        data: newTokens,
        timestamp: Date.now()
      });

      const result = await authApi.refreshToken('old_refresh_token');

      expect(result.code).toBe(200);
      expect(result.data.token).toBe('new_token');
    });
  });

  describe('Logout flow', () => {
    it('should logout and clear tokens', async () => {
      mockRequest.mockResolvedValueOnce({
        code: 200,
        message: '登出成功',
        data: null,
        timestamp: Date.now()
      });

      const result = await authApi.logout();

      expect(result.code).toBe(200);
      expect(request.clearTokens).toHaveBeenCalled();
    });
  });
});
