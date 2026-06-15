jest.mock('../../../miniprogram/utils/request', () => ({
  __esModule: true,
  default: {
    get: jest.fn(),
    post: jest.fn(),
    put: jest.fn(),
    delete: jest.fn(),
    getBaseURL: jest.fn().mockReturnValue('https://api.example.com')
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

import { userApi } from '../../../miniprogram/services/user';
import request from '../../../miniprogram/utils/request';
import mockManager from '../../../miniprogram/utils/mock';
import store from '../../../miniprogram/utils/store';

const mockedRequest = request as jest.Mocked<typeof request>;
const mockedMock = mockManager as jest.Mocked<typeof mockManager>;
const mockedStore = store as jest.Mocked<typeof store>;

beforeEach(() => {
  jest.clearAllMocks();
});

describe('userApi.getCurrentUser', () => {
  const mockUser = {
    id: 1,
    openId: 'o123',
    nickname: 'Test',
    avatarUrl: '',
    status: 'normal' as const,
    createdAt: '2024-01-01T00:00:00.000Z',
    updatedAt: '2024-01-01T00:00:00.000Z'
  };

  const apiResponse = {
    code: 200,
    message: 'success',
    data: mockUser,
    timestamp: Date.now()
  };

  it('returns cached user when available and forceRefresh is false', async () => {
    (mockedStore.getUser as jest.Mock).mockReturnValue(mockUser);

    const result = await userApi.getCurrentUser();

    expect(mockedStore.getUser).toHaveBeenCalled();
    expect(result.code).toBe(200);
    expect(result.data).toEqual(mockUser);
    expect(mockedMock.request).not.toHaveBeenCalled();
    expect(mockedRequest.get).not.toHaveBeenCalled();
  });

  it('fetches from mock when cache is empty and mock is enabled', async () => {
    (mockedStore.getUser as jest.Mock).mockReturnValue(null);
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(true);
    (mockedMock.request as jest.Mock).mockResolvedValue(apiResponse);

    const result = await userApi.getCurrentUser();

    expect(mockedMock.request).toHaveBeenCalledWith({
      url: '/user/profile',
      method: 'GET'
    });
    expect(mockedStore.setUser).toHaveBeenCalledWith(mockUser);
    expect(result).toEqual(apiResponse);
  });

  it('fetches from real API when cache is empty and mock is disabled', async () => {
    (mockedStore.getUser as jest.Mock).mockReturnValue(null);
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(false);
    (mockedRequest.get as jest.Mock).mockResolvedValue(apiResponse);

    const result = await userApi.getCurrentUser();

    expect(mockedRequest.get).toHaveBeenCalledWith('/user/profile');
    expect(mockedStore.setUser).toHaveBeenCalledWith(mockUser);
    expect(result).toEqual(apiResponse);
  });

  it('bypasses cache when forceRefresh is true', async () => {
    (mockedStore.getUser as jest.Mock).mockReturnValue(mockUser);
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(false);
    (mockedRequest.get as jest.Mock).mockResolvedValue(apiResponse);

    await userApi.getCurrentUser(true);

    expect(mockedRequest.get).toHaveBeenCalledWith('/user/profile');
  });

  it('does not store user when response code is not 200', async () => {
    (mockedStore.getUser as jest.Mock).mockReturnValue(null);
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(false);
    (mockedRequest.get as jest.Mock).mockResolvedValue({
      code: 401,
      message: 'unauthorized',
      data: null,
      timestamp: Date.now()
    });

    await userApi.getCurrentUser();

    expect(mockedStore.setUser).not.toHaveBeenCalled();
  });
});

describe('userApi.updateProfile', () => {
  const params = { nickname: 'NewNick' };
  const existingUser = {
    id: 1,
    openId: 'o123',
    nickname: 'OldNick',
    avatarUrl: '',
    status: 'normal' as const,
    createdAt: '2024-01-01T00:00:00.000Z',
    updatedAt: '2024-01-01T00:00:00.000Z'
  };

  const apiResponse = {
    code: 200,
    message: 'success',
    data: null,
    timestamp: Date.now()
  };

  it('calls mockManager.request when mock is enabled and updates user on success', async () => {
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(true);
    (mockedMock.request as jest.Mock).mockResolvedValue(apiResponse);
    (mockedStore.getUser as jest.Mock).mockReturnValue(existingUser);

    const result = await userApi.updateProfile(params);

    expect(mockedMock.request).toHaveBeenCalledWith({
      url: '/user/profile',
      method: 'PUT',
      data: params
    });
    expect(mockedStore.setUser).toHaveBeenCalledWith({ ...existingUser, nickname: 'NewNick' });
    expect(result).toEqual(apiResponse);
  });

  it('calls request.put when mock is disabled and updates user on success', async () => {
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(false);
    (mockedRequest.put as jest.Mock).mockResolvedValue(apiResponse);
    (mockedStore.getUser as jest.Mock).mockReturnValue(existingUser);

    const result = await userApi.updateProfile(params);

    expect(mockedRequest.put).toHaveBeenCalledWith('/user/profile', params);
    expect(mockedStore.setUser).toHaveBeenCalledWith({ ...existingUser, nickname: 'NewNick' });
    expect(result).toEqual(apiResponse);
  });

  it('does not update user when response code is not 200', async () => {
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(false);
    (mockedRequest.put as jest.Mock).mockResolvedValue({
      code: 400,
      message: 'bad request',
      data: null,
      timestamp: Date.now()
    });

    await userApi.updateProfile(params);

    expect(mockedStore.setUser).not.toHaveBeenCalled();
  });

  it('does not update user when nickname is not provided in params', async () => {
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(false);
    (mockedRequest.put as jest.Mock).mockResolvedValue(apiResponse);
    (mockedStore.getUser as jest.Mock).mockReturnValue(existingUser);

    await userApi.updateProfile({});

    expect(mockedStore.setUser).not.toHaveBeenCalled();
  });
});

describe('userApi.uploadAvatar', () => {
  const existingUser = {
    id: 1,
    openId: 'o123',
    nickname: 'Test',
    avatarUrl: '',
    status: 'normal' as const,
    createdAt: '2024-01-01T00:00:00.000Z',
    updatedAt: '2024-01-01T00:00:00.000Z'
  };

  it('returns mock avatar URL when mock is enabled and updates user', async () => {
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(true);
    (mockedStore.getUser as jest.Mock).mockReturnValue(existingUser);

    const result = await userApi.uploadAvatar('/tmp/avatar.jpg');

    expect(result.code).toBe(200);
    expect(result.data).toEqual({ avatarUrl: '/tmp/avatar.jpg' });
    expect(mockedStore.setUser).toHaveBeenCalledWith({
      ...existingUser,
      avatarUrl: '/tmp/avatar.jpg'
    });
  });

  it('calls wx.uploadFile when mock is disabled and updates user on success', async () => {
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(false);
    (mockedRequest.getBaseURL as jest.Mock).mockReturnValue('https://api.example.com');
    (mockedStore.getUser as jest.Mock).mockReturnValue(existingUser);

    const uploadResponse = {
        code: 200,
        message: '上传成功',
        data: { avatarUrl: '/avatar/new.jpg' },
        timestamp: Date.now()
      };
    (wx.uploadFile as jest.Mock).mockImplementation(({ success }: any) => {
      success({ data: JSON.stringify(uploadResponse) });
    });

    const result = await userApi.uploadAvatar('/tmp/avatar.jpg');

    expect(wx.uploadFile).toHaveBeenCalledWith(
      expect.objectContaining({
        url: 'https://api.example.com/user/avatar',
        filePath: '/tmp/avatar.jpg',
        name: 'file'
      })
    );
    expect(result.code).toBe(200);
    expect(result.data).toEqual({ avatarUrl: '/avatar/new.jpg' });
    expect(mockedStore.setUser).toHaveBeenCalledWith({
      ...existingUser,
      avatarUrl: '/avatar/new.jpg'
    });
  });

  it('returns error when wx.uploadFile fails', async () => {
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(false);
    (mockedRequest.getBaseURL as jest.Mock).mockReturnValue('https://api.example.com');
    (wx.uploadFile as jest.Mock).mockImplementation(({ fail }: any) => {
      fail({ errMsg: 'upload failed' });
    });

    const result = await userApi.uploadAvatar('/tmp/avatar.jpg');

    expect(result.code).toBe(500);
    expect(result.message).toBe('上传失败');
  });

  it('returns error when wx.uploadFile response cannot be parsed', async () => {
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(false);
    (mockedRequest.getBaseURL as jest.Mock).mockReturnValue('https://api.example.com');
    (wx.uploadFile as jest.Mock).mockImplementation(({ success }: any) => {
      success({ data: 'invalid json' });
    });

    const result = await userApi.uploadAvatar('/tmp/avatar.jpg');

    expect(result.code).toBe(500);
    expect(result.message).toBe('上传失败');
  });
});

describe('userApi.getUserHome', () => {
  const homeData = {
    user: { id: 1, nickname: 'Test', avatarUrl: '' },
    reservationCount: 5,
    studyHours: 12,
    creditScore: 100
  };

  const apiResponse = {
    code: 200,
    message: 'success',
    data: homeData,
    timestamp: Date.now()
  };

  it('calls mockManager.request when mock is enabled', async () => {
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(true);
    (mockedMock.request as jest.Mock).mockResolvedValue(apiResponse);

    const result = await userApi.getUserHome();

    expect(mockedMock.request).toHaveBeenCalledWith({
      url: '/user/home',
      method: 'GET'
    });
    expect(result).toEqual(apiResponse);
  });

  it('calls request.get when mock is disabled', async () => {
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(false);
    (mockedRequest.get as jest.Mock).mockResolvedValue(apiResponse);

    const result = await userApi.getUserHome();

    expect(mockedRequest.get).toHaveBeenCalledWith('/user/home');
    expect(result).toEqual(apiResponse);
  });
});
