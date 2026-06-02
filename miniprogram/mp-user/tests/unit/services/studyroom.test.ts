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

jest.mock('../../../miniprogram/utils/cache', () => ({
  __esModule: true,
  default: {}
}));

import { studyRoomApi } from '../../../miniprogram/services/studyroom';
import request from '../../../miniprogram/utils/request';
import mockManager from '../../../miniprogram/utils/mock';
import store from '../../../miniprogram/utils/store';

const mockedRequest = request as jest.Mocked<typeof request>;
const mockedMock = mockManager as jest.Mocked<typeof mockManager>;
const mockedStore = store as jest.Mocked<typeof store>;

beforeEach(() => {
  jest.clearAllMocks();
});

describe('studyRoomApi.getStudyRooms', () => {
  const mockRooms = [
    {
      id: 'room1',
      name: 'Room A',
      description: 'Desc',
      location: 'Floor 1',
      floor: 1,
      capacity: 50,
      openTime: '08:00',
      closeTime: '22:00',
      facilities: ['wifi'],
      image: '',
      status: 'open' as const
    }
  ];

  it('returns cached rooms when available and forceRefresh is false and no params', async () => {
    (mockedStore.getStudyRooms as jest.Mock).mockReturnValue(mockRooms);

    const result = await studyRoomApi.getStudyRooms();

    expect(result.code).toBe(200);
    expect(result.data.list).toEqual(mockRooms);
    expect(result.data.total).toBe(1);
    expect(mockedMock.request).not.toHaveBeenCalled();
    expect(mockedRequest.get).not.toHaveBeenCalled();
  });

  it('fetches from mock when cache is empty and mock is enabled', async () => {
    (mockedStore.getStudyRooms as jest.Mock).mockReturnValue([]);
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(true);
    const apiResponse = {
      code: 200,
      message: 'success',
      data: { list: mockRooms, total: 1, page: 1, pageSize: 1 },
      timestamp: Date.now()
    };
    (mockedMock.request as jest.Mock).mockResolvedValue(apiResponse);

    const result = await studyRoomApi.getStudyRooms();

    expect(mockedMock.request).toHaveBeenCalledWith({
      url: '/studyrooms',
      method: 'GET',
      data: undefined
    });
    expect(mockedStore.setStudyRooms).toHaveBeenCalledWith(mockRooms);
    expect(result).toEqual(apiResponse);
  });

  it('fetches from real API when cache is empty and mock is disabled', async () => {
    (mockedStore.getStudyRooms as jest.Mock).mockReturnValue([]);
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(false);
    const apiResponse = {
      code: 200,
      message: 'success',
      data: { list: mockRooms, total: 1, page: 1, pageSize: 1 },
      timestamp: Date.now()
    };
    (mockedRequest.get as jest.Mock).mockResolvedValue(apiResponse);

    const result = await studyRoomApi.getStudyRooms();

    expect(mockedRequest.get).toHaveBeenCalledWith('/studyrooms', undefined);
    expect(mockedStore.setStudyRooms).toHaveBeenCalledWith(mockRooms);
    expect(result).toEqual(apiResponse);
  });

  it('bypasses cache when params are provided', async () => {
    (mockedStore.getStudyRooms as jest.Mock).mockReturnValue(mockRooms);
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(false);
    const apiResponse = {
      code: 200,
      message: 'success',
      data: { list: mockRooms, total: 1, page: 1, pageSize: 1 },
      timestamp: Date.now()
    };
    (mockedRequest.get as jest.Mock).mockResolvedValue(apiResponse);

    await studyRoomApi.getStudyRooms({ status: 'open' });

    expect(mockedRequest.get).toHaveBeenCalled();
  });

  it('bypasses cache when forceRefresh is true', async () => {
    (mockedStore.getStudyRooms as jest.Mock).mockReturnValue(mockRooms);
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(false);
    const apiResponse = {
      code: 200,
      message: 'success',
      data: { list: mockRooms, total: 1, page: 1, pageSize: 1 },
      timestamp: Date.now()
    };
    (mockedRequest.get as jest.Mock).mockResolvedValue(apiResponse);

    await studyRoomApi.getStudyRooms(undefined, true);

    expect(mockedRequest.get).toHaveBeenCalled();
  });
});

describe('studyRoomApi.getStudyRoomDetail', () => {
  const mockDetail = {
    id: 'room1',
    name: 'Room A',
    description: 'Desc',
    location: 'Floor 1',
    floor: 1,
    capacity: 50,
    openTime: '08:00',
    closeTime: '22:00',
    facilities: ['wifi'],
    image: '',
    status: 'open' as const,
    rules: [],
    createdAt: '2024-01-01T00:00:00Z',
    updatedAt: '2024-01-01T00:00:00Z'
  };

  it('returns cached detail when available and forceRefresh is false', async () => {
    (mockedStore.getStudyRoomDetail as jest.Mock).mockReturnValue(mockDetail);

    const result = await studyRoomApi.getStudyRoomDetail('room1');

    expect(mockedStore.getStudyRoomDetail).toHaveBeenCalledWith('room1');
    expect(result.code).toBe(200);
    expect(result.data).toEqual(mockDetail);
    expect(mockedMock.request).not.toHaveBeenCalled();
    expect(mockedRequest.get).not.toHaveBeenCalled();
  });

  it('fetches from mock when cache is empty and mock is enabled', async () => {
    (mockedStore.getStudyRoomDetail as jest.Mock).mockReturnValue(null);
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(true);
    const apiResponse = { code: 200, message: 'success', data: mockDetail, timestamp: Date.now() };
    (mockedMock.request as jest.Mock).mockResolvedValue(apiResponse);

    const result = await studyRoomApi.getStudyRoomDetail('room1');

    expect(mockedMock.request).toHaveBeenCalledWith({
      url: '/studyrooms/room1',
      method: 'GET'
    });
    expect(mockedStore.setStudyRoomDetail).toHaveBeenCalledWith(mockDetail);
    expect(result).toEqual(apiResponse);
  });

  it('fetches from real API when cache is empty and mock is disabled', async () => {
    (mockedStore.getStudyRoomDetail as jest.Mock).mockReturnValue(null);
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(false);
    const apiResponse = { code: 200, message: 'success', data: mockDetail, timestamp: Date.now() };
    (mockedRequest.get as jest.Mock).mockResolvedValue(apiResponse);

    const result = await studyRoomApi.getStudyRoomDetail('room1');

    expect(mockedRequest.get).toHaveBeenCalledWith('/studyrooms/room1');
    expect(mockedStore.setStudyRoomDetail).toHaveBeenCalledWith(mockDetail);
    expect(result).toEqual(apiResponse);
  });

  it('bypasses cache when forceRefresh is true', async () => {
    (mockedStore.getStudyRoomDetail as jest.Mock).mockReturnValue(mockDetail);
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(false);
    const apiResponse = { code: 200, message: 'success', data: mockDetail, timestamp: Date.now() };
    (mockedRequest.get as jest.Mock).mockResolvedValue(apiResponse);

    await studyRoomApi.getStudyRoomDetail('room1', true);

    expect(mockedRequest.get).toHaveBeenCalled();
  });
});
