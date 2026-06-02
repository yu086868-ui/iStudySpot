import cache, { CacheKey, CacheExpireTime } from '../../../miniprogram/utils/cache';

const storage: Record<string, string> = {};

beforeEach(() => {
  Object.keys(storage).forEach(k => delete storage[k]);
  (global.wx.setStorageSync as jest.Mock).mockImplementation((key: string, value: unknown) => {
    storage[key] = typeof value === 'string' ? value : JSON.stringify(value);
  });
  (global.wx.getStorageSync as jest.Mock).mockImplementation((key: string) => {
    return storage[key] || '';
  });
  (global.wx.removeStorageSync as jest.Mock).mockImplementation((key: string) => {
    delete storage[key];
  });
  (global.wx.getStorageInfoSync as jest.Mock).mockImplementation(() => ({
    keys: Object.keys(storage)
  }));
});

describe('CacheService', () => {
  describe('set and get', () => {
    it('stores and retrieves data correctly', () => {
      const data = { name: 'test' };
      cache.set(CacheKey.USER_INFO, data, CacheExpireTime.USER_INFO);
      expect(cache.get(CacheKey.USER_INFO)).toEqual(data);
    });

    it('stores and retrieves array data', () => {
      const data = [{ id: 1 }, { id: 2 }];
      cache.set(CacheKey.STUDY_ROOMS, data, CacheExpireTime.STUDY_ROOMS);
      expect(cache.get(CacheKey.STUDY_ROOMS)).toEqual(data);
    });
  });

  describe('get with expiration', () => {
    it('returns null for expired items', () => {
      const now = Date.now();
      jest.spyOn(Date, 'now').mockReturnValue(now);
      cache.set(CacheKey.USER_INFO, { name: 'test' }, CacheExpireTime.USER_INFO);

      jest.spyOn(Date, 'now').mockReturnValue(now + CacheExpireTime.USER_INFO + 1);
      expect(cache.get(CacheKey.USER_INFO)).toBeNull();
    });

    it('returns data for non-expired items', () => {
      const now = Date.now();
      jest.spyOn(Date, 'now').mockReturnValue(now);
      const data = { name: 'test' };
      cache.set(CacheKey.USER_INFO, data, CacheExpireTime.USER_INFO);

      jest.spyOn(Date, 'now').mockReturnValue(now + CacheExpireTime.USER_INFO - 1);
      expect(cache.get(CacheKey.USER_INFO)).toEqual(data);
    });
  });

  it('get returns null for non-existent keys', () => {
    expect(cache.get(CacheKey.USER_INFO)).toBeNull();
  });

  it('remove deletes stored data', () => {
    cache.set(CacheKey.USER_INFO, { name: 'test' }, CacheExpireTime.USER_INFO);
    expect(cache.get(CacheKey.USER_INFO)).toEqual({ name: 'test' });

    cache.remove(CacheKey.USER_INFO);
    expect(cache.get(CacheKey.USER_INFO)).toBeNull();
  });

  describe('has', () => {
    it('returns true when key exists and is not expired', () => {
      cache.set(CacheKey.USER_INFO, { name: 'test' }, CacheExpireTime.USER_INFO);
      expect(cache.has(CacheKey.USER_INFO)).toBe(true);
    });

    it('returns false when key does not exist', () => {
      expect(cache.has(CacheKey.USER_INFO)).toBe(false);
    });

    it('returns false when key is expired', () => {
      const now = Date.now();
      jest.spyOn(Date, 'now').mockReturnValue(now);
      cache.set(CacheKey.USER_INFO, { name: 'test' }, CacheExpireTime.USER_INFO);

      jest.spyOn(Date, 'now').mockReturnValue(now + CacheExpireTime.USER_INFO + 1);
      expect(cache.has(CacheKey.USER_INFO)).toBe(false);
    });
  });

  describe('getUpdatedAt', () => {
    it('returns updatedAt timestamp', () => {
      const now = 1700000000000;
      jest.spyOn(Date, 'now').mockReturnValue(now);
      cache.set(CacheKey.USER_INFO, { name: 'test' }, CacheExpireTime.USER_INFO);

      expect(cache.getUpdatedAt(CacheKey.USER_INFO)).toBe(now);
    });

    it('returns null for non-existent key', () => {
      expect(cache.getUpdatedAt(CacheKey.USER_INFO)).toBeNull();
    });
  });

  describe('clearAll', () => {
    it('removes only istudyspot_ prefixed keys', () => {
      storage['other_key'] = 'keep';
      storage['istudyspot_user_info'] = JSON.stringify({ data: 'a', expireAt: 1, updatedAt: 1 });
      storage['istudyspot_study_rooms'] = JSON.stringify({ data: 'b', expireAt: 1, updatedAt: 1 });

      cache.clearAll();

      expect(storage['other_key']).toBe('keep');
      expect(storage['istudyspot_user_info']).toBeUndefined();
      expect(storage['istudyspot_study_rooms']).toBeUndefined();
    });
  });

  describe('clearUserData', () => {
    it('removes specific user keys', () => {
      cache.set(CacheKey.USER_INFO, { name: 'test' }, CacheExpireTime.USER_INFO);
      cache.set(CacheKey.MY_RESERVATIONS, [], CacheExpireTime.MY_RESERVATIONS);
      cache.set(CacheKey.CURRENT_CHECKIN, { isCheckedIn: false, checkInRecord: null }, CacheExpireTime.CURRENT_CHECKIN);
      cache.set(CacheKey.CHECKIN_RECORDS, [], CacheExpireTime.CHECKIN_RECORDS);
      cache.set(CacheKey.STUDY_ROOMS, [], CacheExpireTime.STUDY_ROOMS);

      cache.clearUserData();

      expect(cache.get(CacheKey.USER_INFO)).toBeNull();
      expect(cache.get(CacheKey.MY_RESERVATIONS)).toBeNull();
      expect(cache.get(CacheKey.CURRENT_CHECKIN)).toBeNull();
      expect(cache.get(CacheKey.CHECKIN_RECORDS)).toBeNull();
      expect(cache.get(CacheKey.STUDY_ROOMS)).toEqual([]);
    });
  });

  describe('convenience methods', () => {
    it('setUser and getUser', () => {
      const user = { id: '1', name: 'Alice' } as any;
      cache.setUser(user);
      expect(cache.getUser()).toEqual(user);
    });

    it('setStudyRooms and getStudyRooms', () => {
      const rooms = [{ id: '1', name: 'Room A' }] as any;
      cache.setStudyRooms(rooms);
      expect(cache.getStudyRooms()).toEqual(rooms);
    });

    it('setStudyRoomDetail and getStudyRoomDetail', () => {
      const detail = { id: '1', name: 'Room A' } as any;
      cache.setStudyRoomDetail('room1', detail);
      expect(cache.getStudyRoomDetail('room1')).toEqual(detail);
      expect(cache.getStudyRoomDetail('room2')).toBeNull();
    });

    it('setSeats and getSeats', () => {
      const seats = [{ id: '1', name: 'Seat A' }] as any;
      cache.setSeats('room1', seats);
      expect(cache.getSeats('room1')).toEqual(seats);
      expect(cache.getSeats('room2')).toBeNull();
    });

    it('setMyReservations and getMyReservations', () => {
      const reservations = [{ id: '1' }] as any;
      cache.setMyReservations(reservations);
      expect(cache.getMyReservations()).toEqual(reservations);
    });

    it('setCurrentCheckIn and getCurrentCheckIn', () => {
      const status = { isCheckedIn: true, checkInRecord: { id: '1' } as any };
      cache.setCurrentCheckIn(status);
      expect(cache.getCurrentCheckIn()).toEqual(status);
    });

    it('setCheckInRecords and getCheckInRecords', () => {
      const records = [{ id: '1' }] as any;
      cache.setCheckInRecords(records);
      expect(cache.getCheckInRecords()).toEqual(records);
    });

    it('setAnnouncements and getAnnouncements', () => {
      const announcements = [{ id: '1' }] as any;
      cache.setAnnouncements(announcements);
      expect(cache.getAnnouncements()).toEqual(announcements);
    });

    it('setRules and getRules', () => {
      const rules = [{ id: '1' }] as any;
      cache.setRules(rules);
      expect(cache.getRules()).toEqual(rules);
    });

    it('setReservationRules and getReservationRules', () => {
      const rules = { maxHours: 4 } as any;
      cache.setReservationRules(rules);
      expect(cache.getReservationRules()).toEqual(rules);
    });

    it('setCards and getCards', () => {
      const cards = [{ id: '1' }] as any;
      cache.setCards(cards);
      expect(cache.getCards()).toEqual(cards);
    });
  });

  describe('set with suffix', () => {
    it('creates composite key', () => {
      cache.set(CacheKey.STUDY_ROOM_DETAIL, { id: '1' }, CacheExpireTime.STUDY_ROOM_DETAIL, 'room123');
      expect(storage['istudyspot_study_room_detail_room123']).toBeDefined();
      expect(cache.get(CacheKey.STUDY_ROOM_DETAIL, 'room123')).toEqual({ id: '1' });
      expect(cache.get(CacheKey.STUDY_ROOM_DETAIL)).toBeNull();
    });
  });

  describe('error handling', () => {
    it('get returns null on JSON.parse error', () => {
      storage['istudyspot_user_info'] = 'invalid-json{{{';
      expect(cache.get(CacheKey.USER_INFO)).toBeNull();
    });

    it('getUpdatedAt returns null on JSON.parse error', () => {
      storage['istudyspot_user_info'] = 'invalid-json{{{';
      expect(cache.getUpdatedAt(CacheKey.USER_INFO)).toBeNull();
    });
  });
});
