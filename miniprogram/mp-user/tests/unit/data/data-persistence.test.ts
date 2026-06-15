/**
 * 数据持久化测试 (Data Persistence Testing)
 * 测试数据在 store 和 cache 之间的持久化一致性
 */
jest.mock('../../../miniprogram/utils/cache', () => ({
  __esModule: true,
  default: {
    getUser: jest.fn(),
    getStudyRooms: jest.fn(),
    getStudyRoomDetail: jest.fn(),
    getSeats: jest.fn(),
    getMyReservations: jest.fn(),
    getCurrentCheckIn: jest.fn(),
    getCheckInRecords: jest.fn(),
    getAnnouncements: jest.fn(),
    getRules: jest.fn(),
    getReservationRules: jest.fn(),
    getCards: jest.fn(),
    setUser: jest.fn(),
    setStudyRooms: jest.fn(),
    setStudyRoomDetail: jest.fn(),
    setSeats: jest.fn(),
    setMyReservations: jest.fn(),
    setCurrentCheckIn: jest.fn(),
    setCheckInRecords: jest.fn(),
    setAnnouncements: jest.fn(),
    setRules: jest.fn(),
    setReservationRules: jest.fn(),
    setCards: jest.fn(),
    clearUserData: jest.fn(),
    clearAll: jest.fn()
  }
}));

import store, { StoreEvent } from '../../../miniprogram/utils/store';
import cache from '../../../miniprogram/utils/cache';

const mockedCache = cache as jest.Mocked<typeof cache>;

beforeEach(() => {
  store.clearAll();
  jest.clearAllMocks();
});

// ==================== Store 写入后 Cache 同步 ====================

describe('Store 写入数据后 Cache 同步', () => {
  it('setUser 后 cache.setUser 被同步调用', () => {
    const user = { id: 1, openId: 'o1', nickname: 'Test', avatarUrl: '', status: 'normal' as const, createdAt: '', updatedAt: '' };
    store.setUser(user);
    expect(mockedCache.setUser).toHaveBeenCalledWith(user);
  });

  it('setStudyRooms 后 cache.setStudyRooms 被同步调用', () => {
    const rooms = [{ id: 'r1', name: 'Room 1', description: '', location: '', floor: 1, capacity: 50, openTime: '08:00', closeTime: '22:00', facilities: [], image: '', status: 'open' as const }];
    store.setStudyRooms(rooms);
    expect(mockedCache.setStudyRooms).toHaveBeenCalledWith(rooms);
  });

  it('setMyReservations 后 cache.setMyReservations 被同步调用', () => {
    const reservations = [{ id: 'res1', userId: 'u1', studyRoomId: 'room1', seatId: 's1', startTime: '', endTime: '', status: 'confirmed' as const, checkInTime: null, checkOutTime: null, createdAt: '', updatedAt: '' }];
    store.setMyReservations(reservations);
    expect(mockedCache.setMyReservations).toHaveBeenCalledWith(reservations);
  });

  it('setCurrentCheckIn 后 cache.setCurrentCheckIn 被同步调用', () => {
    const status = { isCheckedIn: true, checkInRecord: null };
    store.setCurrentCheckIn(status);
    expect(mockedCache.setCurrentCheckIn).toHaveBeenCalledWith(status);
  });

  it('setCards 后 cache.setCards 被同步调用', () => {
    const cards = [{ uuid: 'c1', userID: 'u1', cardID: 't1', createTime: '', studyDuration: 30, rarity: 'N' as const, borderTheme: 'white', cardTheme: 'normal', themeCategory: 'growth' as const, markdown: '', imageURL: '' }];
    store.setCards(cards);
    expect(mockedCache.setCards).toHaveBeenCalledWith(cards);
  });
});

// ==================== Cache 过期后 Store 处理 ====================

describe('Cache 过期后 Store 处理', () => {
  it('cache 返回 null 时 store 使用内存默认值', () => {
    mockedCache.getUser.mockReturnValue(null);
    mockedCache.getStudyRooms.mockReturnValue(null);
    mockedCache.getMyReservations.mockReturnValue(null);
    mockedCache.getCurrentCheckIn.mockReturnValue(null);
    mockedCache.getCheckInRecords.mockReturnValue(null);
    mockedCache.getCards.mockReturnValue(null);

    // store 在构造时已加载 cache，clearAll 后重新检查
    expect(store.getUser()).toBeNull();
    expect(store.getStudyRooms()).toEqual([]);
    expect(store.getMyReservations()).toEqual([]);
    expect(store.getCurrentCheckIn()).toEqual({ isCheckedIn: false, checkInRecord: null });
    expect(store.getCheckInRecords()).toEqual([]);
    expect(store.getCards()).toEqual([]);
  });

  it('cache 返回有效数据时 store 能正确读取', () => {
    const user = { id: 1, openId: 'o1', nickname: 'Test', avatarUrl: '', status: 'normal' as const, createdAt: '', updatedAt: '' };
    store.setUser(user);

    // store 内部状态已更新
    expect(store.getUser()).toEqual(user);
    expect(store.isLoggedIn()).toBe(true);
  });
});

// ==================== clearUserData 清除用户相关数据 ====================

describe('clearUserData 清除用户相关数据', () => {
  it('clearUser 调用 cache.clearUserData', () => {
    const user = { id: 1, openId: 'o1', nickname: 'Test', avatarUrl: '', status: 'normal' as const, createdAt: '', updatedAt: '' };
    store.setUser(user);
    store.clearUser();

    expect(mockedCache.clearUserData).toHaveBeenCalled();
    expect(store.getUser()).toBeNull();
    expect(store.isLoggedIn()).toBe(false);
  });

  it('clearUser 触发 USER_CHANGED 事件', () => {
    const cb = jest.fn();
    store.on(StoreEvent.USER_CHANGED, cb);
    store.clearUser();
    expect(cb).toHaveBeenCalledWith(null);
  });

  it('clearUser 后重新设置用户可恢复正常', () => {
    const user = { id: 1, openId: 'o1', nickname: 'Test', avatarUrl: '', status: 'normal' as const, createdAt: '', updatedAt: '' };
    store.setUser(user);
    store.clearUser();
    expect(store.getUser()).toBeNull();

    const newUser = { id: 2, openId: 'o2', nickname: 'New', avatarUrl: '', status: 'normal' as const, createdAt: '', updatedAt: '' };
    store.setUser(newUser);
    expect(store.getUser()).toEqual(newUser);
    expect(store.isLoggedIn()).toBe(true);
  });
});

// ==================== clearAll 清除所有数据 ====================

describe('clearAll 清除所有数据', () => {
  it('clearAll 调用 cache.clearAll', () => {
    store.clearAll();
    expect(mockedCache.clearAll).toHaveBeenCalled();
  });

  it('clearAll 重置所有状态为默认值', () => {
    const user = { id: 1, openId: 'o1', nickname: 'Test', avatarUrl: '', status: 'normal' as const, createdAt: '', updatedAt: '' };
    store.setUser(user);
    store.setStudyRooms([{ id: 'r1', name: 'Room', description: '', location: '', floor: 1, capacity: 50, openTime: '08:00', closeTime: '22:00', facilities: [], image: '', status: 'open' as const }]);
    store.setMyReservations([{ id: 'res1', userId: 'u1', studyRoomId: 'room1', seatId: 's1', startTime: '', endTime: '', status: 'confirmed' as const, checkInTime: null, checkOutTime: null, createdAt: '', updatedAt: '' }]);
    store.setCards([{ uuid: 'c1', userID: 'u1', cardID: 't1', createTime: '', studyDuration: 30, rarity: 'N' as const, borderTheme: 'white', cardTheme: 'normal', themeCategory: 'growth' as const, markdown: '', imageURL: '' }]);

    store.clearAll();

    expect(store.getUser()).toBeNull();
    expect(store.isLoggedIn()).toBe(false);
    expect(store.getStudyRooms()).toEqual([]);
    expect(store.getMyReservations()).toEqual([]);
    expect(store.getCards()).toEqual([]);
    expect(store.getCurrentCheckIn()).toEqual({ isCheckedIn: false, checkInRecord: null });
    expect(store.getCheckInRecords()).toEqual([]);
    expect(store.getAnnouncements()).toEqual([]);
    expect(store.getRules()).toEqual([]);
    expect(store.getReservationRules()).toBeNull();
  });
});

// ==================== Store 和 Cache 双向同步 ====================

describe('Store 和 Cache 双向同步', () => {
  it('addReservation 同步更新 cache', () => {
    const res = { id: 'res1', userId: 'u1', studyRoomId: 'room1', seatId: 's1', startTime: '', endTime: '', status: 'confirmed' as const, checkInTime: null, checkOutTime: null, createdAt: '', updatedAt: '' };
    store.addReservation(res);
    expect(mockedCache.setMyReservations).toHaveBeenCalledWith([res]);
  });

  it('updateReservation 同步更新 cache', () => {
    const res = { id: 'res1', userId: 'u1', studyRoomId: 'room1', seatId: 's1', startTime: '', endTime: '', status: 'confirmed' as const, checkInTime: null, checkOutTime: null, createdAt: '', updatedAt: '' };
    store.setMyReservations([res]);
    jest.clearAllMocks();

    const updated = { ...res, status: 'cancelled' as const };
    store.updateReservation(updated);
    expect(mockedCache.setMyReservations).toHaveBeenCalledWith([updated]);
  });

  it('removeReservation 同步更新 cache', () => {
    const res1 = { id: 'res1', userId: 'u1', studyRoomId: 'room1', seatId: 's1', startTime: '', endTime: '', status: 'confirmed' as const, checkInTime: null, checkOutTime: null, createdAt: '', updatedAt: '' };
    const res2 = { id: 'res2', userId: 'u1', studyRoomId: 'room1', seatId: 's2', startTime: '', endTime: '', status: 'confirmed' as const, checkInTime: null, checkOutTime: null, createdAt: '', updatedAt: '' };
    store.setMyReservations([res1, res2]);
    jest.clearAllMocks();

    store.removeReservation('res1');
    expect(mockedCache.setMyReservations).toHaveBeenCalledWith([res2]);
  });

  it('addCard 同步更新 cache', () => {
    const card = { uuid: 'c1', userID: 'u1', cardID: 't1', createTime: '', studyDuration: 30, rarity: 'N' as const, borderTheme: 'white', cardTheme: 'normal', themeCategory: 'growth' as const, markdown: '', imageURL: '' };
    store.addCard(card);
    expect(mockedCache.setCards).toHaveBeenCalledWith([card]);
  });

  it('setAnnouncements 同步更新 cache', () => {
    const announcements = [{ id: 'a1', title: 'Test', content: '', type: 'notice' as const, priority: 'high' as const, publishTime: '', expireTime: null, author: '', status: 'published' as const }];
    store.setAnnouncements(announcements);
    expect(mockedCache.setAnnouncements).toHaveBeenCalledWith(announcements);
  });
});

// ==================== 复合 key 存取一致性 ====================

describe('复合 key 存取一致性', () => {
  it('setStudyRoomDetail 和 getStudyRoomDetail 使用 roomId 作为复合 key', () => {
    const detail = { id: 'room1', name: 'Room 1', description: '', location: '', floor: 1, capacity: 50, openTime: '08:00', closeTime: '22:00', facilities: [], image: '', status: 'open' as const, rules: [], createdAt: '', updatedAt: '' };

    store.setStudyRoomDetail(detail);
    expect(mockedCache.setStudyRoomDetail).toHaveBeenCalledWith('room1', detail);

    // 内存中可读取
    const result = store.getStudyRoomDetail('room1');
    expect(result).toEqual(detail);
  });

  it('getStudyRoomDetail 不同 roomId 返回不同结果', () => {
    const detail1 = { id: 'room1', name: 'Room 1', description: '', location: '', floor: 1, capacity: 50, openTime: '08:00', closeTime: '22:00', facilities: [], image: '', status: 'open' as const, rules: [], createdAt: '', updatedAt: '' };
    store.setStudyRoomDetail(detail1);

    const result = store.getStudyRoomDetail('room2');
    // 内存中只有 room1，room2 会回退到 cache
    expect(mockedCache.getStudyRoomDetail).toHaveBeenCalledWith('room2');
  });

  it('setSeats 和 getSeats 使用 studyRoomId 作为复合 key', () => {
    const seats = [
      { id: 's1', studyRoomId: 'room1', row: 1, col: 1, seatNumber: 'A1', type: 'normal' as const, status: 'available' as const, facilities: [], lastUsedAt: '' }
    ];

    store.setSeats('room1', seats);
    expect(mockedCache.setSeats).toHaveBeenCalledWith('room1', seats);

    const result = store.getSeats('room1');
    expect(result).toEqual(seats);
  });

  it('getSeats 不同 studyRoomId 返回不同结果', () => {
    const seats = [
      { id: 's1', studyRoomId: 'room1', row: 1, col: 1, seatNumber: 'A1', type: 'normal' as const, status: 'available' as const, facilities: [], lastUsedAt: '' }
    ];
    store.setSeats('room1', seats);

    const result = store.getSeats('room2');
    // 内存中 seats 的 studyRoomId 是 room1，room2 不匹配，回退到 cache
    expect(mockedCache.getSeats).toHaveBeenCalledWith('room2');
  });
});
