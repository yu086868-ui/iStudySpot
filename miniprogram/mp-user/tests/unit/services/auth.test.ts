import { authApi } from '../../miniprogram/services/auth';
import { TestDataFactory } from '../../tests/utils/test-data-factory';
import { WxMock } from '../../tests/mocks/wx-mock';

describe('authApi', () => {
  let wxMock: WxMock;

  beforeEach(() => {
    wxMock = new WxMock();
    (global as any).wx = wxMock;
    jest.clearAllMocks();
  });

  afterEach(() => {
    wxMock.clearAllMocks();
  });

  describe('login', () => {
    it('should login successfully', async () => {
      const loginResponse = TestDataFactory.createLoginResponse();
      const mockResponse = TestDataFactory.createSuccessResponse(loginResponse);
      
      wxMock.getMockFunction('request').mockImplementation((options: any) => {
        if (options.success) {
          options.success({
            data: mockResponse,
            statusCode: 200,
            header: {}
          });
        }
      });

      const result = await authApi.login({
        username: 'testuser',
        password: 'password123'
      });

      expect(result.code).toBe(200);
      expect(result.data.token).toBeDefined();
      expect(result.data.user.username).toBe('testuser');
    });

    it('should handle login failure with wrong credentials', async () => {
      const errorResponse = TestDataFactory.createErrorResponse(10001, '用户名或密码错误');
      
      wxMock.getMockFunction('request').mockImplementation((options: any) => {
        if (options.success) {
          options.success({
            data: errorResponse,
            statusCode: 200,
            header: {}
          });
        }
      });

      const result = await authApi.login({
        username: 'wronguser',
        password: 'wrongpass'
      });

      expect(result.code).toBe(10001);
      expect(result.message).toBe('用户名或密码错误');
    });

    it('should save tokens after successful login', async () => {
      const loginResponse = TestDataFactory.createLoginResponse();
      const mockResponse = TestDataFactory.createSuccessResponse(loginResponse);
      
      wxMock.getMockFunction('request').mockImplementation((options: any) => {
        if (options.success) {
          options.success({
            data: mockResponse,
            statusCode: 200,
            header: {}
          });
        }
      });

      await authApi.login({
        username: 'testuser',
        password: 'password123'
      });

      expect(wxMock.getStorageSync('access_token')).toBe(loginResponse.token);
      expect(wxMock.getStorageSync('refresh_token')).toBe(loginResponse.refreshToken);
    });
  });

  describe('register', () => {
    it('should register successfully', async () => {
      const mockResponse = TestDataFactory.createSuccessResponse({ userId: 'user_001' });
      
      wxMock.getMockFunction('request').mockImplementation((options: any) => {
        if (options.success) {
          options.success({
            data: mockResponse,
            statusCode: 200,
            header: {}
          });
        }
      });

      const result = await authApi.register({
        username: 'newuser',
        password: 'password123',
        nickname: '新用户',
        phone: '13800138000',
        studentId: '2020001'
      });

      expect(result.code).toBe(200);
      expect(result.data.userId).toBe('user_001');
    });

    it('should handle registration failure with existing username', async () => {
      const errorResponse = TestDataFactory.createErrorResponse(10002, '用户已存在');
      
      wxMock.getMockFunction('request').mockImplementation((options: any) => {
        if (options.success) {
          options.success({
            data: errorResponse,
            statusCode: 200,
            header: {}
          });
        }
      });

      const result = await authApi.register({
        username: 'existinguser',
        password: 'password123',
        nickname: '新用户',
        phone: '13800138000',
        studentId: '2020001'
      });

      expect(result.code).toBe(10002);
      expect(result.message).toBe('用户已存在');
    });
  });

  describe('refreshToken', () => {
    it('should refresh token successfully', async () => {
      const newTokens = {
        token: 'new_token_123',
        refreshToken: 'new_refresh_token_123'
      };
      const mockResponse = TestDataFactory.createSuccessResponse(newTokens);
      
      wxMock.getMockFunction('request').mockImplementation((options: any) => {
        if (options.success) {
          options.success({
            data: mockResponse,
            statusCode: 200,
            header: {}
          });
        }
      });

      const result = await authApi.refreshToken('old_refresh_token');

      expect(result.code).toBe(200);
      expect(result.data.token).toBe('new_token_123');
    });

    it('should save new tokens after refresh', async () => {
      const newTokens = {
        token: 'new_token_123',
        refreshToken: 'new_refresh_token_123'
      };
      const mockResponse = TestDataFactory.createSuccessResponse(newTokens);
      
      wxMock.getMockFunction('request').mockImplementation((options: any) => {
        if (options.success) {
          options.success({
            data: mockResponse,
            statusCode: 200,
            header: {}
          });
        }
      });

      await authApi.refreshToken('old_refresh_token');

      expect(wxMock.getStorageSync('access_token')).toBe('new_token_123');
      expect(wxMock.getStorageSync('refresh_token')).toBe('new_refresh_token_123');
    });
  });

  describe('logout', () => {
    it('should logout successfully', async () => {
      wxMock.setStorageSync('access_token', 'token123');
      wxMock.setStorageSync('refresh_token', 'refresh123');
      
      const mockResponse = TestDataFactory.createSuccessResponse(null);
      
      wxMock.getMockFunction('request').mockImplementation((options: any) => {
        if (options.success) {
          options.success({
            data: mockResponse,
            statusCode: 200,
            header: {}
          });
        }
      });

      const result = await authApi.logout();

      expect(result.code).toBe(200);
      expect(wxMock.getStorageSync('access_token')).toBeUndefined();
      expect(wxMock.getStorageSync('refresh_token')).toBeUndefined();
    });

    it('should clear tokens even if request fails', async () => {
      wxMock.setStorageSync('access_token', 'token123');
      wxMock.setStorageSync('refresh_token', 'refresh123');
      
      wxMock.getMockFunction('request').mockImplementation((options: any) => {
        if (options.fail) {
          options.fail({ errMsg: 'request:fail' });
        }
      });

      try {
        await authApi.logout();
      } catch (error) {
        expect(wxMock.getStorageSync('access_token')).toBeUndefined();
        expect(wxMock.getStorageSync('refresh_token')).toBeUndefined();
      }
    });
  });
});
