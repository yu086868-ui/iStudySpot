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

import { reservationApi } from '../../../miniprogram/services/reservation';
import request from '../../../miniprogram/utils/request';
import mockManager from '../../../miniprogram/utils/mock';
import store from '../../../miniprogram/utils/store';

const mockedRequest = request as jest.Mocked<typeof request>;
const mockedMock = mockManager as jest.Mocked<typeof mockManager>;
const mockedStore = store as jest.Mocked<typeof store>;

beforeEach(() => {
  jest.clearAllMocks();
});

describe('reservationApi.createReservation', () => {
  const params = { studyRoomId: 'room1', seatId: 'seat1', startTime: '2024-01-01T09:00:00Z', endTime: '2024-01-01T12:00:00Z' };
  const reservation = {
    id: 'res1',
    userId: 'u1',
    studyRoomId: 'room1',
    seatId: 'seat1',
    startTime: '2024-01-01T09:00:00Z',
    endTime: '2024-01-01T12:00:00Z',
    status: 'confirmed' as const,
    checkInTime: null,
    checkOutTime: null,
    createdAt: '2024-01-01T00:00:00Z',
    updatedAt: '2024-01-01T00:00:00Z'
  };
  const apiResponse = { code: 200, message: 'success', data: reservation, timestamp: Date.now() };

  it('calls mockManager.request when mock is enabled and adds reservation to store on success', async () => {
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(true);
    (mockedMock.request as jest.Mock).mockResolvedValue(apiResponse);

    const result = await reservationApi.createReservation(params);

    expect(mockedMock.request).toHaveBeenCalledWith({
      url: '/reservations',
      method: 'POST',
      data: params
    });
    expect(mockedStore.addReservation).toHaveBeenCalledWith(reservation);
    expect(result).toEqual(apiResponse);
  });

  it('calls request.post when mock is disabled and adds reservation to store on success', async () => {
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(false);
    (mockedRequest.post as jest.Mock).mockResolvedValue(apiResponse);

    const result = await reservationApi.createReservation(params);

    expect(mockedRequest.post).toHaveBeenCalledWith('/reservations', params);
    expect(mockedStore.addReservation).toHaveBeenCalledWith(reservation);
    expect(result).toEqual(apiResponse);
  });

  it('does not add reservation to store when response code is not 200', async () => {
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(false);
    (mockedRequest.post as jest.Mock).mockResolvedValue({
      code: 400,
      message: 'bad request',
      data: null,
      timestamp: Date.now()
    });

    await reservationApi.createReservation(params);

    expect(mockedStore.addReservation).not.toHaveBeenCalled();
  });
});

describe('reservationApi.getMyReservations', () => {
  const cachedReservations = [
    {
      id: 'res1',
      userId: 'u1',
      studyRoomId: 'room1',
      seatId: 'seat1',
      startTime: '2024-01-01T09:00:00Z',
      endTime: '2024-01-01T12:00:00Z',
      status: 'confirmed' as const,
      checkInTime: null,
      checkOutTime: null,
      createdAt: '2024-01-01T00:00:00Z',
      updatedAt: '2024-01-01T00:00:00Z'
    },
    {
      id: 'res2',
      userId: 'u1',
      studyRoomId: 'room1',
      seatId: 'seat2',
      startTime: '2024-01-02T09:00:00Z',
      endTime: '2024-01-02T12:00:00Z',
      status: 'cancelled' as const,
      checkInTime: null,
      checkOutTime: null,
      createdAt: '2024-01-02T00:00:00Z',
      updatedAt: '2024-01-02T00:00:00Z'
    }
  ];

  it('returns cached reservations when available and forceRefresh is false', async () => {
    (mockedStore.getMyReservations as jest.Mock).mockReturnValue(cachedReservations);

    const result = await reservationApi.getMyReservations();

    expect(result.code).toBe(200);
    expect(result.data.list).toEqual(cachedReservations);
    expect(result.data.total).toBe(2);
    expect(mockedMock.request).not.toHaveBeenCalled();
    expect(mockedRequest.get).not.toHaveBeenCalled();
  });

  it('filters cached reservations by status when params.status is provided', async () => {
    (mockedStore.getMyReservations as jest.Mock).mockReturnValue(cachedReservations);

    const result = await reservationApi.getMyReservations({ status: 'confirmed' });

    expect(result.data.list).toEqual([cachedReservations[0]]);
    expect(result.data.total).toBe(1);
  });

  it('fetches from mock when cache is empty and mock is enabled', async () => {
    (mockedStore.getMyReservations as jest.Mock).mockReturnValue([]);
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(true);
    const apiResponse = {
      code: 200,
      message: 'success',
      data: { list: cachedReservations, total: 2, page: 1, pageSize: 2 },
      timestamp: Date.now()
    };
    (mockedMock.request as jest.Mock).mockResolvedValue(apiResponse);

    const result = await reservationApi.getMyReservations();

    expect(mockedMock.request).toHaveBeenCalledWith({
      url: '/reservations/my',
      method: 'GET',
      data: undefined
    });
    expect(mockedStore.setMyReservations).toHaveBeenCalledWith(cachedReservations);
    expect(result).toEqual(apiResponse);
  });

  it('fetches from real API when cache is empty and mock is disabled', async () => {
    (mockedStore.getMyReservations as jest.Mock).mockReturnValue([]);
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(false);
    const apiResponse = {
      code: 200,
      message: 'success',
      data: { list: cachedReservations, total: 2, page: 1, pageSize: 2 },
      timestamp: Date.now()
    };
    (mockedRequest.get as jest.Mock).mockResolvedValue(apiResponse);

    const result = await reservationApi.getMyReservations();

    expect(mockedRequest.get).toHaveBeenCalledWith('/reservations/my', undefined);
    expect(mockedStore.setMyReservations).toHaveBeenCalledWith(cachedReservations);
    expect(result).toEqual(apiResponse);
  });

  it('bypasses cache when forceRefresh is true', async () => {
    (mockedStore.getMyReservations as jest.Mock).mockReturnValue(cachedReservations);
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(false);
    const apiResponse = {
      code: 200,
      message: 'success',
      data: { list: cachedReservations, total: 2, page: 1, pageSize: 2 },
      timestamp: Date.now()
    };
    (mockedRequest.get as jest.Mock).mockResolvedValue(apiResponse);

    await reservationApi.getMyReservations(undefined, true);

    expect(mockedRequest.get).toHaveBeenCalled();
  });
});

describe('reservationApi.cancelReservation', () => {
  const apiResponse = { code: 200, message: 'success', data: null, timestamp: Date.now() };

  it('calls mockManager.request when mock is enabled and removes reservation from store on success', async () => {
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(true);
    (mockedMock.request as jest.Mock).mockResolvedValue(apiResponse);

    const result = await reservationApi.cancelReservation('res1');

    expect(mockedMock.request).toHaveBeenCalledWith({
      url: '/reservations/res1/cancel',
      method: 'POST'
    });
    expect(mockedStore.removeReservation).toHaveBeenCalledWith('res1');
    expect(result).toEqual(apiResponse);
  });

  it('calls request.post when mock is disabled and removes reservation from store on success', async () => {
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(false);
    (mockedRequest.post as jest.Mock).mockResolvedValue(apiResponse);

    const result = await reservationApi.cancelReservation('res1');

    expect(mockedRequest.post).toHaveBeenCalledWith('/reservations/res1/cancel');
    expect(mockedStore.removeReservation).toHaveBeenCalledWith('res1');
    expect(result).toEqual(apiResponse);
  });

  it('does not remove reservation from store when response code is not 200', async () => {
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(false);
    (mockedRequest.post as jest.Mock).mockResolvedValue({
      code: 400,
      message: 'bad request',
      data: null,
      timestamp: Date.now()
    });

    await reservationApi.cancelReservation('res1');

    expect(mockedStore.removeReservation).not.toHaveBeenCalled();
  });
});

describe('reservationApi.getReservationRules', () => {
  const mockRules = {
    maxAdvanceDays: 7,
    maxDailyReservations: 3,
    maxDurationHours: 4,
    minDurationMinutes: 30,
    cancellationDeadlineMinutes: 60,
    noShowPenalty: 5
  };
  const apiResponse = { code: 200, message: 'success', data: mockRules, timestamp: Date.now() };

  it('returns cached rules when available and forceRefresh is false', async () => {
    (mockedStore.getReservationRules as jest.Mock).mockReturnValue(mockRules);

    const result = await reservationApi.getReservationRules();

    expect(result.code).toBe(200);
    expect(result.data).toEqual(mockRules);
    expect(mockedMock.request).not.toHaveBeenCalled();
    expect(mockedRequest.get).not.toHaveBeenCalled();
  });

  it('fetches from mock when cache is empty and mock is enabled', async () => {
    (mockedStore.getReservationRules as jest.Mock).mockReturnValue(null);
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(true);
    (mockedMock.request as jest.Mock).mockResolvedValue(apiResponse);

    const result = await reservationApi.getReservationRules();

    expect(mockedMock.request).toHaveBeenCalledWith({
      url: '/reservations/rules',
      method: 'GET'
    });
    expect(mockedStore.setReservationRules).toHaveBeenCalledWith(mockRules);
    expect(result).toEqual(apiResponse);
  });

  it('fetches from real API when cache is empty and mock is disabled', async () => {
    (mockedStore.getReservationRules as jest.Mock).mockReturnValue(null);
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(false);
    (mockedRequest.get as jest.Mock).mockResolvedValue(apiResponse);

    const result = await reservationApi.getReservationRules();

    expect(mockedRequest.get).toHaveBeenCalledWith('/reservations/rules');
    expect(mockedStore.setReservationRules).toHaveBeenCalledWith(mockRules);
    expect(result).toEqual(apiResponse);
  });

  it('bypasses cache when forceRefresh is true', async () => {
    (mockedStore.getReservationRules as jest.Mock).mockReturnValue(mockRules);
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(false);
    (mockedRequest.get as jest.Mock).mockResolvedValue(apiResponse);

    await reservationApi.getReservationRules(true);

    expect(mockedRequest.get).toHaveBeenCalled();
  });
});
