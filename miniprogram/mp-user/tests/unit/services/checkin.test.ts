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

import { checkInApi } from '../../../miniprogram/services/checkin';
import request from '../../../miniprogram/utils/request';
import mockManager from '../../../miniprogram/utils/mock';
import store from '../../../miniprogram/utils/store';

const mockedRequest = request as jest.Mocked<typeof request>;
const mockedMock = mockManager as jest.Mocked<typeof mockManager>;
const mockedStore = store as jest.Mocked<typeof store>;

beforeEach(() => {
  jest.clearAllMocks();
});

describe('checkInApi.checkIn', () => {
  const params = { reservationId: 'res1', seatId: 'seat1' };
  const mockUser = {
    id: 'u1',
    username: 'testuser',
    nickname: 'Test',
    avatar: '',
    phone: '',
    email: '',
    studentId: '',
    creditScore: 100,
    status: 'active' as const,
    createdAt: '2024-01-01T00:00:00.000Z',
    updatedAt: '2024-01-01T00:00:00.000Z'
  };
  const mockReservation = {
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
  const checkInResponse = {
    code: 200,
    message: 'success',
    data: {
      checkInRecordId: 'cir1',
      checkInTime: '2024-01-01T09:00:00Z',
      reservationId: 'res1',
      seatId: 'seat1'
    },
    timestamp: Date.now()
  };

  it('calls mockManager.request when mock is enabled and updates store on success', async () => {
    (mockedStore.getUser as jest.Mock).mockReturnValue(mockUser);
    (mockedStore.getMyReservations as jest.Mock).mockReturnValue([mockReservation]);
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(true);
    (mockedMock.request as jest.Mock).mockResolvedValue(checkInResponse);

    const result = await checkInApi.checkIn(params);

    expect(mockedMock.request).toHaveBeenCalledWith({
      url: '/checkin',
      method: 'POST',
      data: params
    });
    expect(mockedStore.setCurrentCheckIn).toHaveBeenCalledWith({
      isCheckedIn: true,
      checkInRecord: expect.objectContaining({
        id: 'cir1',
        userId: 'u1',
        reservationId: 'res1',
        studyRoomId: 'room1',
        seatId: 'seat1',
        status: 'active'
      })
    });
    expect(mockedStore.updateReservation).toHaveBeenCalledWith(
      expect.objectContaining({ id: 'res1', status: 'checked_in' })
    );
    expect(result).toEqual(checkInResponse);
  });

  it('calls request.post when mock is disabled and updates store on success', async () => {
    (mockedStore.getUser as jest.Mock).mockReturnValue(mockUser);
    (mockedStore.getMyReservations as jest.Mock).mockReturnValue([mockReservation]);
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(false);
    (mockedRequest.post as jest.Mock).mockResolvedValue(checkInResponse);

    const result = await checkInApi.checkIn(params);

    expect(mockedRequest.post).toHaveBeenCalledWith('/checkin', params);
    expect(mockedStore.setCurrentCheckIn).toHaveBeenCalledWith({
      isCheckedIn: true,
      checkInRecord: expect.objectContaining({
        id: 'cir1',
        userId: 'u1',
        reservationId: 'res1',
        studyRoomId: 'room1',
        seatId: 'seat1',
        status: 'active'
      })
    });
    expect(mockedStore.updateReservation).toHaveBeenCalledWith(
      expect.objectContaining({ id: 'res1', status: 'checked_in' })
    );
    expect(result).toEqual(checkInResponse);
  });

  it('does not update store when response code is not 200', async () => {
    (mockedStore.getUser as jest.Mock).mockReturnValue(mockUser);
    (mockedStore.getMyReservations as jest.Mock).mockReturnValue([mockReservation]);
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(false);
    (mockedRequest.post as jest.Mock).mockResolvedValue({
      code: 400,
      message: 'bad request',
      data: null,
      timestamp: Date.now()
    });

    await checkInApi.checkIn(params);

    expect(mockedStore.setCurrentCheckIn).not.toHaveBeenCalled();
    expect(mockedStore.updateReservation).not.toHaveBeenCalled();
  });
});

describe('checkInApi.checkOut', () => {
  const params = { checkInRecordId: 'cir1' };
  const mockCheckInRecord = {
    id: 'cir1',
    userId: 'u1',
    reservationId: 'res1',
    studyRoomId: 'room1',
    seatId: 'seat1',
    checkInTime: '2024-01-01T09:00:00Z',
    checkOutTime: null,
    duration: 0,
    status: 'active' as const
  };
  const mockReservation = {
    id: 'res1',
    userId: 'u1',
    studyRoomId: 'room1',
    seatId: 'seat1',
    startTime: '2024-01-01T09:00:00Z',
    endTime: '2024-01-01T12:00:00Z',
    status: 'checked_in' as const,
    checkInTime: '2024-01-01T09:00:00Z',
    checkOutTime: null,
    createdAt: '2024-01-01T00:00:00Z',
    updatedAt: '2024-01-01T00:00:00Z'
  };
  const checkOutResponse = {
    code: 200,
    message: 'success',
    data: { checkOutTime: '2024-01-01T12:00:00Z', duration: 10800 },
    timestamp: Date.now()
  };

  it('calls mockManager.request when mock is enabled and updates store on success', async () => {
    (mockedStore.getCurrentCheckIn as jest.Mock).mockReturnValue({
      isCheckedIn: true,
      checkInRecord: mockCheckInRecord
    });
    (mockedStore.getMyReservations as jest.Mock).mockReturnValue([mockReservation]);
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(true);
    (mockedMock.request as jest.Mock).mockResolvedValue(checkOutResponse);

    const result = await checkInApi.checkOut(params);

    expect(mockedMock.request).toHaveBeenCalledWith({
      url: '/checkout',
      method: 'POST',
      data: params
    });
    expect(mockedStore.setCurrentCheckIn).toHaveBeenCalledWith({
      isCheckedIn: false,
      checkInRecord: null
    });
    expect(mockedStore.updateReservation).toHaveBeenCalledWith(
      expect.objectContaining({ id: 'res1', status: 'completed' })
    );
    expect(result).toEqual(checkOutResponse);
  });

  it('calls request.post when mock is disabled and updates store on success', async () => {
    (mockedStore.getCurrentCheckIn as jest.Mock).mockReturnValue({
      isCheckedIn: true,
      checkInRecord: mockCheckInRecord
    });
    (mockedStore.getMyReservations as jest.Mock).mockReturnValue([mockReservation]);
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(false);
    (mockedRequest.post as jest.Mock).mockResolvedValue(checkOutResponse);

    const result = await checkInApi.checkOut(params);

    expect(mockedRequest.post).toHaveBeenCalledWith('/checkout', params);
    expect(mockedStore.setCurrentCheckIn).toHaveBeenCalledWith({
      isCheckedIn: false,
      checkInRecord: null
    });
    expect(mockedStore.updateReservation).toHaveBeenCalledWith(
      expect.objectContaining({ id: 'res1', status: 'completed' })
    );
    expect(result).toEqual(checkOutResponse);
  });

  it('does not update reservation when currentCheckIn has no checkInRecord', async () => {
    (mockedStore.getCurrentCheckIn as jest.Mock).mockReturnValue({
      isCheckedIn: false,
      checkInRecord: null
    });
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(true);
    (mockedMock.request as jest.Mock).mockResolvedValue(checkOutResponse);

    await checkInApi.checkOut(params);

    expect(mockedStore.setCurrentCheckIn).toHaveBeenCalledWith({
      isCheckedIn: false,
      checkInRecord: null
    });
    expect(mockedStore.updateReservation).not.toHaveBeenCalled();
  });
});

describe('checkInApi.getMyCheckInRecords', () => {
  const cachedRecords = [
    {
      id: 'cir1',
      userId: 'u1',
      reservationId: 'res1',
      studyRoomId: 'room1',
      seatId: 'seat1',
      checkInTime: '2024-01-01T09:00:00Z',
      checkOutTime: '2024-01-01T12:00:00Z',
      duration: 10800,
      status: 'completed' as const
    }
  ];

  it('returns cached records when available and forceRefresh is false and no params', async () => {
    (mockedStore.getCheckInRecords as jest.Mock).mockReturnValue(cachedRecords);

    const result = await checkInApi.getMyCheckInRecords();

    expect(result.code).toBe(200);
    expect(result.data.list).toEqual(cachedRecords);
    expect(mockedMock.request).not.toHaveBeenCalled();
    expect(mockedRequest.get).not.toHaveBeenCalled();
  });

  it('fetches from mock when cache is empty and mock is enabled', async () => {
    (mockedStore.getCheckInRecords as jest.Mock).mockReturnValue([]);
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(true);
    const apiResponse = {
      code: 200,
      message: 'success',
      data: { list: cachedRecords, total: 1, page: 1, pageSize: 1 },
      timestamp: Date.now()
    };
    (mockedMock.request as jest.Mock).mockResolvedValue(apiResponse);

    const result = await checkInApi.getMyCheckInRecords();

    expect(mockedMock.request).toHaveBeenCalledWith({
      url: '/checkin/records',
      method: 'GET',
      data: undefined
    });
    expect(mockedStore.setCheckInRecords).toHaveBeenCalledWith(cachedRecords);
    expect(result).toEqual(apiResponse);
  });

  it('fetches from real API when cache is empty and mock is disabled', async () => {
    (mockedStore.getCheckInRecords as jest.Mock).mockReturnValue([]);
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(false);
    const apiResponse = {
      code: 200,
      message: 'success',
      data: { list: cachedRecords, total: 1, page: 1, pageSize: 1 },
      timestamp: Date.now()
    };
    (mockedRequest.get as jest.Mock).mockResolvedValue(apiResponse);

    const result = await checkInApi.getMyCheckInRecords();

    expect(mockedRequest.get).toHaveBeenCalledWith('/checkin/records', undefined);
    expect(mockedStore.setCheckInRecords).toHaveBeenCalledWith(cachedRecords);
    expect(result).toEqual(apiResponse);
  });

  it('bypasses cache when params are provided', async () => {
    (mockedStore.getCheckInRecords as jest.Mock).mockReturnValue(cachedRecords);
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(false);
    const apiResponse = {
      code: 200,
      message: 'success',
      data: { list: cachedRecords, total: 1, page: 1, pageSize: 1 },
      timestamp: Date.now()
    };
    (mockedRequest.get as jest.Mock).mockResolvedValue(apiResponse);

    await checkInApi.getMyCheckInRecords({ page: 1, pageSize: 10 });

    expect(mockedRequest.get).toHaveBeenCalled();
  });
});

describe('checkInApi.getCurrentCheckInStatus', () => {
  const mockCheckInRecord = {
    id: 'cir1',
    userId: 'u1',
    reservationId: 'res1',
    studyRoomId: 'room1',
    seatId: 'seat1',
    checkInTime: '2024-01-01T09:00:00Z',
    checkOutTime: null,
    duration: 0,
    status: 'active' as const
  };

  it('returns cached status when isCheckedIn is true and forceRefresh is false', async () => {
    (mockedStore.getCurrentCheckIn as jest.Mock).mockReturnValue({
      isCheckedIn: true,
      checkInRecord: mockCheckInRecord
    });

    const result = await checkInApi.getCurrentCheckInStatus();

    expect(result.code).toBe(200);
    expect(result.data.isCheckedIn).toBe(true);
    expect(result.data.checkInRecord).toEqual(mockCheckInRecord);
    expect(mockedMock.request).not.toHaveBeenCalled();
    expect(mockedRequest.get).not.toHaveBeenCalled();
  });

  it('fetches from mock when not checked in and mock is enabled', async () => {
    (mockedStore.getCurrentCheckIn as jest.Mock).mockReturnValue({
      isCheckedIn: false,
      checkInRecord: null
    });
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(true);
    const apiResponse = {
      code: 200,
      message: 'success',
      data: { isCheckedIn: true, checkInRecord: mockCheckInRecord },
      timestamp: Date.now()
    };
    (mockedMock.request as jest.Mock).mockResolvedValue(apiResponse);

    const result = await checkInApi.getCurrentCheckInStatus();

    expect(mockedMock.request).toHaveBeenCalledWith({
      url: '/checkin/current',
      method: 'GET'
    });
    expect(mockedStore.setCurrentCheckIn).toHaveBeenCalledWith({ isCheckedIn: true, checkInRecord: mockCheckInRecord });
    expect(result).toEqual(apiResponse);
  });

  it('fetches from real API when not checked in and mock is disabled', async () => {
    (mockedStore.getCurrentCheckIn as jest.Mock).mockReturnValue({
      isCheckedIn: false,
      checkInRecord: null
    });
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(false);
    const apiResponse = {
      code: 200,
      message: 'success',
      data: { isCheckedIn: true, checkInRecord: mockCheckInRecord },
      timestamp: Date.now()
    };
    (mockedRequest.get as jest.Mock).mockResolvedValue(apiResponse);

    const result = await checkInApi.getCurrentCheckInStatus();

    expect(mockedRequest.get).toHaveBeenCalledWith('/checkin/current');
    expect(mockedStore.setCurrentCheckIn).toHaveBeenCalledWith({ isCheckedIn: true, checkInRecord: mockCheckInRecord });
    expect(result).toEqual(apiResponse);
  });

  it('bypasses cache when forceRefresh is true', async () => {
    (mockedStore.getCurrentCheckIn as jest.Mock).mockReturnValue({
      isCheckedIn: true,
      checkInRecord: mockCheckInRecord
    });
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(false);
    const apiResponse = {
      code: 200,
      message: 'success',
      data: { isCheckedIn: true, checkInRecord: mockCheckInRecord },
      timestamp: Date.now()
    };
    (mockedRequest.get as jest.Mock).mockResolvedValue(apiResponse);

    await checkInApi.getCurrentCheckInStatus(true);

    expect(mockedRequest.get).toHaveBeenCalled();
  });
});
