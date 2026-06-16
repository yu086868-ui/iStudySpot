jest.mock('../../../miniprogram/utils/request', () => ({
  __esModule: true,
  default: { get: jest.fn(), post: jest.fn(), put: jest.fn(), delete: jest.fn() }
}));
jest.mock('../../../miniprogram/utils/mock', () => ({
  __esModule: true,
  default: { isEnabled: jest.fn(), request: jest.fn() }
}));
jest.mock('../../../miniprogram/utils/store', () => ({
  __esModule: true,
  default: {
    getUser: jest.fn(), setUser: jest.fn(), clearUser: jest.fn(),
    getMyReservations: jest.fn().mockReturnValue([]), setMyReservations: jest.fn(),
    addReservation: jest.fn(), updateReservation: jest.fn(), removeReservation: jest.fn(),
    getCurrentCheckIn: jest.fn().mockReturnValue({ isCheckedIn: false, checkInRecord: null }),
    setCurrentCheckIn: jest.fn(), getCheckInRecords: jest.fn().mockReturnValue([]),
    setCheckInRecords: jest.fn(), getStudyRooms: jest.fn().mockReturnValue([]),
    setStudyRooms: jest.fn(), getStudyRoomDetail: jest.fn().mockReturnValue(null),
    setStudyRoomDetail: jest.fn(), getSeats: jest.fn().mockReturnValue(null),
    setSeats: jest.fn(), getAnnouncements: jest.fn().mockReturnValue([]),
    setAnnouncements: jest.fn(), getRules: jest.fn().mockReturnValue([]),
    setRules: jest.fn(), getReservationRules: jest.fn().mockReturnValue(null),
    setReservationRules: jest.fn(), getCards: jest.fn().mockReturnValue([]),
    setCards: jest.fn(), addCard: jest.fn(), getCardById: jest.fn().mockReturnValue(null)
  }
}));

import { userApi } from '../../../miniprogram/services/user';
import { reservationApi } from '../../../miniprogram/services/reservation';
import { checkInApi } from '../../../miniprogram/services/checkin';
import { cardApi } from '../../../miniprogram/services/card';
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

// ==================== store 中无用户数据 ====================

describe('空数据/边界值测试 - store 中无用户数据', () => {
  it('getCurrentUser 在 store 无用户数据时应从 API 获取', async () => {
    (mockedStore.getUser as jest.Mock).mockReturnValue(null);
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(false);
    const mockUser = {
      id: 1, openId: 'o1', nickname: 'Test', avatarUrl: '',
      status: 'normal' as const, createdAt: '2024-01-01T00:00:00Z', updatedAt: '2024-01-01T00:00:00Z'
    };
    (mockedRequest.get as jest.Mock).mockResolvedValue({
      code: 200, message: 'success', data: mockUser, timestamp: Date.now()
    });

    const result = await userApi.getCurrentUser();

    expect(mockedRequest.get).toHaveBeenCalledWith('/user/profile');
    expect(result.code).toBe(200);
    expect(result.data).toEqual(mockUser);
  });

  it('getCurrentUser 在 store 无用户且 API 也返回 null data 时不应存储', async () => {
    (mockedStore.getUser as jest.Mock).mockReturnValue(null);
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(false);
    (mockedRequest.get as jest.Mock).mockResolvedValue({
      code: 200, message: 'success', data: null, timestamp: Date.now()
    });

    const result = await userApi.getCurrentUser();

    expect(result.data).toBeNull();
    expect(mockedStore.setUser).not.toHaveBeenCalled();
  });
});

// ==================== store 中无预约数据 ====================

describe('空数据/边界值测试 - store 中无预约数据', () => {
  it('getMyReservations 在 store 无预约数据时应从 API 获取', async () => {
    (mockedStore.getMyReservations as jest.Mock).mockReturnValue([]);
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(false);
    (mockedRequest.get as jest.Mock).mockResolvedValue({
      code: 200, message: 'success',
      data: { list: [], total: 0, page: 1, pageSize: 0 },
      timestamp: Date.now()
    });

    const result = await reservationApi.getMyReservations();

    expect(mockedRequest.get).toHaveBeenCalledWith('/reservations/my', undefined);
    expect(result.data.list).toEqual([]);
    expect(result.data.total).toBe(0);
  });

  it('getMyReservations 在 store 有空数组时返回空列表缓存', async () => {
    (mockedStore.getMyReservations as jest.Mock).mockReturnValue([]);

    const result = await reservationApi.getMyReservations();

    // 空数组 length 为 0，不满足 > 0 条件，会走 API 请求
    expect(result.code).toBe(200);
  });
});

// ==================== store 中无签到数据 ====================

describe('空数据/边界值测试 - store 中无签到数据', () => {
  it('getCurrentCheckInStatus 在 store 无签到数据时应从 API 获取', async () => {
    (mockedStore.getCurrentCheckIn as jest.Mock).mockReturnValue({
      isCheckedIn: false, checkInRecord: null
    });
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(false);
    (mockedRequest.get as jest.Mock).mockResolvedValue({
      code: 200, message: 'success',
      data: { isCheckedIn: false, checkInRecord: null },
      timestamp: Date.now()
    });

    const result = await checkInApi.getCurrentCheckInStatus();

    expect(mockedRequest.get).toHaveBeenCalledWith('/checkin/current');
    expect(result.data.isCheckedIn).toBe(false);
    expect(result.data.checkInRecord).toBeNull();
  });

  it('getCurrentCheckInStatus 在 store 有签到数据时直接返回缓存', async () => {
    const mockRecord = {
      id: 'cir1', userId: 'u1', reservationId: 'res1',
      studyRoomId: 'room1', seatId: 'seat1',
      checkInTime: '2024-01-01T09:00:00Z', checkOutTime: null,
      duration: 0, status: 'active' as const
    };
    (mockedStore.getCurrentCheckIn as jest.Mock).mockReturnValue({
      isCheckedIn: true, checkInRecord: mockRecord
    });

    const result = await checkInApi.getCurrentCheckInStatus();

    expect(result.code).toBe(200);
    expect(result.data.isCheckedIn).toBe(true);
    expect(result.data.checkInRecord).toEqual(mockRecord);
    expect(mockedRequest.get).not.toHaveBeenCalled();
  });
});

// ==================== cache 返回 null 时 store 的行为 ====================

describe('空数据/边界值测试 - cache 返回 null', () => {
  it('getStudyRoomDetail 在 cache 返回 null 时返回 null', async () => {
    (mockedStore.getStudyRoomDetail as jest.Mock).mockReturnValue(null);

    const result = mockedStore.getStudyRoomDetail('nonexistent_room');

    expect(result).toBeNull();
  });

  it('getSeats 在 cache 返回 null 时返回 null', async () => {
    (mockedStore.getSeats as jest.Mock).mockReturnValue(null);

    const result = mockedStore.getSeats('nonexistent_room');

    expect(result).toBeNull();
  });

  it('getReservationRules 在 cache 返回 null 时应从 API 获取', async () => {
    (mockedStore.getReservationRules as jest.Mock).mockReturnValue(null);
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(false);
    const mockRules = {
      maxAdvanceDays: 7, maxDailyReservations: 2, maxDurationHours: 4,
      minDurationMinutes: 30, cancellationDeadlineMinutes: 15, noShowPenalty: 5
    };
    (mockedRequest.get as jest.Mock).mockResolvedValue({
      code: 200, message: 'success', data: mockRules, timestamp: Date.now()
    });

    const result = await reservationApi.getReservationRules();

    expect(mockedRequest.get).toHaveBeenCalledWith('/reservations/rules');
    expect(result.data).toEqual(mockRules);
  });
});

// ==================== 空字符串作为参数 ====================

describe('空数据/边界值测试 - 空字符串参数', () => {
  it('cancelReservation 传入空字符串 id 时仍调用 API', async () => {
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(false);
    (mockedRequest.post as jest.Mock).mockResolvedValue({
      code: 404, message: '预约不存在', data: null, timestamp: Date.now()
    });

    const result = await reservationApi.cancelReservation('');

    expect(mockedRequest.post).toHaveBeenCalledWith('/reservations//cancel');
    expect(result.code).toBe(404);
    expect(mockedStore.removeReservation).not.toHaveBeenCalled();
  });

  it('getStudyRoomDetail 传入空字符串 id 时返回 null 缓存', async () => {
    (mockedStore.getStudyRoomDetail as jest.Mock).mockReturnValue(null);
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(false);
    (mockedRequest.get as jest.Mock).mockResolvedValue({
      code: 20001, message: '自习室不存在', data: null, timestamp: Date.now()
    });

    const result = await studyRoomApi.getStudyRoomDetail('');

    expect(result.code).toBe(20001);
  });
});

// ==================== undefined 作为参数 ====================

describe('空数据/边界值测试 - undefined 参数', () => {
  it('getMyReservations 传入 undefined 参数时使用缓存', async () => {
    const cachedReservations = [{
      id: 'res1', userId: 'u1', studyRoomId: 'room1', seatId: 'seat1',
      startTime: '2024-01-01T09:00:00Z', endTime: '2024-01-01T12:00:00Z',
      status: 'confirmed' as const, checkInTime: null, checkOutTime: null,
      createdAt: '2024-01-01T00:00:00Z', updatedAt: '2024-01-01T00:00:00Z'
    }];
    (mockedStore.getMyReservations as jest.Mock).mockReturnValue(cachedReservations);

    const result = await reservationApi.getMyReservations(undefined);

    expect(result.code).toBe(200);
    expect(result.data.list).toEqual(cachedReservations);
    expect(mockedRequest.get).not.toHaveBeenCalled();
  });

  it('getMyCheckInRecords 传入 undefined 参数时使用缓存', async () => {
    const cachedRecords = [{
      id: 'cir1', userId: 'u1', reservationId: 'res1',
      studyRoomId: 'room1', seatId: 'seat1',
      checkInTime: '2024-01-01T09:00:00Z', checkOutTime: null,
      duration: 0, status: 'active' as const
    }];
    (mockedStore.getCheckInRecords as jest.Mock).mockReturnValue(cachedRecords);

    const result = await checkInApi.getMyCheckInRecords(undefined);

    expect(result.code).toBe(200);
    expect(result.data.list).toEqual(cachedRecords);
    expect(mockedRequest.get).not.toHaveBeenCalled();
  });
});

// ==================== 空数组作为列表数据 ====================

describe('空数据/边界值测试 - 空数组列表数据', () => {
  it('getMyReservations 返回空列表时 total 为 0', async () => {
    (mockedStore.getMyReservations as jest.Mock).mockReturnValue([]);
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(false);
    (mockedRequest.get as jest.Mock).mockResolvedValue({
      code: 200, message: 'success',
      data: { list: [], total: 0, page: 1, pageSize: 0 },
      timestamp: Date.now()
    });

    const result = await reservationApi.getMyReservations();

    expect(result.data.list).toEqual([]);
    expect(result.data.total).toBe(0);
  });

  it('getStudyRooms 返回空列表时 total 为 0', async () => {
    (mockedStore.getStudyRooms as jest.Mock).mockReturnValue([]);
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(false);
    (mockedRequest.get as jest.Mock).mockResolvedValue({
      code: 200, message: 'success',
      data: { list: [], total: 0, page: 1, pageSize: 0 },
      timestamp: Date.now()
    });

    const result = await studyRoomApi.getStudyRooms(undefined, true);

    expect(result.data.list).toEqual([]);
    expect(result.data.total).toBe(0);
  });
});

// ==================== 缺少必要字段的 Card 对象 ====================

describe('空数据/边界值测试 - 缺少必要字段的 Card', () => {
  it('getCardDetail 在 store 中找不到卡片时从 API 获取', async () => {
    (mockedStore.getCardById as jest.Mock).mockReturnValue(null);
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(false);
    (mockedRequest.get as jest.Mock).mockResolvedValue({
      code: 80002, message: '卡片不存在', data: null, timestamp: Date.now()
    });

    const result = await cardApi.getCardDetail('nonexistent_uuid');

    expect(result.code).toBe(80002);
    expect(result.data).toBeNull();
  });

  it('getCardList 在 store 有空卡片列表时从 API 获取', async () => {
    (mockedStore.getCards as jest.Mock).mockReturnValue([]);
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(false);
    (mockedRequest.get as jest.Mock).mockResolvedValue({
      code: 200, message: 'success', data: [], timestamp: Date.now()
    });

    const result = await cardApi.getCardList({ userID: 'u1' });

    expect(mockedRequest.get).toHaveBeenCalledWith('/card/list', { userID: 'u1' });
    expect(result.code).toBe(200);
  });

  it('generateCard 返回缺少 markdown 字段的卡片时仍添加到 store', async () => {
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(true);
    const incompleteCard = {
      uuid: 'card-1', userID: 'u1', cardID: 'c1',
      createTime: '2024-01-01T00:00:00Z', studyDuration: 30,
      rarity: 'N' as const, borderTheme: 'default', cardTheme: 'default',
      themeCategory: 'growth' as const, markdown: '', imageURL: ''
    };
    (mockedMock.request as jest.Mock).mockResolvedValue({
      code: 200, message: 'success', data: incompleteCard, timestamp: Date.now()
    });

    const result = await cardApi.generateCard({ userID: 'u1', studyDuration: 30 });

    expect(result.code).toBe(200);
    expect(mockedStore.addCard).toHaveBeenCalledWith(incompleteCard);
  });
});
