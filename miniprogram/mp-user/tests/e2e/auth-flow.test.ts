import { authApi } from '../../miniprogram/services/auth';
import { reservationApi } from '../../miniprogram/services/reservation';
import { seatApi } from '../../miniprogram/services/seat';
import { checkInApi } from '../../miniprogram/services/checkin';
import { TestDataFactory } from '../../tests/utils/test-data-factory';
import { WxMock } from '../../tests/mocks/wx-mock';

describe('User Authentication Flow E2E Tests', () => {
  let wxMock: WxMock;

  beforeEach(() => {
    wxMock = new WxMock();
    (global as any).wx = wxMock;
    jest.clearAllMocks();
  });

  afterEach(() => {
    wxMock.clearAllMocks();
  });

  describe('Complete login flow', () => {
    it('should complete user login successfully', async () => {
      const loginResponse = TestDataFactory.createLoginResponse();
      
      wxMock.getMockFunction('request').mockImplementation((options: any) => {
        if (options.url.includes('/auth/login')) {
          if (options.success) {
            options.success({
              data: TestDataFactory.createSuccessResponse(loginResponse),
              statusCode: 200,
              header: {}
            });
          }
        }
      });

      const result = await authApi.login({
        username: 'testuser',
        password: 'password123'
      });

      expect(result.code).toBe(200);
      expect(result.data.token).toBeDefined();
      expect(wxMock.getStorageSync('access_token')).toBe(loginResponse.token);
    });

    it('should handle login failure', async () => {
      wxMock.getMockFunction('request').mockImplementation((options: any) => {
        if (options.success) {
          options.success({
            data: TestDataFactory.createErrorResponse(10001, '用户名或密码错误'),
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
  });

  describe('Complete registration flow', () => {
    it('should register new user successfully', async () => {
      wxMock.getMockFunction('request').mockImplementation((options: any) => {
        if (options.url.includes('/auth/register')) {
          if (options.success) {
            options.success({
              data: TestDataFactory.createSuccessResponse({ userId: 'user_001' }),
              statusCode: 200,
              header: {}
            });
          }
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
      expect(result.data.userId).toBeDefined();
    });
  });

  describe('Token refresh flow', () => {
    it('should refresh expired token', async () => {
      const newTokens = {
        token: 'new_token',
        refreshToken: 'new_refresh_token'
      };
      
      wxMock.getMockFunction('request').mockImplementation((options: any) => {
        if (options.url.includes('/auth/refresh')) {
          if (options.success) {
            options.success({
              data: TestDataFactory.createSuccessResponse(newTokens),
              statusCode: 200,
              header: {}
            });
          }
        }
      });

      const result = await authApi.refreshToken('old_refresh_token');

      expect(result.code).toBe(200);
      expect(result.data.token).toBe('new_token');
    });
  });

  describe('Logout flow', () => {
    it('should logout and clear tokens', async () => {
      wxMock.setStorageSync('access_token', 'token123');
      wxMock.setStorageSync('refresh_token', 'refresh123');
      
      wxMock.getMockFunction('request').mockImplementation((options: any) => {
        if (options.url.includes('/auth/logout')) {
          if (options.success) {
            options.success({
              data: TestDataFactory.createSuccessResponse(null),
              statusCode: 200,
              header: {}
            });
          }
        }
      });

      const result = await authApi.logout();

      expect(result.code).toBe(200);
      expect(wxMock.getStorageSync('access_token')).toBeUndefined();
    });
  });
});
