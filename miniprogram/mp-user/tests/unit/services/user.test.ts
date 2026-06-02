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
    id: 'u1',
    username: 'testuser',
    nickname: 'Test',
    avatar: '',
    phone: '13800000000',
    email: 'test@test.com',
    studentId: 'S001',
    creditScore: 100,
    status: 'active' as const,
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
      url: '/users/me',
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

    expect(mockedRequest.get).toHaveBeenCalledWith('/users/me');
    expect(mockedStore.setUser).toHaveBeenCalledWith(mockUser);
    expect(result).toEqual(apiResponse);
  });

  it('bypasses cache when forceRefresh is true', async () => {
    (mockedStore.getUser as jest.Mock).mockReturnValue(mockUser);
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(false);
    (mockedRequest.get as jest.Mock).mockResolvedValue(apiResponse);

    await userApi.getCurrentUser(true);

    expect(mockedRequest.get).toHaveBeenCalledWith('/users/me');
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

describe('userApi.updateUser', () => {
  const params = { nickname: 'NewNick', avatar: 'new.png' };
  const updatedUser = {
    id: 'u1',
    username: 'testuser',
    nickname: 'NewNick',
    avatar: 'new.png',
    phone: '13800000000',
    email: 'test@test.com',
    studentId: 'S001',
    creditScore: 100,
    status: 'active' as const,
    createdAt: '2024-01-01T00:00:00.000Z',
    updatedAt: '2024-01-02T00:00:00.000Z'
  };

  const apiResponse = {
    code: 200,
    message: 'success',
    data: updatedUser,
    timestamp: Date.now()
  };

  it('calls mockManager.request when mock is enabled and stores user on success', async () => {
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(true);
    (mockedMock.request as jest.Mock).mockResolvedValue(apiResponse);

    const result = await userApi.updateUser(params);

    expect(mockedMock.request).toHaveBeenCalledWith({
      url: '/users/me',
      method: 'PUT',
      data: params
    });
    expect(mockedStore.setUser).toHaveBeenCalledWith(updatedUser);
    expect(result).toEqual(apiResponse);
  });

  it('calls request.put when mock is disabled and stores user on success', async () => {
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(false);
    (mockedRequest.put as jest.Mock).mockResolvedValue(apiResponse);

    const result = await userApi.updateUser(params);

    expect(mockedRequest.put).toHaveBeenCalledWith('/users/me', params);
    expect(mockedStore.setUser).toHaveBeenCalledWith(updatedUser);
    expect(result).toEqual(apiResponse);
  });

  it('does not store user when response code is not 200', async () => {
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(false);
    (mockedRequest.put as jest.Mock).mockResolvedValue({
      code: 400,
      message: 'bad request',
      data: null,
      timestamp: Date.now()
    });

    await userApi.updateUser(params);

    expect(mockedStore.setUser).not.toHaveBeenCalled();
  });
});

describe('userApi.changePassword', () => {
  const params = { oldPassword: 'old123', newPassword: 'new456' };
  const apiResponse = {
    code: 200,
    message: 'success',
    data: null,
    timestamp: Date.now()
  };

  it('calls mockManager.request when mock is enabled', async () => {
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(true);
    (mockedMock.request as jest.Mock).mockResolvedValue(apiResponse);

    const result = await userApi.changePassword(params);

    expect(mockedMock.request).toHaveBeenCalledWith({
      url: '/users/me/password',
      method: 'PUT',
      data: params
    });
    expect(result).toEqual(apiResponse);
  });

  it('calls request.put when mock is disabled', async () => {
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(false);
    (mockedRequest.put as jest.Mock).mockResolvedValue(apiResponse);

    const result = await userApi.changePassword(params);

    expect(mockedRequest.put).toHaveBeenCalledWith('/users/me/password', params);
    expect(result).toEqual(apiResponse);
  });
});
