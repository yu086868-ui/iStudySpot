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

import { ruleApi } from '../../../miniprogram/services/rule';
import request from '../../../miniprogram/utils/request';
import mockManager from '../../../miniprogram/utils/mock';
import store from '../../../miniprogram/utils/store';

const mockedRequest = request as jest.Mocked<typeof request>;
const mockedMock = mockManager as jest.Mocked<typeof mockManager>;
const mockedStore = store as jest.Mocked<typeof store>;

beforeEach(() => {
  jest.clearAllMocks();
});

describe('ruleApi.getRules', () => {
  const mockRules = [
    {
      id: 'rule1',
      studyRoomId: null,
      category: 'general' as const,
      title: 'General Rule',
      content: 'Be quiet',
      priority: 1,
      createdAt: '2024-01-01T00:00:00Z',
      updatedAt: '2024-01-01T00:00:00Z'
    }
  ];

  it('returns cached rules when available and forceRefresh is false and no params', async () => {
    (mockedStore.getRules as jest.Mock).mockReturnValue(mockRules);

    const result = await ruleApi.getRules();

    expect(result.code).toBe(200);
    expect(result.data).toEqual(mockRules);
    expect(mockedMock.request).not.toHaveBeenCalled();
    expect(mockedRequest.get).not.toHaveBeenCalled();
  });

  it('fetches from mock when cache is empty and mock is enabled', async () => {
    (mockedStore.getRules as jest.Mock).mockReturnValue([]);
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(true);
    const apiResponse = { code: 200, message: 'success', data: mockRules, timestamp: Date.now() };
    (mockedMock.request as jest.Mock).mockResolvedValue(apiResponse);

    const result = await ruleApi.getRules();

    expect(mockedMock.request).toHaveBeenCalledWith({
      url: '/rules',
      method: 'GET',
      data: undefined
    });
    expect(mockedStore.setRules).toHaveBeenCalledWith(mockRules);
    expect(result).toEqual(apiResponse);
  });

  it('fetches from real API when cache is empty and mock is disabled', async () => {
    (mockedStore.getRules as jest.Mock).mockReturnValue([]);
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(false);
    const apiResponse = { code: 200, message: 'success', data: mockRules, timestamp: Date.now() };
    (mockedRequest.get as jest.Mock).mockResolvedValue(apiResponse);

    const result = await ruleApi.getRules();

    expect(mockedRequest.get).toHaveBeenCalledWith('/rules', undefined);
    expect(mockedStore.setRules).toHaveBeenCalledWith(mockRules);
    expect(result).toEqual(apiResponse);
  });

  it('bypasses cache when params are provided', async () => {
    (mockedStore.getRules as jest.Mock).mockReturnValue(mockRules);
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(false);
    const apiResponse = { code: 200, message: 'success', data: mockRules, timestamp: Date.now() };
    (mockedRequest.get as jest.Mock).mockResolvedValue(apiResponse);

    await ruleApi.getRules({ category: 'general' });

    expect(mockedRequest.get).toHaveBeenCalled();
  });

  it('bypasses cache when forceRefresh is true', async () => {
    (mockedStore.getRules as jest.Mock).mockReturnValue(mockRules);
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(false);
    const apiResponse = { code: 200, message: 'success', data: mockRules, timestamp: Date.now() };
    (mockedRequest.get as jest.Mock).mockResolvedValue(apiResponse);

    await ruleApi.getRules(undefined, true);

    expect(mockedRequest.get).toHaveBeenCalled();
  });
});

describe('ruleApi.getRuleDetail', () => {
  const mockRule = {
    id: 'rule1',
    studyRoomId: null,
    category: 'general' as const,
    title: 'General Rule',
    content: 'Be quiet',
    priority: 1,
    createdAt: '2024-01-01T00:00:00Z',
    updatedAt: '2024-01-01T00:00:00Z'
  };
  const apiResponse = { code: 200, message: 'success', data: mockRule, timestamp: Date.now() };

  it('calls mockManager.request when mock is enabled', async () => {
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(true);
    (mockedMock.request as jest.Mock).mockResolvedValue(apiResponse);

    const result = await ruleApi.getRuleDetail('rule1');

    expect(mockedMock.request).toHaveBeenCalledWith({
      url: '/rules/rule1',
      method: 'GET'
    });
    expect(result).toEqual(apiResponse);
  });

  it('calls request.get when mock is disabled', async () => {
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(false);
    (mockedRequest.get as jest.Mock).mockResolvedValue(apiResponse);

    const result = await ruleApi.getRuleDetail('rule1');

    expect(mockedRequest.get).toHaveBeenCalledWith('/rules/rule1');
    expect(result).toEqual(apiResponse);
  });
});
