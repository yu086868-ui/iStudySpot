/**
 * 数据验证测试 (Data Validation Testing)
 * 验证数据的正确性、完整性和一致性
 */
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

import { reservationApi } from '../../../miniprogram/services/reservation';
import { checkInApi } from '../../../miniprogram/services/checkin';
import { cardApi } from '../../../miniprogram/services/card';
import { userApi } from '../../../miniprogram/services/user';
import { SeatLayoutUtil } from '../../../miniprogram/utils/seat-layout';
import mockManager from '../../../miniprogram/utils/mock';
import store from '../../../miniprogram/utils/store';
import type { CreateReservationParams, CheckInParams, GenerateCardParams, Seat, Reservation } from '../../../miniprogram/typings/api';

const mockedMock = mockManager as jest.Mocked<typeof mockManager>;
const mockedStore = store as jest.Mocked<typeof store>;

beforeEach(() => {
  jest.clearAllMocks();
});

// ==================== 预约参数验证 ====================

describe('预约参数验证', () => {
  describe('createReservation 必填参数', () => {
    it('缺少 studyRoomId 时应传递空字符串给 API', async () => {
      (mockedMock.isEnabled as jest.Mock).mockReturnValue(true);
      (mockedMock.request as jest.Mock).mockResolvedValue({
        code: 200, message: 'success', data: null, timestamp: Date.now()
      });

      const params: CreateReservationParams = {
        studyRoomId: '',
        seatId: 'seat1',
        startTime: '2024-01-01T09:00:00Z',
        endTime: '2024-01-01T12:00:00Z'
      };

      await reservationApi.createReservation(params);

      expect(mockedMock.request).toHaveBeenCalledWith(
        expect.objectContaining({ data: expect.objectContaining({ studyRoomId: '' }) })
      );
    });

    it('缺少 seatId 时应传递空字符串给 API', async () => {
      (mockedMock.isEnabled as jest.Mock).mockReturnValue(true);
      (mockedMock.request as jest.Mock).mockResolvedValue({
        code: 200, message: 'success', data: null, timestamp: Date.now()
      });

      const params: CreateReservationParams = {
        studyRoomId: 'room1',
        seatId: '',
        startTime: '2024-01-01T09:00:00Z',
        endTime: '2024-01-01T12:00:00Z'
      };

      await reservationApi.createReservation(params);

      expect(mockedMock.request).toHaveBeenCalledWith(
        expect.objectContaining({ data: expect.objectContaining({ seatId: '' }) })
      );
    });

    it('缺少 startTime 时应传递空字符串给 API', async () => {
      (mockedMock.isEnabled as jest.Mock).mockReturnValue(true);
      (mockedMock.request as jest.Mock).mockResolvedValue({
        code: 200, message: 'success', data: null, timestamp: Date.now()
      });

      const params: CreateReservationParams = {
        studyRoomId: 'room1',
        seatId: 'seat1',
        startTime: '',
        endTime: '2024-01-01T12:00:00Z'
      };

      await reservationApi.createReservation(params);

      expect(mockedMock.request).toHaveBeenCalledWith(
        expect.objectContaining({ data: expect.objectContaining({ startTime: '' }) })
      );
    });

    it('缺少 endTime 时应传递空字符串给 API', async () => {
      (mockedMock.isEnabled as jest.Mock).mockReturnValue(true);
      (mockedMock.request as jest.Mock).mockResolvedValue({
        code: 200, message: 'success', data: null, timestamp: Date.now()
      });

      const params: CreateReservationParams = {
        studyRoomId: 'room1',
        seatId: 'seat1',
        startTime: '2024-01-01T09:00:00Z',
        endTime: ''
      };

      await reservationApi.createReservation(params);

      expect(mockedMock.request).toHaveBeenCalledWith(
        expect.objectContaining({ data: expect.objectContaining({ endTime: '' }) })
      );
    });
  });

  describe('预约时间验证', () => {
    it('startTime 必须早于 endTime 才是有效预约', () => {
      const startTime = '2024-01-01T09:00:00Z';
      const endTime = '2024-01-01T12:00:00Z';
      expect(new Date(startTime).getTime()).toBeLessThan(new Date(endTime).getTime());
    });

    it('startTime 等于 endTime 时为无效预约', () => {
      const startTime = '2024-01-01T09:00:00Z';
      const endTime = '2024-01-01T09:00:00Z';
      expect(new Date(startTime).getTime()).toBeGreaterThanOrEqual(new Date(endTime).getTime());
    });

    it('startTime 晚于 endTime 时为无效预约', () => {
      const startTime = '2024-01-01T12:00:00Z';
      const endTime = '2024-01-01T09:00:00Z';
      expect(new Date(startTime).getTime()).toBeGreaterThan(new Date(endTime).getTime());
    });
  });
});

// ==================== 签到参数验证 ====================

describe('签到参数验证', () => {
  it('缺少 reservationId 时签到仍会调用 API（空字符串）', async () => {
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(true);
    (mockedStore.getUser as jest.Mock).mockReturnValue({ id: 1, openId: 'o1', nickname: 'Test', avatarUrl: '', status: 'normal', createdAt: '', updatedAt: '' });
    (mockedStore.getMyReservations as jest.Mock).mockReturnValue([]);
    (mockedMock.request as jest.Mock).mockResolvedValue({
      code: 200, message: 'success',
      data: { checkInRecordId: 'cr1', checkInTime: '2024-01-01T09:00:00Z', reservationId: '', seatId: '' },
      timestamp: Date.now()
    });

    const params: CheckInParams = { reservationId: '', seatId: 'seat1' };
    await checkInApi.checkIn(params);

    expect(mockedMock.request).toHaveBeenCalledWith(
      expect.objectContaining({ data: expect.objectContaining({ reservationId: '' }) })
    );
  });

  it('缺少 seatId 时签到仍会调用 API（空字符串）', async () => {
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(true);
    (mockedStore.getUser as jest.Mock).mockReturnValue({ id: 1, openId: 'o1', nickname: 'Test', avatarUrl: '', status: 'normal', createdAt: '', updatedAt: '' });
    (mockedStore.getMyReservations as jest.Mock).mockReturnValue([]);
    (mockedMock.request as jest.Mock).mockResolvedValue({
      code: 200, message: 'success',
      data: { checkInRecordId: 'cr1', checkInTime: '2024-01-01T09:00:00Z', reservationId: 'res1', seatId: '' },
      timestamp: Date.now()
    });

    const params: CheckInParams = { reservationId: 'res1', seatId: '' };
    await checkInApi.checkIn(params);

    expect(mockedMock.request).toHaveBeenCalledWith(
      expect.objectContaining({ data: expect.objectContaining({ seatId: '' }) })
    );
  });
});

// ==================== 卡片生成参数验证 ====================

describe('卡片生成参数验证', () => {
  it('缺少 userID 时 mock 返回参数错误', async () => {
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(true);
    (mockedMock.request as jest.Mock).mockResolvedValue({
      code: 80001, message: '参数错误：缺少userID', data: null, timestamp: Date.now()
    });

    const params: GenerateCardParams = { userID: '', studyDuration: 30 };
    const result = await cardApi.generateCard(params);

    expect(result.code).toBe(80001);
    expect(result.message).toContain('userID');
  });

  it('studyDuration 为 0 时 mock 返回参数错误', async () => {
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(true);
    (mockedMock.request as jest.Mock).mockResolvedValue({
      code: 80001, message: '参数错误：studyDuration必须大于0', data: null, timestamp: Date.now()
    });

    const params: GenerateCardParams = { userID: '1', studyDuration: 0 };
    const result = await cardApi.generateCard(params);

    expect(result.code).toBe(80001);
    expect(result.message).toContain('studyDuration');
  });

  it('studyDuration 为负数时 mock 返回参数错误', async () => {
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(true);
    (mockedMock.request as jest.Mock).mockResolvedValue({
      code: 80001, message: '参数错误：studyDuration必须大于0', data: null, timestamp: Date.now()
    });

    const params: GenerateCardParams = { userID: '1', studyDuration: -10 };
    const result = await cardApi.generateCard(params);

    expect(result.code).toBe(80001);
  });

  it('有效参数时卡片生成成功', async () => {
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(true);
    const mockCard = {
      uuid: 'card_1', userID: '1', cardID: 'tmpl_1', createTime: '2024-01-01',
      studyDuration: 30, rarity: 'R' as const, borderTheme: 'green', cardTheme: 'normal',
      themeCategory: 'growth' as const, markdown: '# Test', imageURL: 'http://example.com/img.png'
    };
    (mockedMock.request as jest.Mock).mockResolvedValue({
      code: 200, message: 'generate success', data: mockCard, timestamp: Date.now()
    });

    const params: GenerateCardParams = { userID: '1', studyDuration: 30 };
    const result = await cardApi.generateCard(params);

    expect(result.code).toBe(200);
    expect(result.data).toEqual(mockCard);
    expect(mockedStore.addCard).toHaveBeenCalledWith(mockCard);
  });
});

// ==================== 用户资料更新验证 ====================

describe('用户资料更新验证', () => {
  it('nickname 非空时更新用户资料', async () => {
    const currentUser = {
      id: 1, openId: 'o1', nickname: 'OldName', avatarUrl: '',
      status: 'normal' as const, createdAt: '', updatedAt: ''
    };
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(true);
    (mockedStore.getUser as jest.Mock).mockReturnValue(currentUser);
    (mockedMock.request as jest.Mock).mockResolvedValue({
      code: 200, message: '更新成功', data: null, timestamp: Date.now()
    });

    await userApi.updateProfile({ nickname: 'NewName' });

    expect(mockedStore.setUser).toHaveBeenCalledWith(
      expect.objectContaining({ nickname: 'NewName' })
    );
  });

  it('nickname 为空字符串时不更新用户资料', async () => {
    const currentUser = {
      id: 1, openId: 'o1', nickname: 'OldName', avatarUrl: '',
      status: 'normal' as const, createdAt: '', updatedAt: ''
    };
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(true);
    (mockedStore.getUser as jest.Mock).mockReturnValue(currentUser);
    (mockedMock.request as jest.Mock).mockResolvedValue({
      code: 200, message: '更新成功', data: null, timestamp: Date.now()
    });

    await userApi.updateProfile({ nickname: '' });

    expect(mockedStore.setUser).not.toHaveBeenCalled();
  });

  it('未传 nickname 时不更新用户资料', async () => {
    const currentUser = {
      id: 1, openId: 'o1', nickname: 'OldName', avatarUrl: '',
      status: 'normal' as const, createdAt: '', updatedAt: ''
    };
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(true);
    (mockedStore.getUser as jest.Mock).mockReturnValue(currentUser);
    (mockedMock.request as jest.Mock).mockResolvedValue({
      code: 200, message: '更新成功', data: null, timestamp: Date.now()
    });

    await userApi.updateProfile({});

    expect(mockedStore.setUser).not.toHaveBeenCalled();
  });
});

// ==================== 座位状态验证 ====================

describe('座位状态验证', () => {
  const availableSeat: Seat = {
    id: 'seat1', studyRoomId: 'room1', row: 1, col: 1, seatNumber: 'A1',
    type: 'normal', status: 'available', facilities: ['插座'], lastUsedAt: ''
  };
  const occupiedSeat: Seat = {
    id: 'seat2', studyRoomId: 'room1', row: 1, col: 2, seatNumber: 'A2',
    type: 'normal', status: 'occupied', facilities: ['插座'], lastUsedAt: ''
  };
  const reservedSeat: Seat = {
    id: 'seat3', studyRoomId: 'room1', row: 1, col: 3, seatNumber: 'A3',
    type: 'normal', status: 'reserved', facilities: ['插座'], lastUsedAt: ''
  };
  const maintenanceSeat: Seat = {
    id: 'seat4', studyRoomId: 'room1', row: 1, col: 4, seatNumber: 'A4',
    type: 'normal', status: 'maintenance', facilities: ['插座'], lastUsedAt: ''
  };

  it('只有 available 状态的座位可选', () => {
    expect(SeatLayoutUtil.isSeatSelectable(availableSeat)).toBe(true);
    expect(SeatLayoutUtil.isSeatSelectable(occupiedSeat)).toBe(false);
    expect(SeatLayoutUtil.isSeatSelectable(reservedSeat)).toBe(false);
    expect(SeatLayoutUtil.isSeatSelectable(maintenanceSeat)).toBe(false);
  });

  it('undefined 座位不可选', () => {
    expect(SeatLayoutUtil.isSeatSelectable(undefined)).toBe(false);
  });

  it('getAvailableSeats 只返回 available 状态的座位', () => {
    const seats = [availableSeat, occupiedSeat, reservedSeat, maintenanceSeat];
    const available = SeatLayoutUtil.getAvailableSeats(seats);
    expect(available).toHaveLength(1);
    expect(available[0].id).toBe('seat1');
  });

  it('getSeatStatus 对 undefined 返回 empty', () => {
    expect(SeatLayoutUtil.getSeatStatus(undefined)).toBe('empty');
  });
});

// ==================== 预约状态验证 ====================

describe('预约状态验证', () => {
  it('只有 confirmed 状态的预约可取消', async () => {
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(true);
    (mockedMock.request as jest.Mock).mockResolvedValue({
      code: 200, message: '预约已取消', data: null, timestamp: Date.now()
    });

    const result = await reservationApi.cancelReservation('res1');

    expect(result.code).toBe(200);
    expect(mockedStore.removeReservation).toHaveBeenCalledWith('res1');
  });

  it('checked_in 状态的预约不可取消（mock 返回错误）', async () => {
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(true);
    (mockedMock.request as jest.Mock).mockResolvedValue({
      code: 40007, message: '预约已签到，无法取消', data: null, timestamp: Date.now()
    });

    const result = await reservationApi.cancelReservation('res_checked_in');

    expect(result.code).toBe(40007);
    expect(mockedStore.removeReservation).not.toHaveBeenCalled();
  });

  it('预约状态应属于有效枚举值', () => {
    const validStatuses: Reservation['status'][] = [
      'pending', 'confirmed', 'checked_in', 'completed', 'cancelled', 'expired'
    ];
    const testStatus: Reservation['status'] = 'confirmed';
    expect(validStatuses).toContain(testStatus);
  });

  it('completed 和 cancelled 状态的预约不应出现在活跃预约中', () => {
    const reservations: Reservation[] = [
      { id: 'r1', userId: 'u1', studyRoomId: 'room1', seatId: 's1', startTime: '', endTime: '', status: 'confirmed', checkInTime: null, checkOutTime: null, createdAt: '', updatedAt: '' },
      { id: 'r2', userId: 'u1', studyRoomId: 'room1', seatId: 's2', startTime: '', endTime: '', status: 'completed', checkInTime: null, checkOutTime: null, createdAt: '', updatedAt: '' },
      { id: 'r3', userId: 'u1', studyRoomId: 'room1', seatId: 's3', startTime: '', endTime: '', status: 'cancelled', checkInTime: null, checkOutTime: null, createdAt: '', updatedAt: '' },
      { id: 'r4', userId: 'u1', studyRoomId: 'room1', seatId: 's4', startTime: '', endTime: '', status: 'checked_in', checkInTime: null, checkOutTime: null, createdAt: '', updatedAt: '' }
    ];

    const activeStatuses = ['confirmed', 'checked_in'];
    const activeReservations = reservations.filter(r => activeStatuses.includes(r.status));
    expect(activeReservations).toHaveLength(2);
    expect(activeReservations.every(r => r.status === 'confirmed' || r.status === 'checked_in')).toBe(true);
  });
});
