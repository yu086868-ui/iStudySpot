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

import { announcementApi } from '../../../miniprogram/services/announcement';
import request from '../../../miniprogram/utils/request';
import mockManager from '../../../miniprogram/utils/mock';
import store from '../../../miniprogram/utils/store';

const mockedRequest = request as jest.Mocked<typeof request>;
const mockedMock = mockManager as jest.Mocked<typeof mockManager>;
const mockedStore = store as jest.Mocked<typeof store>;

beforeEach(() => {
  jest.clearAllMocks();
});

describe('announcementApi.getAnnouncements', () => {
  const mockAnnouncements = [
    {
      id: 'ann1',
      title: 'Notice',
      content: 'Hello',
      type: 'notice' as const,
      priority: 'medium' as const,
      publishTime: '2024-01-01T00:00:00Z',
      expireTime: null,
      author: 'admin',
      status: 'published' as const
    }
  ];

  it('returns cached announcements when available and forceRefresh is false and no params', async () => {
    (mockedStore.getAnnouncements as jest.Mock).mockReturnValue(mockAnnouncements);

    const result = await announcementApi.getAnnouncements();

    expect(result.code).toBe(200);
    expect(result.data.list).toEqual(mockAnnouncements);
    expect(result.data.total).toBe(1);
    expect(mockedMock.request).not.toHaveBeenCalled();
    expect(mockedRequest.get).not.toHaveBeenCalled();
  });

  it('fetches from mock when cache is empty and mock is enabled', async () => {
    (mockedStore.getAnnouncements as jest.Mock).mockReturnValue([]);
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(true);
    const apiResponse = {
      code: 200,
      message: 'success',
      data: { list: mockAnnouncements, total: 1, page: 1, pageSize: 1 },
      timestamp: Date.now()
    };
    (mockedMock.request as jest.Mock).mockResolvedValue(apiResponse);

    const result = await announcementApi.getAnnouncements();

    expect(mockedMock.request).toHaveBeenCalledWith({
      url: '/announcements',
      method: 'GET',
      data: undefined
    });
    expect(mockedStore.setAnnouncements).toHaveBeenCalledWith(mockAnnouncements);
    expect(result).toEqual(apiResponse);
  });

  it('fetches from real API when cache is empty and mock is disabled', async () => {
    (mockedStore.getAnnouncements as jest.Mock).mockReturnValue([]);
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(false);
    const apiResponse = {
      code: 200,
      message: 'success',
      data: { list: mockAnnouncements, total: 1, page: 1, pageSize: 1 },
      timestamp: Date.now()
    };
    (mockedRequest.get as jest.Mock).mockResolvedValue(apiResponse);

    const result = await announcementApi.getAnnouncements();

    expect(mockedRequest.get).toHaveBeenCalledWith('/announcements', undefined);
    expect(mockedStore.setAnnouncements).toHaveBeenCalledWith(mockAnnouncements);
    expect(result).toEqual(apiResponse);
  });

  it('bypasses cache when params are provided', async () => {
    (mockedStore.getAnnouncements as jest.Mock).mockReturnValue(mockAnnouncements);
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(false);
    const apiResponse = {
      code: 200,
      message: 'success',
      data: { list: mockAnnouncements, total: 1, page: 1, pageSize: 1 },
      timestamp: Date.now()
    };
    (mockedRequest.get as jest.Mock).mockResolvedValue(apiResponse);

    await announcementApi.getAnnouncements({ type: 'notice' });

    expect(mockedRequest.get).toHaveBeenCalled();
  });

  it('bypasses cache when forceRefresh is true', async () => {
    (mockedStore.getAnnouncements as jest.Mock).mockReturnValue(mockAnnouncements);
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(false);
    const apiResponse = {
      code: 200,
      message: 'success',
      data: { list: mockAnnouncements, total: 1, page: 1, pageSize: 1 },
      timestamp: Date.now()
    };
    (mockedRequest.get as jest.Mock).mockResolvedValue(apiResponse);

    await announcementApi.getAnnouncements(undefined, true);

    expect(mockedRequest.get).toHaveBeenCalled();
  });
});

describe('announcementApi.getAnnouncementDetail', () => {
  const mockAnnouncement = {
    id: 'ann1',
    title: 'Notice',
    content: 'Hello World',
    type: 'notice' as const,
    priority: 'medium' as const,
    publishTime: '2024-01-01T00:00:00Z',
    expireTime: null,
    author: 'admin',
    status: 'published' as const
  };
  const apiResponse = { code: 200, message: 'success', data: mockAnnouncement, timestamp: Date.now() };

  it('calls mockManager.request when mock is enabled', async () => {
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(true);
    (mockedMock.request as jest.Mock).mockResolvedValue(apiResponse);

    const result = await announcementApi.getAnnouncementDetail('ann1');

    expect(mockedMock.request).toHaveBeenCalledWith({
      url: '/announcements/ann1',
      method: 'GET'
    });
    expect(result).toEqual(apiResponse);
  });

  it('calls request.get when mock is disabled', async () => {
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(false);
    (mockedRequest.get as jest.Mock).mockResolvedValue(apiResponse);

    const result = await announcementApi.getAnnouncementDetail('ann1');

    expect(mockedRequest.get).toHaveBeenCalledWith('/announcements/ann1');
    expect(result).toEqual(apiResponse);
  });
});
