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

import { cardApi } from '../../../miniprogram/services/card';
import request from '../../../miniprogram/utils/request';
import mockManager from '../../../miniprogram/utils/mock';
import store from '../../../miniprogram/utils/store';

const mockedRequest = request as jest.Mocked<typeof request>;
const mockedMock = mockManager as jest.Mocked<typeof mockManager>;
const mockedStore = store as jest.Mocked<typeof store>;

beforeEach(() => {
  jest.clearAllMocks();
});

describe('cardApi.generateCard', () => {
  const params = { userID: 'u1', studyDuration: 3600 };
  const mockCard = {
    uuid: 'card-uuid-1',
    userID: 'u1',
    cardID: 'cid1',
    createTime: '2024-01-01T00:00:00Z',
    studyDuration: 3600,
    rarity: 'SR' as const,
    borderTheme: 'gold',
    cardTheme: 'growth',
    themeCategory: 'growth' as const,
    markdown: '# Hello',
    imageURL: 'https://example.com/card.png'
  };
  const apiResponse = { code: 200, message: 'success', data: mockCard, timestamp: Date.now() };

  it('calls mockManager.request when mock is enabled and adds card to store on success', async () => {
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(true);
    (mockedMock.request as jest.Mock).mockResolvedValue(apiResponse);

    const result = await cardApi.generateCard(params);

    expect(mockedMock.request).toHaveBeenCalledWith({
      url: '/card/generate',
      method: 'POST',
      data: params
    });
    expect(mockedStore.addCard).toHaveBeenCalledWith(mockCard);
    expect(result).toEqual(apiResponse);
  });

  it('calls request.post when mock is disabled and adds card to store on success', async () => {
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(false);
    (mockedRequest.post as jest.Mock).mockResolvedValue(apiResponse);

    const result = await cardApi.generateCard(params);

    expect(mockedRequest.post).toHaveBeenCalledWith('/card/generate', params);
    expect(mockedStore.addCard).toHaveBeenCalledWith(mockCard);
    expect(result).toEqual(apiResponse);
  });

  it('does not add card to store when response code is not 200', async () => {
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(false);
    (mockedRequest.post as jest.Mock).mockResolvedValue({
      code: 400,
      message: 'bad request',
      data: null,
      timestamp: Date.now()
    });

    await cardApi.generateCard(params);

    expect(mockedStore.addCard).not.toHaveBeenCalled();
  });
});

describe('cardApi.getCardDetail', () => {
  const mockCard = {
    uuid: 'card-uuid-1',
    userID: 'u1',
    cardID: 'cid1',
    createTime: '2024-01-01T00:00:00Z',
    studyDuration: 3600,
    rarity: 'SR' as const,
    borderTheme: 'gold',
    cardTheme: 'growth',
    themeCategory: 'growth' as const,
    markdown: '# Hello',
    imageURL: 'https://example.com/card.png'
  };

  it('returns cached card when available and forceRefresh is false', async () => {
    (mockedStore.getCardById as jest.Mock).mockReturnValue(mockCard);

    const result = await cardApi.getCardDetail('card-uuid-1');

    expect(mockedStore.getCardById).toHaveBeenCalledWith('card-uuid-1');
    expect(result.code).toBe(200);
    expect(result.data).toEqual(mockCard);
    expect(mockedMock.request).not.toHaveBeenCalled();
    expect(mockedRequest.get).not.toHaveBeenCalled();
  });

  it('fetches from mock when cache is empty and mock is enabled', async () => {
    (mockedStore.getCardById as jest.Mock).mockReturnValue(null);
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(true);
    const apiResponse = { code: 200, message: 'success', data: mockCard, timestamp: Date.now() };
    (mockedMock.request as jest.Mock).mockResolvedValue(apiResponse);

    const result = await cardApi.getCardDetail('card-uuid-1');

    expect(mockedMock.request).toHaveBeenCalledWith({
      url: '/card/detail?id=card-uuid-1',
      method: 'GET'
    });
    expect(result).toEqual(apiResponse);
  });

  it('fetches from real API when cache is empty and mock is disabled', async () => {
    (mockedStore.getCardById as jest.Mock).mockReturnValue(null);
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(false);
    const apiResponse = { code: 200, message: 'success', data: mockCard, timestamp: Date.now() };
    (mockedRequest.get as jest.Mock).mockResolvedValue(apiResponse);

    const result = await cardApi.getCardDetail('card-uuid-1');

    expect(mockedRequest.get).toHaveBeenCalledWith('/card/detail', { id: 'card-uuid-1' });
    expect(result).toEqual(apiResponse);
  });

  it('bypasses cache when forceRefresh is true', async () => {
    (mockedStore.getCardById as jest.Mock).mockReturnValue(mockCard);
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(false);
    const apiResponse = { code: 200, message: 'success', data: mockCard, timestamp: Date.now() };
    (mockedRequest.get as jest.Mock).mockResolvedValue(apiResponse);

    await cardApi.getCardDetail('card-uuid-1', true);

    expect(mockedRequest.get).toHaveBeenCalled();
  });
});

describe('cardApi.getCardList', () => {
  const params = { userID: 'u1' };
  const mockCards = [
    {
      uuid: 'card-uuid-1',
      userID: 'u1',
      cardID: 'cid1',
      createTime: '2024-01-01T00:00:00Z',
      studyDuration: 3600,
      rarity: 'SR' as const,
      borderTheme: 'gold',
      cardTheme: 'growth',
      themeCategory: 'growth' as const,
      markdown: '# Hello',
      imageURL: 'https://example.com/card.png'
    }
  ];

  it('returns cached cards when available and forceRefresh is false', async () => {
    (mockedStore.getCards as jest.Mock).mockReturnValue(mockCards);

    const result = await cardApi.getCardList(params);

    expect(result.code).toBe(200);
    expect(result.data).toEqual(mockCards);
    expect(mockedMock.request).not.toHaveBeenCalled();
    expect(mockedRequest.get).not.toHaveBeenCalled();
  });

  it('fetches from mock when cache is empty and mock is enabled', async () => {
    (mockedStore.getCards as jest.Mock).mockReturnValue([]);
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(true);
    const apiResponse = { code: 200, message: 'success', data: mockCards, timestamp: Date.now() };
    (mockedMock.request as jest.Mock).mockResolvedValue(apiResponse);

    const result = await cardApi.getCardList(params);

    expect(mockedMock.request).toHaveBeenCalledWith({
      url: '/card/list',
      method: 'GET',
      data: params
    });
    expect(mockedStore.setCards).toHaveBeenCalledWith(mockCards);
    expect(result).toEqual(apiResponse);
  });

  it('fetches from real API when cache is empty and mock is disabled', async () => {
    (mockedStore.getCards as jest.Mock).mockReturnValue([]);
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(false);
    const apiResponse = { code: 200, message: 'success', data: mockCards, timestamp: Date.now() };
    (mockedRequest.get as jest.Mock).mockResolvedValue(apiResponse);

    const result = await cardApi.getCardList(params);

    expect(mockedRequest.get).toHaveBeenCalledWith('/card/list', params);
    expect(mockedStore.setCards).toHaveBeenCalledWith(mockCards);
    expect(result).toEqual(apiResponse);
  });

  it('bypasses cache when forceRefresh is true', async () => {
    (mockedStore.getCards as jest.Mock).mockReturnValue(mockCards);
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(false);
    const apiResponse = { code: 200, message: 'success', data: mockCards, timestamp: Date.now() };
    (mockedRequest.get as jest.Mock).mockResolvedValue(apiResponse);

    await cardApi.getCardList(params, true);

    expect(mockedRequest.get).toHaveBeenCalled();
  });
});
