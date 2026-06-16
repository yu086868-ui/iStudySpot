jest.mock('../../../miniprogram/utils/request', () => ({
  __esModule: true,
  default: { get: jest.fn(), post: jest.fn(), put: jest.fn(), delete: jest.fn(), getBaseURL: jest.fn().mockReturnValue('https://192.168.21.3:8080/api/wx') }
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

import { authApi } from '../../../miniprogram/services/auth';
import { reservationApi } from '../../../miniprogram/services/reservation';
import { checkInApi } from '../../../miniprogram/services/checkin';
import { userApi } from '../../../miniprogram/services/user';
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

// ==================== request.post 抛出异常 ====================

describe('网络异常测试 - request.post 抛出异常', () => {
  it('reservationApi.createReservation 在 request.post 抛出异常时应 reject', async () => {
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(false);
    (mockedRequest.post as jest.Mock).mockRejectedValue(new Error('Network Error'));

    await expect(reservationApi.createReservation({
      studyRoomId: 'room1', seatId: 'seat1',
      startTime: '2024-01-01T09:00:00Z', endTime: '2024-01-01T12:00:00Z'
    })).rejects.toThrow('Network Error');

    expect(mockedStore.addReservation).not.toHaveBeenCalled();
  });

  it('authApi.wxLogin 在 request.post 抛出异常时应 reject', async () => {
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(false);
    (mockedRequest.post as jest.Mock).mockRejectedValue(new Error('Connection refused'));

    await expect(authApi.wxLogin({ code: 'wx_code' }))
      .rejects.toThrow('Connection refused');

    expect(mockedStore.setUser).not.toHaveBeenCalled();
  });

  it('checkInApi.checkIn 在 request.post 抛出异常时应 reject', async () => {
    (mockedStore.getUser as jest.Mock).mockReturnValue({ id: 1, openId: 'o1' });
    (mockedStore.getMyReservations as jest.Mock).mockReturnValue([]);
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(false);
    (mockedRequest.post as jest.Mock).mockRejectedValue(new Error('Timeout'));

    await expect(checkInApi.checkIn({ reservationId: 'res1', seatId: 'seat1' }))
      .rejects.toThrow('Timeout');

    expect(mockedStore.setCurrentCheckIn).not.toHaveBeenCalled();
  });
});

// ==================== request.get 抛出异常 ====================

describe('网络异常测试 - request.get 抛出异常', () => {
  it('userApi.getCurrentUser 在 request.get 抛出异常时应 reject', async () => {
    (mockedStore.getUser as jest.Mock).mockReturnValue(null);
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(false);
    (mockedRequest.get as jest.Mock).mockRejectedValue(new Error('DNS resolution failed'));

    await expect(userApi.getCurrentUser(true))
      .rejects.toThrow('DNS resolution failed');

    expect(mockedStore.setUser).not.toHaveBeenCalled();
  });

  it('studyRoomApi.getStudyRooms 在 request.get 抛出异常时应 reject', async () => {
    (mockedStore.getStudyRooms as jest.Mock).mockReturnValue([]);
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(false);
    (mockedRequest.get as jest.Mock).mockRejectedValue(new Error('Server unreachable'));

    await expect(studyRoomApi.getStudyRooms(undefined, true))
      .rejects.toThrow('Server unreachable');

    expect(mockedStore.setStudyRooms).not.toHaveBeenCalled();
  });

  it('reservationApi.getMyReservations 在 request.get 抛出异常时应 reject', async () => {
    (mockedStore.getMyReservations as jest.Mock).mockReturnValue([]);
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(false);
    (mockedRequest.get as jest.Mock).mockRejectedValue(new Error('Network timeout'));

    await expect(reservationApi.getMyReservations(undefined, true))
      .rejects.toThrow('Network timeout');
  });
});

// ==================== mockManager.request 抛出异常 ====================

describe('网络异常测试 - mockManager.request 抛出异常', () => {
  it('reservationApi.createReservation 在 mockManager.request 抛出异常时应 reject', async () => {
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(true);
    (mockedMock.request as jest.Mock).mockRejectedValue(new Error('Mock internal error'));

    await expect(reservationApi.createReservation({
      studyRoomId: 'room1', seatId: 'seat1',
      startTime: '2024-01-01T09:00:00Z', endTime: '2024-01-01T12:00:00Z'
    })).rejects.toThrow('Mock internal error');

    expect(mockedStore.addReservation).not.toHaveBeenCalled();
  });

  it('userApi.getCurrentUser 在 mockManager.request 抛出异常时应 reject', async () => {
    (mockedStore.getUser as jest.Mock).mockReturnValue(null);
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(true);
    (mockedMock.request as jest.Mock).mockRejectedValue(new Error('Mock crash'));

    await expect(userApi.getCurrentUser(true))
      .rejects.toThrow('Mock crash');

    expect(mockedStore.setUser).not.toHaveBeenCalled();
  });
});

// ==================== wx.login 失败 ====================

describe('网络异常测试 - wx.login 失败', () => {
  it('wx.login 调用 fail 回调时返回错误响应', async () => {
    (wx.login as jest.Mock).mockImplementation(({ fail }: any) => {
      fail({ errMsg: 'login:fail -1' });
    });

    const result = await authApi.loginWithWx();

    expect(result.code).toBe(10001);
    expect(result.message).toBe('微信登录失败');
    expect(result.data).toBeNull();
    expect(mockedStore.setUser).not.toHaveBeenCalled();
  });

  it('wx.login 成功但未返回 code 时返回错误响应', async () => {
    (wx.login as jest.Mock).mockImplementation(({ success }: any) => {
      success({ code: '' });
    });

    const result = await authApi.loginWithWx();

    expect(result.code).toBe(10001);
    expect(result.message).toBe('微信登录失败');
    expect(mockedStore.setUser).not.toHaveBeenCalled();
  });
});

// ==================== wx.uploadFile 失败 ====================

describe('网络异常测试 - wx.uploadFile 失败', () => {
  it('wx.uploadFile 调用 fail 回调时返回上传失败响应', async () => {
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(false);
    (wx.uploadFile as jest.Mock).mockImplementation(({ fail }: any) => {
      fail({ errMsg: 'uploadFile:fail' });
    });

    const result = await userApi.uploadAvatar('/tmp/avatar.png');

    expect(result.code).toBe(500);
    expect(result.message).toBe('上传失败');
    expect(result.data).toBeNull();
    expect(mockedStore.setUser).not.toHaveBeenCalled();
  });

  it('wx.uploadFile 返回无效 JSON 时返回上传失败响应', async () => {
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(false);
    (wx.uploadFile as jest.Mock).mockImplementation(({ success }: any) => {
      success({ data: 'this is not valid json{{{', statusCode: 200 });
    });

    const result = await userApi.uploadAvatar('/tmp/avatar.png');

    expect(result.code).toBe(500);
    expect(result.message).toBe('上传失败');
    expect(result.data).toBeNull();
    expect(mockedStore.setUser).not.toHaveBeenCalled();
  });
});

// ==================== 服务器返回非 200 状态码 ====================

describe('网络异常测试 - 服务器返回非 200 状态码', () => {
  it('服务器返回 401 时 service 不更新 store', async () => {
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(false);
    (mockedRequest.post as jest.Mock).mockResolvedValue({
      code: 401, message: 'unauthorized', data: null, timestamp: Date.now()
    });

    const result = await authApi.wxLogin({ code: 'wx_code' });

    expect(result.code).toBe(401);
    expect(mockedStore.setUser).not.toHaveBeenCalled();
  });

  it('服务器返回 500 时 service 不更新 store 并返回错误信息', async () => {
    (mockedStore.getUser as jest.Mock).mockReturnValue(null);
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(false);
    (mockedRequest.get as jest.Mock).mockResolvedValue({
      code: 500, message: 'Internal Server Error', data: null, timestamp: Date.now()
    });

    const result = await userApi.getCurrentUser(true);

    expect(result.code).toBe(500);
    expect(result.message).toBe('Internal Server Error');
    expect(mockedStore.setUser).not.toHaveBeenCalled();
  });

  it('服务器返回 404 时 reservationApi 不更新 store', async () => {
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(false);
    (mockedRequest.post as jest.Mock).mockResolvedValue({
      code: 404, message: 'Not Found', data: null, timestamp: Date.now()
    });

    const result = await reservationApi.cancelReservation('nonexistent_id');

    expect(result.code).toBe(404);
    expect(mockedStore.removeReservation).not.toHaveBeenCalled();
  });
});

// ==================== 服务器返回 null data ====================

describe('网络异常测试 - 服务器返回 null data', () => {
  it('authApi.wxLogin 返回 code 200 但 data 为 null 时不存储用户', async () => {
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(false);
    (mockedRequest.post as jest.Mock).mockResolvedValue({
      code: 200, message: 'success', data: null, timestamp: Date.now()
    });

    const result = await authApi.wxLogin({ code: 'wx_code' });

    expect(result.code).toBe(200);
    expect(result.data).toBeNull();
    expect(mockedStore.setUser).not.toHaveBeenCalled();
  });

  it('reservationApi.createReservation 返回 code 200 但 data 为 null 时不添加预约', async () => {
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(false);
    (mockedRequest.post as jest.Mock).mockResolvedValue({
      code: 200, message: 'success', data: null, timestamp: Date.now()
    });

    const result = await reservationApi.createReservation({
      studyRoomId: 'room1', seatId: 'seat1',
      startTime: '2024-01-01T09:00:00Z', endTime: '2024-01-01T12:00:00Z'
    });

    expect(result.code).toBe(200);
    expect(result.data).toBeNull();
    expect(mockedStore.addReservation).not.toHaveBeenCalled();
  });

  it('studyRoomApi.getStudyRooms 返回 code 200 但 data.list 为 null 时不更新 store', async () => {
    (mockedStore.getStudyRooms as jest.Mock).mockReturnValue([]);
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(false);
    (mockedRequest.get as jest.Mock).mockResolvedValue({
      code: 200, message: 'success',
      data: { list: null, total: 0, page: 1, pageSize: 0 },
      timestamp: Date.now()
    });

    const result = await studyRoomApi.getStudyRooms(undefined, true);

    expect(result.code).toBe(200);
    expect(mockedStore.setStudyRooms).not.toHaveBeenCalled();
  });
});
