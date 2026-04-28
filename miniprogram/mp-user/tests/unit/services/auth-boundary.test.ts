import { authApi } from '../miniprogram/services/auth';
import mockManager from '../miniprogram/utils/mock';
import request from '../miniprogram/utils/request';

jest.mock('../miniprogram/utils/mock');
jest.mock('../miniprogram/utils/request');

describe('authApi - Boundary & Error Tests', () => {
  const mockRequest = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
    (mockManager.isEnabled as jest.Mock).mockReturnValue(true);
    (mockManager.request as jest.Mock) = mockRequest;
    (request.saveTokens as jest.Mock) = jest.fn();
    (request.clearTokens as jest.Mock) = jest.fn();
  });

  describe('Login - Boundary Tests', () => {
    it('should handle empty username', async () => {
      mockRequest.mockResolvedValueOnce({
        code: 10001,
        message: '用户名或密码错误',
        data: null,
        timestamp: Date.now()
      });

      const result = await authApi.login({
        username: '',
        password: 'password123'
      });

      expect(result.code).toBe(10001);
    });

    it('should handle empty password', async () => {
      mockRequest.mockResolvedValueOnce({
        code: 10001,
        message: '用户名或密码错误',
        data: null,
        timestamp: Date.now()
      });

      const result = await authApi.login({
        username: 'testuser',
        password: ''
      });

      expect(result.code).toBe(10001);
    });

    it('should handle very long username', async () => {
      const longUsername = 'a'.repeat(1000);
      
      mockRequest.mockResolvedValueOnce({
        code: 10001,
        message: '用户名或密码错误',
        data: null,
        timestamp: Date.now()
      });

      const result = await authApi.login({
        username: longUsername,
        password: 'password123'
      });

      expect(result.code).toBe(10001);
    });

    it('should handle special characters in username', async () => {
      mockRequest.mockResolvedValueOnce({
        code: 10001,
        message: '用户名或密码错误',
        data: null,
        timestamp: Date.now()
      });

      const result = await authApi.login({
        username: 'user<script>alert(1)</script>',
        password: 'password123'
      });

      expect(result.code).toBe(10001);
    });

    it('should handle SQL injection attempt', async () => {
      mockRequest.mockResolvedValueOnce({
        code: 10001,
        message: '用户名或密码错误',
        data: null,
        timestamp: Date.now()
      });

      const result = await authApi.login({
        username: "admin' OR '1'='1",
        password: "anything' OR '1'='1"
      });

      expect(result.code).toBe(10001);
    });
  });

  describe('Register - Boundary Tests', () => {
    it('should handle missing required fields', async () => {
      mockRequest.mockResolvedValueOnce({
        code: 10003,
        message: '参数错误',
        data: null,
        timestamp: Date.now()
      });

      const result = await authApi.register({
        username: '',
        password: '',
        nickname: '',
        phone: '',
        studentId: ''
      });

      expect(result.code).toBe(10003);
    });

    it('should handle invalid phone number format', async () => {
      mockRequest.mockResolvedValueOnce({
        code: 10004,
        message: '手机号格式不正确',
        data: null,
        timestamp: Date.now()
      });

      const result = await authApi.register({
        username: 'newuser',
        password: 'password123',
        nickname: '新用户',
        phone: 'invalid-phone',
        studentId: '2020001'
      });

      expect(result.code).toBe(10004);
    });

    it('should handle invalid student ID format', async () => {
      mockRequest.mockResolvedValueOnce({
        code: 10005,
        message: '学号格式不正确',
        data: null,
        timestamp: Date.now()
      });

      const result = await authApi.register({
        username: 'newuser',
        password: 'password123',
        nickname: '新用户',
        phone: '13800138000',
        studentId: 'invalid'
      });

      expect(result.code).toBe(10005);
    });
  });

  describe('Token Refresh - Error Tests', () => {
    it('should handle expired refresh token', async () => {
      mockRequest.mockResolvedValueOnce({
        code: 401,
        message: '刷新令牌已过期',
        data: null,
        timestamp: Date.now()
      });

      const result = await authApi.refreshToken('expired_refresh_token');

      expect(result.code).toBe(401);
    });

    it('should handle invalid refresh token', async () => {
      mockRequest.mockResolvedValueOnce({
        code: 401,
        message: '无效的刷新令牌',
        data: null,
        timestamp: Date.now()
      });

      const result = await authApi.refreshToken('invalid_token');

      expect(result.code).toBe(401);
    });

    it('should handle empty refresh token', async () => {
      mockRequest.mockResolvedValueOnce({
        code: 401,
        message: '刷新令牌不能为空',
        data: null,
        timestamp: Date.now()
      });

      const result = await authApi.refreshToken('');

      expect(result.code).toBe(401);
    });
  });
});
