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

import { seatApi } from '../../../miniprogram/services/seat';
import request from '../../../miniprogram/utils/request';
import mockManager from '../../../miniprogram/utils/mock';
import store from '../../../miniprogram/utils/store';

const mockedRequest = request as jest.Mocked<typeof request>;
const mockedMock = mockManager as jest.Mocked<typeof mockManager>;
const mockedStore = store as jest.Mocked<typeof store>;

beforeEach(() => {
  jest.clearAllMocks();
});

describe('seatApi.getSeats', () => {
  const mockSeats = [
    {
      id: 'seat1',
      studyRoomId: 'room1',
      row: 1,
      col: 1,
      seatNumber: 'A1',
      type: 'normal' as const,
      status: 'available' as const,
      facilities: [],
      lastUsedAt: ''
    }
  ];

  it('returns cached seats when available and forceRefresh is false and no params', async () => {
    (mockedStore.getSeats as jest.Mock).mockReturnValue(mockSeats);

    const result = await seatApi.getSeats('room1');

    expect(mockedStore.getSeats).toHaveBeenCalledWith('room1');
    expect(result.code).toBe(200);
    expect(result.data).toEqual(mockSeats);
    expect(mockedMock.request).not.toHaveBeenCalled();
    expect(mockedRequest.get).not.toHaveBeenCalled();
  });

  it('fetches from mock when cache is empty and mock is enabled', async () => {
    (mockedStore.getSeats as jest.Mock).mockReturnValue(null);
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(true);
    const apiResponse = { code: 200, message: 'success', data: mockSeats, timestamp: Date.now() };
    (mockedMock.request as jest.Mock).mockResolvedValue(apiResponse);

    const result = await seatApi.getSeats('room1');

    expect(mockedMock.request).toHaveBeenCalledWith({
      url: '/studyrooms/room1/seats',
      method: 'GET',
      data: undefined
    });
    expect(mockedStore.setSeats).toHaveBeenCalledWith('room1', mockSeats);
    expect(result).toEqual(apiResponse);
  });

  it('fetches from real API when cache is empty and mock is disabled', async () => {
    (mockedStore.getSeats as jest.Mock).mockReturnValue(null);
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(false);
    const apiResponse = { code: 200, message: 'success', data: mockSeats, timestamp: Date.now() };
    (mockedRequest.get as jest.Mock).mockResolvedValue(apiResponse);

    const result = await seatApi.getSeats('room1');

    expect(mockedRequest.get).toHaveBeenCalledWith('/studyrooms/room1/seats', undefined);
    expect(mockedStore.setSeats).toHaveBeenCalledWith('room1', mockSeats);
    expect(result).toEqual(apiResponse);
  });

  it('bypasses cache when params are provided', async () => {
    (mockedStore.getSeats as jest.Mock).mockReturnValue(mockSeats);
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(false);
    const apiResponse = { code: 200, message: 'success', data: mockSeats, timestamp: Date.now() };
    (mockedRequest.get as jest.Mock).mockResolvedValue(apiResponse);

    await seatApi.getSeats('room1', { status: 'available' });

    expect(mockedRequest.get).toHaveBeenCalled();
  });

  it('bypasses cache when forceRefresh is true', async () => {
    (mockedStore.getSeats as jest.Mock).mockReturnValue(mockSeats);
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(false);
    const apiResponse = { code: 200, message: 'success', data: mockSeats, timestamp: Date.now() };
    (mockedRequest.get as jest.Mock).mockResolvedValue(apiResponse);

    await seatApi.getSeats('room1', undefined, true);

    expect(mockedRequest.get).toHaveBeenCalled();
  });
});

describe('seatApi.getSeatDetail', () => {
  const mockSeat = {
    id: 'seat1',
    studyRoomId: 'room1',
    row: 1,
    col: 1,
    seatNumber: 'A1',
    type: 'normal' as const,
    status: 'available' as const,
    facilities: [],
    lastUsedAt: ''
  };
  const apiResponse = { code: 200, message: 'success', data: mockSeat, timestamp: Date.now() };

  it('calls mockManager.request when mock is enabled', async () => {
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(true);
    (mockedMock.request as jest.Mock).mockResolvedValue(apiResponse);

    const result = await seatApi.getSeatDetail('seat1');

    expect(mockedMock.request).toHaveBeenCalledWith({
      url: '/seats/seat1',
      method: 'GET'
    });
    expect(result).toEqual(apiResponse);
  });

  it('calls request.get when mock is disabled', async () => {
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(false);
    (mockedRequest.get as jest.Mock).mockResolvedValue(apiResponse);

    const result = await seatApi.getSeatDetail('seat1');

    expect(mockedRequest.get).toHaveBeenCalledWith('/seats/seat1');
    expect(result).toEqual(apiResponse);
  });
});
