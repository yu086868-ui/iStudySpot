jest.mock('../../../miniprogram/utils/request', () => ({
  __esModule: true,
  default: {
    get: jest.fn(),
    post: jest.fn(),
    put: jest.fn(),
    delete: jest.fn(),
    saveTokens: jest.fn(),
    clearTokens: jest.fn()
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
    setUser: jest.fn(),
    clearUser: jest.fn(),
    getMyReservations: jest.fn().mockReturnValue([]),
    setMyReservations: jest.fn(),
    addReservation: jest.fn(),
    updateReservation: jest.fn(),
    removeReservation: jest.fn(),
    getCurrentCheckIn: jest.fn().mockReturnValue({ isCheckedIn: false, checkInRecord: null }),
    setCurrentCheckIn: jest.fn(),
    getCheckInRecords: jest.fn().mockReturnValue([]),
    setCheckInRecords: jest.fn(),
    getStudyRooms: jest.fn().mockReturnValue([]),
    setStudyRooms: jest.fn(),
    getStudyRoomDetail: jest.fn().mockReturnValue(null),
    setStudyRoomDetail: jest.fn(),
    getSeats: jest.fn().mockReturnValue(null),
    setSeats: jest.fn(),
    getAnnouncements: jest.fn().mockReturnValue([]),
    setAnnouncements: jest.fn(),
    getRules: jest.fn().mockReturnValue([]),
    setRules: jest.fn(),
    getReservationRules: jest.fn().mockReturnValue(null),
    setReservationRules: jest.fn(),
    getCards: jest.fn().mockReturnValue([]),
    setCards: jest.fn(),
    addCard: jest.fn(),
    getCardById: jest.fn().mockReturnValue(null)
  }
}));

import { authApi } from '../../../miniprogram/services/auth';
import request from '../../../miniprogram/utils/request';
import mockManager from '../../../miniprogram/utils/mock';
import store from '../../../miniprogram/utils/store';

const mockedRequest = request as jest.Mocked<typeof request>;
const mockedMock = mockManager as jest.Mocked<typeof mockManager>;
const mockedStore = store as jest.Mocked<typeof store>;

beforeEach(() => {
  jest.clearAllMocks();
});

describe('authApi.login', () => {
  const params = { username: 'testuser', password: '123456' };
  const loginResponse = {
    code: 200,
    message: 'success',
    data: {
      token: 'token123',
      refreshToken: 'refresh123',
      user: { id: 'u1', username: 'testuser', nickname: 'Test', avatar: '' }
    },
    timestamp: Date.now()
  };

  it('calls mockManager.request when mock is enabled and stores user + tokens on success', async () => {
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(true);
    (mockedMock.request as jest.Mock).mockResolvedValue(loginResponse);

    const result = await authApi.login(params);

    expect(mockedMock.isEnabled).toHaveBeenCalled();
    expect(mockedMock.request).toHaveBeenCalledWith({
      url: '/auth/login',
      method: 'POST',
      data: params
    });
    expect(mockedRequest.saveTokens).toHaveBeenCalledWith('token123', 'refresh123');
    expect(mockedStore.setUser).toHaveBeenCalled();
    expect(result).toEqual(loginResponse);
  });

  it('calls request.post when mock is disabled and stores user + tokens on success', async () => {
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(false);
    (mockedRequest.post as jest.Mock).mockResolvedValue(loginResponse);

    const result = await authApi.login(params);

    expect(mockedRequest.post).toHaveBeenCalledWith('/auth/login', params, false);
    expect(mockedRequest.saveTokens).toHaveBeenCalledWith('token123', 'refresh123');
    expect(mockedStore.setUser).toHaveBeenCalled();
    expect(result).toEqual(loginResponse);
  });

  it('does not store user or tokens when mock response code is not 200', async () => {
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(true);
    (mockedMock.request as jest.Mock).mockResolvedValue({
      code: 401,
      message: 'unauthorized',
      data: null,
      timestamp: Date.now()
    });

    await authApi.login(params);

    expect(mockedRequest.saveTokens).not.toHaveBeenCalled();
    expect(mockedStore.setUser).not.toHaveBeenCalled();
  });

  it('does not store user or tokens when real response code is not 200', async () => {
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(false);
    (mockedRequest.post as jest.Mock).mockResolvedValue({
      code: 401,
      message: 'unauthorized',
      data: null,
      timestamp: Date.now()
    });

    await authApi.login(params);

    expect(mockedRequest.saveTokens).not.toHaveBeenCalled();
    expect(mockedStore.setUser).not.toHaveBeenCalled();
  });
});

describe('authApi.register', () => {
  const params = { username: 'newuser', password: '123456', nickname: 'New', phone: '13800000000', studentId: 'S001' };
  const registerResponse = {
    code: 200,
    message: 'success',
    data: { userId: 'u2' },
    timestamp: Date.now()
  };

  it('calls mockManager.request when mock is enabled', async () => {
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(true);
    (mockedMock.request as jest.Mock).mockResolvedValue(registerResponse);

    const result = await authApi.register(params);

    expect(mockedMock.request).toHaveBeenCalledWith({
      url: '/auth/register',
      method: 'POST',
      data: params
    });
    expect(result).toEqual(registerResponse);
  });

  it('calls request.post when mock is disabled', async () => {
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(false);
    (mockedRequest.post as jest.Mock).mockResolvedValue(registerResponse);

    const result = await authApi.register(params);

    expect(mockedRequest.post).toHaveBeenCalledWith('/auth/register', params, false);
    expect(result).toEqual(registerResponse);
  });
});

describe('authApi.refreshToken', () => {
  const refreshResponse = {
    code: 200,
    message: 'success',
    data: { token: 'newToken', refreshToken: 'newRefresh' },
    timestamp: Date.now()
  };

  it('calls mockManager.request when mock is enabled and saves tokens on success', async () => {
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(true);
    (mockedMock.request as jest.Mock).mockResolvedValue(refreshResponse);

    const result = await authApi.refreshToken('oldRefresh');

    expect(mockedMock.request).toHaveBeenCalledWith({
      url: '/auth/refresh',
      method: 'POST',
      data: { refreshToken: 'oldRefresh' }
    });
    expect(mockedRequest.saveTokens).toHaveBeenCalledWith('newToken', 'newRefresh');
    expect(result).toEqual(refreshResponse);
  });

  it('calls request.post when mock is disabled and saves tokens on success', async () => {
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(false);
    (mockedRequest.post as jest.Mock).mockResolvedValue(refreshResponse);

    const result = await authApi.refreshToken('oldRefresh');

    expect(mockedRequest.post).toHaveBeenCalledWith('/auth/refresh', { refreshToken: 'oldRefresh' }, false);
    expect(mockedRequest.saveTokens).toHaveBeenCalledWith('newToken', 'newRefresh');
    expect(result).toEqual(refreshResponse);
  });

  it('does not save tokens when mock response code is not 200', async () => {
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(true);
    (mockedMock.request as jest.Mock).mockResolvedValue({
      code: 401,
      message: 'unauthorized',
      data: null,
      timestamp: Date.now()
    });

    await authApi.refreshToken('oldRefresh');

    expect(mockedRequest.saveTokens).not.toHaveBeenCalled();
  });
});

describe('authApi.logout', () => {
  const logoutResponse = {
    code: 200,
    message: 'success',
    data: null,
    timestamp: Date.now()
  };

  it('calls mockManager.request when mock is enabled and clears tokens + user', async () => {
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(true);
    (mockedMock.request as jest.Mock).mockResolvedValue(logoutResponse);

    const result = await authApi.logout();

    expect(mockedMock.request).toHaveBeenCalledWith({
      url: '/auth/logout',
      method: 'POST'
    });
    expect(mockedRequest.clearTokens).toHaveBeenCalled();
    expect(mockedStore.clearUser).toHaveBeenCalled();
    expect(result).toEqual(logoutResponse);
  });

  it('calls request.post when mock is disabled and clears tokens + user', async () => {
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(false);
    (mockedRequest.post as jest.Mock).mockResolvedValue(logoutResponse);

    const result = await authApi.logout();

    expect(mockedRequest.post).toHaveBeenCalledWith('/auth/logout');
    expect(mockedRequest.clearTokens).toHaveBeenCalled();
    expect(mockedStore.clearUser).toHaveBeenCalled();
    expect(result).toEqual(logoutResponse);
  });
});
