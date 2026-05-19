import type {
  User,
  StudyRoom,
  StudyRoomDetail,
  Seat,
  Reservation,
  CheckInRecord,
  Announcement,
  Rule,
  ReservationRules
} from '../typings/api';

const CACHE_PREFIX = 'istudyspot_';

interface CacheItem<T> {
  data: T;
  expireAt: number;
  updatedAt: number;
}

enum CacheKey {
  USER_INFO = 'user_info',
  STUDY_ROOMS = 'study_rooms',
  STUDY_ROOM_DETAIL = 'study_room_detail',
  SEATS = 'seats',
  MY_RESERVATIONS = 'my_reservations',
  CURRENT_CHECKIN = 'current_checkin',
  CHECKIN_RECORDS = 'checkin_records',
  ANNOUNCEMENTS = 'announcements',
  RULES = 'rules',
  RESERVATION_RULES = 'reservation_rules'
}

enum CacheExpireTime {
  USER_INFO = 24 * 60 * 60 * 1000,
  STUDY_ROOMS = 30 * 60 * 1000,
  STUDY_ROOM_DETAIL = 30 * 60 * 1000,
  SEATS = 5 * 60 * 1000,
  MY_RESERVATIONS = 5 * 60 * 1000,
  CURRENT_CHECKIN = 1 * 60 * 1000,
  CHECKIN_RECORDS = 10 * 60 * 1000,
  ANNOUNCEMENTS = 10 * 60 * 1000,
  RULES = 60 * 60 * 1000,
  RESERVATION_RULES = 60 * 60 * 1000
}

class CacheService {
  private getKey(key: string): string {
    return `${CACHE_PREFIX}${key}`;
  }

  private getFullKey(key: CacheKey, suffix?: string): string {
    return suffix ? `${CACHE_PREFIX}${key}_${suffix}` : `${CACHE_PREFIX}${key}`;
  }

  set<T>(key: CacheKey, data: T, expireTime: number, suffix?: string): void {
    try {
      const fullKey = this.getFullKey(key, suffix);
      const item: CacheItem<T> = {
        data,
        expireAt: Date.now() + expireTime,
        updatedAt: Date.now()
      };
      wx.setStorageSync(fullKey, JSON.stringify(item));
    } catch (e) {
      console.error(`[Cache] Failed to set ${key}:`, e);
    }
  }

  get<T>(key: CacheKey, suffix?: string): T | null {
    try {
      const fullKey = this.getFullKey(key, suffix);
      const itemStr = wx.getStorageSync(fullKey);

      if (!itemStr) return null;

      const item: CacheItem<T> = JSON.parse(itemStr);

      if (Date.now() > item.expireAt) {
        this.remove(key, suffix);
        return null;
      }

      return item.data;
    } catch (e) {
      console.error(`[Cache] Failed to get ${key}:`, e);
      return null;
    }
  }

  remove(key: CacheKey, suffix?: string): void {
    try {
      const fullKey = this.getFullKey(key, suffix);
      wx.removeStorageSync(fullKey);
    } catch (e) {
      console.error(`[Cache] Failed to remove ${key}:`, e);
    }
  }

  has(key: CacheKey, suffix?: string): boolean {
    return this.get(key, suffix) !== null;
  }

  getUpdatedAt(key: CacheKey, suffix?: string): number | null {
    try {
      const fullKey = this.getFullKey(key, suffix);
      const itemStr = wx.getStorageSync(fullKey);

      if (!itemStr) return null;

      const item: CacheItem<unknown> = JSON.parse(itemStr);
      return item.updatedAt;
    } catch (e) {
      return null;
    }
  }

  clearAll(): void {
    try {
      const res = wx.getStorageInfoSync();
      res.keys.forEach(key => {
        if (key.startsWith(CACHE_PREFIX)) {
          wx.removeStorageSync(key);
        }
      });
    } catch (e) {
      console.error('[Cache] Failed to clear all:', e);
    }
  }

  clearUserData(): void {
    this.remove(CacheKey.USER_INFO);
    this.remove(CacheKey.MY_RESERVATIONS);
    this.remove(CacheKey.CURRENT_CHECKIN);
    this.remove(CacheKey.CHECKIN_RECORDS);
  }

  setUser(user: User): void {
    this.set(CacheKey.USER_INFO, user, CacheExpireTime.USER_INFO);
  }

  getUser(): User | null {
    return this.get<User>(CacheKey.USER_INFO);
  }

  setStudyRooms(rooms: StudyRoom[]): void {
    this.set(CacheKey.STUDY_ROOMS, rooms, CacheExpireTime.STUDY_ROOMS);
  }

  getStudyRooms(): StudyRoom[] | null {
    return this.get<StudyRoom[]>(CacheKey.STUDY_ROOMS);
  }

  setStudyRoomDetail(roomId: string, room: StudyRoomDetail): void {
    this.set(CacheKey.STUDY_ROOM_DETAIL, room, CacheExpireTime.STUDY_ROOM_DETAIL, roomId);
  }

  getStudyRoomDetail(roomId: string): StudyRoomDetail | null {
    return this.get<StudyRoomDetail>(CacheKey.STUDY_ROOM_DETAIL, roomId);
  }

  setSeats(studyRoomId: string, seats: Seat[]): void {
    this.set(CacheKey.SEATS, seats, CacheExpireTime.SEATS, studyRoomId);
  }

  getSeats(studyRoomId: string): Seat[] | null {
    return this.get<Seat[]>(CacheKey.SEATS, studyRoomId);
  }

  setMyReservations(reservations: Reservation[]): void {
    this.set(CacheKey.MY_RESERVATIONS, reservations, CacheExpireTime.MY_RESERVATIONS);
  }

  getMyReservations(): Reservation[] | null {
    return this.get<Reservation[]>(CacheKey.MY_RESERVATIONS);
  }

  setCurrentCheckIn(status: { isCheckedIn: boolean; checkInRecord: CheckInRecord | null }): void {
    this.set(CacheKey.CURRENT_CHECKIN, status, CacheExpireTime.CURRENT_CHECKIN);
  }

  getCurrentCheckIn(): { isCheckedIn: boolean; checkInRecord: CheckInRecord | null } | null {
    return this.get<{ isCheckedIn: boolean; checkInRecord: CheckInRecord | null }>(CacheKey.CURRENT_CHECKIN);
  }

  setCheckInRecords(records: CheckInRecord[]): void {
    this.set(CacheKey.CHECKIN_RECORDS, records, CacheExpireTime.CHECKIN_RECORDS);
  }

  getCheckInRecords(): CheckInRecord[] | null {
    return this.get<CheckInRecord[]>(CacheKey.CHECKIN_RECORDS);
  }

  setAnnouncements(announcements: Announcement[]): void {
    this.set(CacheKey.ANNOUNCEMENTS, announcements, CacheExpireTime.ANNOUNCEMENTS);
  }

  getAnnouncements(): Announcement[] | null {
    return this.get<Announcement[]>(CacheKey.ANNOUNCEMENTS);
  }

  setRules(rules: Rule[]): void {
    this.set(CacheKey.RULES, rules, CacheExpireTime.RULES);
  }

  getRules(): Rule[] | null {
    return this.get<Rule[]>(CacheKey.RULES);
  }

  setReservationRules(rules: ReservationRules): void {
    this.set(CacheKey.RESERVATION_RULES, rules, CacheExpireTime.RESERVATION_RULES);
  }

  getReservationRules(): ReservationRules | null {
    return this.get<ReservationRules>(CacheKey.RESERVATION_RULES);
  }
}

const cache = new CacheService();

export { CacheKey, CacheExpireTime };
export default cache;
