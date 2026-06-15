jest.mock('../../../miniprogram/utils/request', () => ({
  __esModule: true,
  default: {
    get: jest.fn(),
    post: jest.fn(),
    put: jest.fn(),
    delete: jest.fn()
  }
}));

jest.mock('../../../miniprogram/utils/mock', () => ({
  __esModule: true,
  default: {
    isEnabled: jest.fn(),
    request: jest.fn()
  }
}));

jest.mock('../../../miniprogram/utils/store', () => ({
  __esModule: true,
  default: {
    getUser: jest.fn(),
    setUser: jest.fn()
  }
}));

import { authApi } from '../../../miniprogram/services/auth';
import request from '../../../miniprogram/utils/request';
import mockManager from '../../../miniprogram/utils/mock';
import store from '../../../miniprogram/utils/store';

const mockedRequest = request as jest.Mocked<typeof request>;
const mockedMock = mockManager as jest.Mocked<typeof mockManager>;
const mockedStore = store as jest.Mocked<typeof store>;

const mockUser = {
  id: 1,
  openId: 'oTest123',
  nickname: '测试用户',
  avatarUrl: 'https://example.com/avatar.png',
  status: 'normal' as const,
  createdAt: '2026-01-01T00:00:00.000Z',
  updatedAt: '2026-01-01T00:00:00.000Z'
};

beforeEach(() => {
  jest.clearAllMocks();
});

describe('authApi.wxLogin', () => {
  const params = { code: 'wx_code_123' };
  const wxLoginResponse = {
    code: 200,
    message: 'success',
    data: {
      isNewUser: true,
      user: mockUser
    },
    timestamp: Date.now()
  };

  it('mock 启用时调用 mockManager.request 并在成功时存储用户', async () => {
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(true);
    (mockedMock.request as jest.Mock).mockResolvedValue(wxLoginResponse);

    const result = await authApi.wxLogin(params);

    expect(mockedMock.isEnabled).toHaveBeenCalled();
    expect(mockedMock.request).toHaveBeenCalledWith({
      url: '/user/login',
      method: 'POST',
      data: params
    });
    expect(mockedStore.setUser).toHaveBeenCalledWith(mockUser);
    expect(result).toEqual(wxLoginResponse);
  });

  it('mock 未启用时调用 request.post 并在成功时存储用户', async () => {
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(false);
    (mockedRequest.post as jest.Mock).mockResolvedValue(wxLoginResponse);

    const result = await authApi.wxLogin(params);

    expect(mockedRequest.post).toHaveBeenCalledWith('/user/login', params);
    expect(mockedStore.setUser).toHaveBeenCalledWith(mockUser);
    expect(result).toEqual(wxLoginResponse);
  });

  it('mock 启用时响应 code 非 200 不存储用户', async () => {
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(true);
    (mockedMock.request as jest.Mock).mockResolvedValue({
      code: 401,
      message: 'unauthorized',
      data: null,
      timestamp: Date.now()
    });

    await authApi.wxLogin(params);

    expect(mockedStore.setUser).not.toHaveBeenCalled();
  });

  it('mock 未启用时响应 code 非 200 不存储用户', async () => {
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(false);
    (mockedRequest.post as jest.Mock).mockResolvedValue({
      code: 401,
      message: 'unauthorized',
      data: null,
      timestamp: Date.now()
    });

    await authApi.wxLogin(params);

    expect(mockedStore.setUser).not.toHaveBeenCalled();
  });

  it('响应 data 中无 user 时不存储用户', async () => {
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(true);
    (mockedMock.request as jest.Mock).mockResolvedValue({
      code: 200,
      message: 'success',
      data: { isNewUser: true },
      timestamp: Date.now()
    });

    await authApi.wxLogin(params);

    expect(mockedStore.setUser).not.toHaveBeenCalled();
  });
});

describe('authApi.loginWithWx', () => {
  const wxLoginResponse = {
    code: 200,
    message: 'success',
    data: {
      isNewUser: false,
      user: mockUser
    },
    timestamp: Date.now()
  };

  it('wx.login 成功时调用 wxLogin 并返回结果', async () => {
    (wx.login as jest.Mock).mockImplementation(({ success }: any) => {
      success({ code: 'wx_code_456' });
    });
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(true);
    (mockedMock.request as jest.Mock).mockResolvedValue(wxLoginResponse);

    const result = await authApi.loginWithWx();

    expect(wx.login).toHaveBeenCalled();
    expect(mockedMock.request).toHaveBeenCalledWith({
      url: '/user/login',
      method: 'POST',
      data: { code: 'wx_code_456' }
    });
    expect(mockedStore.setUser).toHaveBeenCalledWith(mockUser);
    expect(result).toEqual(wxLoginResponse);
  });

  it('wx.login 成功但未返回 code 时返回错误响应', async () => {
    (wx.login as jest.Mock).mockImplementation(({ success }: any) => {
      success({ code: '' });
    });

    const result = await authApi.loginWithWx();

    expect(result.code).toBe(10001);
    expect(result.message).toBe('微信登录失败');
    expect(mockedStore.setUser).not.toHaveBeenCalled();
  });

  it('wx.login 失败时返回错误响应', async () => {
    (wx.login as jest.Mock).mockImplementation(({ fail }: any) => {
      fail({ errMsg: 'login:fail' });
    });

    const result = await authApi.loginWithWx();

    expect(result.code).toBe(10001);
    expect(result.message).toBe('微信登录失败');
    expect(mockedStore.setUser).not.toHaveBeenCalled();
  });
});
