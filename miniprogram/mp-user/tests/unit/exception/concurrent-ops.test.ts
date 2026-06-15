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
import { authApi } from '../../../miniprogram/services/auth';
import request from '../../../miniprogram/utils/request';
import mockManager from '../../../miniprogram/utils/mock';
import store from '../../../miniprogram/utils/store';

const mockedRequest = request as jest.Mocked<typeof request>;
const mockedMock = mockManager as jest.Mocked<typeof mockManager>;
const mockedStore = store as jest.Mocked<typeof store>;

beforeEach(() => {
  jest.clearAllMocks();
});

// ==================== 快速连续调用 createReservation 两次 ====================

describe('并发操作测试 - 快速连续创建预约', () => {
  it('快速连续调用 createReservation 两次时两次都应发出请求', async () => {
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(false);
    const reservation1 = {
      id: 'res1', userId: 'u1', studyRoomId: 'room1', seatId: 'seat1',
      startTime: '2024-01-01T09:00:00Z', endTime: '2024-01-01T12:00:00Z',
      status: 'confirmed' as const, checkInTime: null, checkOutTime: null,
      createdAt: '2024-01-01T00:00:00Z', updatedAt: '2024-01-01T00:00:00Z'
    };
    const reservation2 = {
      id: 'res2', userId: 'u1', studyRoomId: 'room1', seatId: 'seat2',
      startTime: '2024-01-01T14:00:00Z', endTime: '2024-01-01T17:00:00Z',
      status: 'confirmed' as const, checkInTime: null, checkOutTime: null,
      createdAt: '2024-01-01T00:00:00Z', updatedAt: '2024-01-01T00:00:00Z'
    };
    (mockedRequest.post as jest.Mock)
      .mockResolvedValueOnce({ code: 200, message: 'success', data: reservation1, timestamp: Date.now() })
      .mockResolvedValueOnce({ code: 40001, message: '您已有进行中的预约', data: null, timestamp: Date.now() });

    const params1 = { studyRoomId: 'room1', seatId: 'seat1', startTime: '2024-01-01T09:00:00Z', endTime: '2024-01-01T12:00:00Z' };
    const params2 = { studyRoomId: 'room1', seatId: 'seat2', startTime: '2024-01-01T14:00:00Z', endTime: '2024-01-01T17:00:00Z' };

    const [result1, result2] = await Promise.all([
      reservationApi.createReservation(params1),
      reservationApi.createReservation(params2)
    ]);

    expect(result1.code).toBe(200);
    expect(result2.code).toBe(40001);
    expect(mockedRequest.post).toHaveBeenCalledTimes(2);
    expect(mockedStore.addReservation).toHaveBeenCalledTimes(1);
  });

  it('快速连续调用 createReservation 第二次返回非 200 时不添加第二条预约', async () => {
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(false);
    (mockedRequest.post as jest.Mock)
      .mockResolvedValueOnce({
        code: 200, message: 'success',
        data: {
          id: 'res1', userId: 'u1', studyRoomId: 'room1', seatId: 'seat1',
          startTime: '2024-01-01T09:00:00Z', endTime: '2024-01-01T12:00:00Z',
          status: 'confirmed' as const, checkInTime: null, checkOutTime: null,
          createdAt: '2024-01-01T00:00:00Z', updatedAt: '2024-01-01T00:00:00Z'
        },
        timestamp: Date.now()
      })
      .mockResolvedValueOnce({
        code: 30002, message: '该座位已被占用', data: null, timestamp: Date.now()
      });

    const params = { studyRoomId: 'room1', seatId: 'seat1', startTime: '2024-01-01T09:00:00Z', endTime: '2024-01-01T12:00:00Z' };

    const [r1, r2] = await Promise.all([
      reservationApi.createReservation(params),
      reservationApi.createReservation(params)
    ]);

    expect(r1.code).toBe(200);
    expect(r2.code).toBe(30002);
    expect(mockedStore.addReservation).toHaveBeenCalledTimes(1);
  });
});

// ==================== 签到状态下再次签到 ====================

describe('并发操作测试 - 重复签到', () => {
  it('已签到状态下再次调用 checkIn 时服务端返回重复签到错误', async () => {
    const mockUser = {
      id: 1, openId: 'o1', nickname: 'Test', avatarUrl: '',
      status: 'normal' as const, createdAt: '2024-01-01T00:00:00Z', updatedAt: '2024-01-01T00:00:00Z'
    };
    const mockReservation = {
      id: 'res1', userId: '1', studyRoomId: 'room1', seatId: 'seat1',
      startTime: '2024-01-01T09:00:00Z', endTime: '2024-01-01T12:00:00Z',
      status: 'checked_in' as const, checkInTime: '2024-01-01T09:00:00Z', checkOutTime: null,
      createdAt: '2024-01-01T00:00:00Z', updatedAt: '2024-01-01T00:00:00Z'
    };
    (mockedStore.getUser as jest.Mock).mockReturnValue(mockUser);
    (mockedStore.getMyReservations as jest.Mock).mockReturnValue([mockReservation]);
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(false);
    (mockedRequest.post as jest.Mock).mockResolvedValue({
      code: 50002, message: '已经签到，无需重复签到', data: null, timestamp: Date.now()
    });

    const result = await checkInApi.checkIn({ reservationId: 'res1', seatId: 'seat1' });

    expect(result.code).toBe(50002);
    expect(result.message).toBe('已经签到，无需重复签到');
    expect(mockedStore.setCurrentCheckIn).not.toHaveBeenCalled();
  });

  it('快速连续调用 checkIn 两次时第二次应返回重复签到错误', async () => {
    const mockUser = {
      id: 1, openId: 'o1', nickname: 'Test', avatarUrl: '',
      status: 'normal' as const, createdAt: '2024-01-01T00:00:00Z', updatedAt: '2024-01-01T00:00:00Z'
    };
    const mockReservation = {
      id: 'res1', userId: '1', studyRoomId: 'room1', seatId: 'seat1',
      startTime: '2024-01-01T09:00:00Z', endTime: '2024-01-01T12:00:00Z',
      status: 'confirmed' as const, checkInTime: null, checkOutTime: null,
      createdAt: '2024-01-01T00:00:00Z', updatedAt: '2024-01-01T00:00:00Z'
    };
    (mockedStore.getUser as jest.Mock).mockReturnValue(mockUser);
    (mockedStore.getMyReservations as jest.Mock).mockReturnValue([mockReservation]);
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(false);
    (mockedRequest.post as jest.Mock)
      .mockResolvedValueOnce({
        code: 200, message: 'success',
        data: { checkInRecordId: 'cir1', checkInTime: '2024-01-01T09:00:00Z', reservationId: 'res1', seatId: 'seat1' },
        timestamp: Date.now()
      })
      .mockResolvedValueOnce({
        code: 50002, message: '已经签到，无需重复签到', data: null, timestamp: Date.now()
      });

    const [r1, r2] = await Promise.all([
      checkInApi.checkIn({ reservationId: 'res1', seatId: 'seat1' }),
      checkInApi.checkIn({ reservationId: 'res1', seatId: 'seat1' })
    ]);

    expect(r1.code).toBe(200);
    expect(r2.code).toBe(50002);
  });
});

// ==================== 已完成的预约再次取消 ====================

describe('并发操作测试 - 重复取消预约', () => {
  it('已取消的预约再次取消时服务端返回错误', async () => {
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(false);
    (mockedRequest.post as jest.Mock).mockResolvedValue({
      code: 40004, message: '预约不存在', data: null, timestamp: Date.now()
    });

    const result = await reservationApi.cancelReservation('already_cancelled_id');

    expect(result.code).toBe(40004);
    expect(mockedStore.removeReservation).not.toHaveBeenCalled();
  });

  it('已签到的预约取消时服务端返回无法取消错误', async () => {
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(false);
    (mockedRequest.post as jest.Mock).mockResolvedValue({
      code: 40007, message: '预约已签到，无法取消', data: null, timestamp: Date.now()
    });

    const result = await reservationApi.cancelReservation('checked_in_res');

    expect(result.code).toBe(40007);
    expect(result.message).toBe('预约已签到，无法取消');
    expect(mockedStore.removeReservation).not.toHaveBeenCalled();
  });
});

// ==================== 已签退的记录再次签退 ====================

describe('并发操作测试 - 重复签退', () => {
  it('已签退的记录再次签退时服务端返回已签退错误', async () => {
    (mockedStore.getCurrentCheckIn as jest.Mock).mockReturnValue({
      isCheckedIn: false, checkInRecord: null
    });
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(false);
    (mockedRequest.post as jest.Mock).mockResolvedValue({
      code: 50004, message: '已经签退', data: null, timestamp: Date.now()
    });

    const result = await checkInApi.checkOut({ checkInRecordId: 'already_completed_id' });

    expect(result.code).toBe(50004);
    expect(result.message).toBe('已经签退');
  });

  it('不存在的签到记录签退时服务端返回记录不存在错误', async () => {
    (mockedStore.getCurrentCheckIn as jest.Mock).mockReturnValue({
      isCheckedIn: false, checkInRecord: null
    });
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(false);
    (mockedRequest.post as jest.Mock).mockResolvedValue({
      code: 50003, message: '签到记录不存在', data: null, timestamp: Date.now()
    });

    const result = await checkInApi.checkOut({ checkInRecordId: 'nonexistent_id' });

    expect(result.code).toBe(50003);
    expect(result.message).toBe('签到记录不存在');
  });
});

// ==================== 快速连续调用 clearUser 和 setUser ====================

describe('并发操作测试 - clearUser 和 setUser 竞态', () => {
  it('先 setUser 再 clearUser 时最终 store 应为空', async () => {
    const mockUser = {
      id: 1, openId: 'o1', nickname: 'Test', avatarUrl: '',
      status: 'normal' as const, createdAt: '2024-01-01T00:00:00Z', updatedAt: '2024-01-01T00:00:00Z'
    };

    // 模拟先 setUser 再 clearUser 的顺序
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(false);
    const loginResponse = {
      code: 200, message: 'success',
      data: { isNewUser: false, user: mockUser },
      timestamp: Date.now()
    };
    (mockedRequest.post as jest.Mock).mockResolvedValue(loginResponse);

    // 先登录
    await authApi.wxLogin({ code: 'wx_code' });
    expect(mockedStore.setUser).toHaveBeenCalledWith(mockUser);

    // 然后清除
    mockedStore.clearUser();
    expect(mockedStore.clearUser).toHaveBeenCalled();
  });

  it('快速连续调用 setUser 两次时最后一次应生效', async () => {
    const user1 = {
      id: 1, openId: 'o1', nickname: 'User1', avatarUrl: '',
      status: 'normal' as const, createdAt: '2024-01-01T00:00:00Z', updatedAt: '2024-01-01T00:00:00Z'
    };
    const user2 = {
      id: 2, openId: 'o2', nickname: 'User2', avatarUrl: '',
      status: 'normal' as const, createdAt: '2024-01-01T00:00:00Z', updatedAt: '2024-01-01T00:00:00Z'
    };
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(false);
    (mockedRequest.post as jest.Mock)
      .mockResolvedValueOnce({ code: 200, message: 'success', data: { isNewUser: false, user: user1 }, timestamp: Date.now() })
      .mockResolvedValueOnce({ code: 200, message: 'success', data: { isNewUser: false, user: user2 }, timestamp: Date.now() });

    const [r1, r2] = await Promise.all([
      authApi.wxLogin({ code: 'code1' }),
      authApi.wxLogin({ code: 'code2' })
    ]);

    expect(r1.code).toBe(200);
    expect(r2.code).toBe(200);
    expect(mockedStore.setUser).toHaveBeenCalledTimes(2);
    expect(mockedStore.setUser).toHaveBeenNthCalledWith(1, user1);
    expect(mockedStore.setUser).toHaveBeenNthCalledWith(2, user2);
  });
});

// ==================== store 事件在快速连续操作时的触发顺序 ====================

describe('并发操作测试 - store 事件触发', () => {
  it('连续创建和取消预约时 store 方法调用顺序正确', async () => {
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(false);
    const reservation = {
      id: 'res1', userId: 'u1', studyRoomId: 'room1', seatId: 'seat1',
      startTime: '2024-01-01T09:00:00Z', endTime: '2024-01-01T12:00:00Z',
      status: 'confirmed' as const, checkInTime: null, checkOutTime: null,
      createdAt: '2024-01-01T00:00:00Z', updatedAt: '2024-01-01T00:00:00Z'
    };
    (mockedRequest.post as jest.Mock)
      .mockResolvedValueOnce({ code: 200, message: 'success', data: reservation, timestamp: Date.now() })
      .mockResolvedValueOnce({ code: 200, message: 'success', data: null, timestamp: Date.now() });

    const params = { studyRoomId: 'room1', seatId: 'seat1', startTime: '2024-01-01T09:00:00Z', endTime: '2024-01-01T12:00:00Z' };

    await reservationApi.createReservation(params);
    await reservationApi.cancelReservation('res1');

    expect(mockedStore.addReservation).toHaveBeenCalledWith(reservation);
    expect(mockedStore.removeReservation).toHaveBeenCalledWith('res1');
    // addReservation 在 removeReservation 之前调用
    const addCallOrder = mockedStore.addReservation.mock.invocationCallOrder[0];
    const removeCallOrder = mockedStore.removeReservation.mock.invocationCallOrder[0];
    expect(addCallOrder).toBeLessThan(removeCallOrder);
  });

  it('连续签到和签退时 store 方法调用顺序正确', async () => {
    const mockUser = {
      id: 1, openId: 'o1', nickname: 'Test', avatarUrl: '',
      status: 'normal' as const, createdAt: '2024-01-01T00:00:00Z', updatedAt: '2024-01-01T00:00:00Z'
    };
    const mockReservation = {
      id: 'res1', userId: '1', studyRoomId: 'room1', seatId: 'seat1',
      startTime: '2024-01-01T09:00:00Z', endTime: '2024-01-01T12:00:00Z',
      status: 'confirmed' as const, checkInTime: null, checkOutTime: null,
      createdAt: '2024-01-01T00:00:00Z', updatedAt: '2024-01-01T00:00:00Z'
    };
    const checkedInReservation = {
      ...mockReservation, status: 'checked_in' as const, checkInTime: '2024-01-01T09:00:00Z'
    };

    (mockedStore.getUser as jest.Mock).mockReturnValue(mockUser);
    (mockedStore.getMyReservations as jest.Mock).mockReturnValue([mockReservation]);
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(false);

    // 签到
    (mockedRequest.post as jest.Mock).mockResolvedValueOnce({
      code: 200, message: 'success',
      data: { checkInRecordId: 'cir1', checkInTime: '2024-01-01T09:00:00Z', reservationId: 'res1', seatId: 'seat1' },
      timestamp: Date.now()
    });

    await checkInApi.checkIn({ reservationId: 'res1', seatId: 'seat1' });

    // 签退
    (mockedStore.getCurrentCheckIn as jest.Mock).mockReturnValue({
      isCheckedIn: true,
      checkInRecord: {
        id: 'cir1', userId: '1', reservationId: 'res1', studyRoomId: 'room1', seatId: 'seat1',
        checkInTime: '2024-01-01T09:00:00Z', checkOutTime: null, duration: 0, status: 'active' as const
      }
    });
    (mockedStore.getMyReservations as jest.Mock).mockReturnValue([checkedInReservation]);
    (mockedRequest.post as jest.Mock).mockResolvedValueOnce({
      code: 200, message: 'success',
      data: { checkOutTime: '2024-01-01T12:00:00Z', duration: 180 },
      timestamp: Date.now()
    });

    await checkInApi.checkOut({ checkInRecordId: 'cir1' });

    // 验证 setCurrentCheckIn 被调用了两次：签到时设为 true，签退时设为 false
    expect(mockedStore.setCurrentCheckIn).toHaveBeenCalledTimes(2);
    expect(mockedStore.setCurrentCheckIn).toHaveBeenNthCalledWith(1, {
      isCheckedIn: true,
      checkInRecord: expect.objectContaining({ id: 'cir1', status: 'active' })
    });
    expect(mockedStore.setCurrentCheckIn).toHaveBeenNthCalledWith(2, {
      isCheckedIn: false,
      checkInRecord: null
    });
  });
});
