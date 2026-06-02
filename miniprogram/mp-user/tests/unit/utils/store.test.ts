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

describe('User management', () => {
  it('returns null user and false isLoggedIn initially', () => {
    expect(store.getUser()).toBeNull();
    expect(store.isLoggedIn()).toBe(false);
  });

  it('setUser updates state and isLoggedIn', () => {
    const user = { id: '1', username: 'test', nickname: 'Test' } as any;
    store.setUser(user);
    expect(store.getUser()).toEqual(user);
    expect(store.isLoggedIn()).toBe(true);
    expect(mockedCache.setUser).toHaveBeenCalledWith(user);
  });

  it('clearUser resets user and isLoggedIn', () => {
    const user = { id: '1', username: 'test', nickname: 'Test' } as any;
    store.setUser(user);
    store.clearUser();
    expect(store.getUser()).toBeNull();
    expect(store.isLoggedIn()).toBe(false);
    expect(mockedCache.clearUserData).toHaveBeenCalled();
  });

  it('fires USER_CHANGED on setUser', () => {
    const cb = jest.fn();
    store.on(StoreEvent.USER_CHANGED, cb);
    const user = { id: '1', username: 'test' } as any;
    store.setUser(user);
    expect(cb).toHaveBeenCalledWith(user);
  });

  it('fires USER_CHANGED with null on clearUser', () => {
    const cb = jest.fn();
    store.on(StoreEvent.USER_CHANGED, cb);
    store.clearUser();
    expect(cb).toHaveBeenCalledWith(null);
  });
});

describe('StudyRooms', () => {
  it('returns empty array initially', () => {
    expect(store.getStudyRooms()).toEqual([]);
  });

  it('setStudyRooms updates state', () => {
    const rooms = [{ id: 'r1', name: 'Room 1' }] as any[];
    store.setStudyRooms(rooms);
    expect(store.getStudyRooms()).toEqual(rooms);
    expect(mockedCache.setStudyRooms).toHaveBeenCalledWith(rooms);
  });

  it('fires STUDY_ROOMS_CHANGED on setStudyRooms', () => {
    const cb = jest.fn();
    store.on(StoreEvent.STUDY_ROOMS_CHANGED, cb);
    const rooms = [{ id: 'r1', name: 'Room 1' }] as any[];
    store.setStudyRooms(rooms);
    expect(cb).toHaveBeenCalledWith(rooms);
  });
});

describe('Reservations', () => {
  it('returns empty array initially', () => {
    expect(store.getMyReservations()).toEqual([]);
  });

  it('setMyReservations updates state', () => {
    const reservations = [{ id: 'res1', status: 'confirmed' }] as any[];
    store.setMyReservations(reservations);
    expect(store.getMyReservations()).toEqual(reservations);
    expect(mockedCache.setMyReservations).toHaveBeenCalledWith(reservations);
  });

  it('fires RESERVATIONS_CHANGED on setMyReservations', () => {
    const cb = jest.fn();
    store.on(StoreEvent.RESERVATIONS_CHANGED, cb);
    const reservations = [{ id: 'res1' }] as any[];
    store.setMyReservations(reservations);
    expect(cb).toHaveBeenCalledWith(reservations);
  });

  it('addReservation prepends to list', () => {
    const existing = { id: 'res1', status: 'confirmed' } as any;
    store.setMyReservations([existing]);
    const newRes = { id: 'res2', status: 'confirmed' } as any;
    store.addReservation(newRes);
    expect(store.getMyReservations()).toEqual([newRes, existing]);
    expect(mockedCache.setMyReservations).toHaveBeenCalledWith([newRes, existing]);
  });

  it('addReservation fires RESERVATIONS_CHANGED', () => {
    const cb = jest.fn();
    store.on(StoreEvent.RESERVATIONS_CHANGED, cb);
    const res = { id: 'res1' } as any;
    store.addReservation(res);
    expect(cb).toHaveBeenCalledWith([res]);
  });

  it('updateReservation replaces matching reservation', () => {
    const res1 = { id: 'res1', status: 'confirmed' } as any;
    store.setMyReservations([res1]);
    const updated = { id: 'res1', status: 'cancelled' } as any;
    store.updateReservation(updated);
    expect(store.getMyReservations()).toEqual([updated]);
    expect(mockedCache.setMyReservations).toHaveBeenCalledWith([updated]);
  });

  it('updateReservation does nothing when id not found', () => {
    const res1 = { id: 'res1', status: 'confirmed' } as any;
    store.setMyReservations([res1]);
    const updated = { id: 'res2', status: 'cancelled' } as any;
    store.updateReservation(updated);
    expect(store.getMyReservations()).toEqual([res1]);
  });

  it('updateReservation fires RESERVATIONS_CHANGED when found', () => {
    const res1 = { id: 'res1', status: 'confirmed' } as any;
    store.setMyReservations([res1]);
    const cb = jest.fn();
    store.on(StoreEvent.RESERVATIONS_CHANGED, cb);
    const updated = { id: 'res1', status: 'cancelled' } as any;
    store.updateReservation(updated);
    expect(cb).toHaveBeenCalledWith([updated]);
  });

  it('removeReservation removes by id', () => {
    const res1 = { id: 'res1' } as any;
    const res2 = { id: 'res2' } as any;
    store.setMyReservations([res1, res2]);
    store.removeReservation('res1');
    expect(store.getMyReservations()).toEqual([res2]);
    expect(mockedCache.setMyReservations).toHaveBeenCalledWith([res2]);
  });

  it('removeReservation fires RESERVATIONS_CHANGED', () => {
    const res1 = { id: 'res1' } as any;
    store.setMyReservations([res1]);
    const cb = jest.fn();
    store.on(StoreEvent.RESERVATIONS_CHANGED, cb);
    store.removeReservation('res1');
    expect(cb).toHaveBeenCalledWith([]);
  });
});

describe('CheckIn', () => {
  it('returns default state initially', () => {
    expect(store.getCurrentCheckIn()).toEqual({ isCheckedIn: false, checkInRecord: null });
  });

  it('setCurrentCheckIn updates state', () => {
    const status = { isCheckedIn: true, checkInRecord: { id: 'c1' } as any };
    store.setCurrentCheckIn(status);
    expect(store.getCurrentCheckIn()).toEqual(status);
    expect(mockedCache.setCurrentCheckIn).toHaveBeenCalledWith(status);
  });

  it('fires CHECKIN_CHANGED on setCurrentCheckIn', () => {
    const cb = jest.fn();
    store.on(StoreEvent.CHECKIN_CHANGED, cb);
    const status = { isCheckedIn: true, checkInRecord: null };
    store.setCurrentCheckIn(status);
    expect(cb).toHaveBeenCalledWith(status);
  });
});

describe('CheckInRecords', () => {
  it('returns empty array initially', () => {
    expect(store.getCheckInRecords()).toEqual([]);
  });

  it('setCheckInRecords updates state', () => {
    const records = [{ id: 'cr1' }] as any[];
    store.setCheckInRecords(records);
    expect(store.getCheckInRecords()).toEqual(records);
    expect(mockedCache.setCheckInRecords).toHaveBeenCalledWith(records);
  });
});

describe('Announcements', () => {
  it('returns empty array initially', () => {
    expect(store.getAnnouncements()).toEqual([]);
  });

  it('setAnnouncements updates state', () => {
    const announcements = [{ id: 'a1', title: 'Announcement' }] as any[];
    store.setAnnouncements(announcements);
    expect(store.getAnnouncements()).toEqual(announcements);
    expect(mockedCache.setAnnouncements).toHaveBeenCalledWith(announcements);
  });

  it('fires ANNOUNCEMENTS_CHANGED on setAnnouncements', () => {
    const cb = jest.fn();
    store.on(StoreEvent.ANNOUNCEMENTS_CHANGED, cb);
    const announcements = [{ id: 'a1' }] as any[];
    store.setAnnouncements(announcements);
    expect(cb).toHaveBeenCalledWith(announcements);
  });
});

describe('Rules', () => {
  it('returns empty array initially', () => {
    expect(store.getRules()).toEqual([]);
  });

  it('setRules updates state', () => {
    const rules = [{ id: 'rule1', content: 'No food' }] as any[];
    store.setRules(rules);
    expect(store.getRules()).toEqual(rules);
    expect(mockedCache.setRules).toHaveBeenCalledWith(rules);
  });
});

describe('ReservationRules', () => {
  it('returns null initially', () => {
    expect(store.getReservationRules()).toBeNull();
  });

  it('setReservationRules updates state', () => {
    const rules = { maxHours: 4, minAdvance: 1 } as any;
    store.setReservationRules(rules);
    expect(store.getReservationRules()).toEqual(rules);
    expect(mockedCache.setReservationRules).toHaveBeenCalledWith(rules);
  });
});

describe('Cards', () => {
  it('returns empty array initially', () => {
    expect(store.getCards()).toEqual([]);
  });

  it('setCards updates state', () => {
    const cards = [{ uuid: 'c1', userID: 'u1' }] as any[];
    store.setCards(cards);
    expect(store.getCards()).toEqual(cards);
    expect(mockedCache.setCards).toHaveBeenCalledWith(cards);
  });

  it('fires CARDS_CHANGED on setCards', () => {
    const cb = jest.fn();
    store.on(StoreEvent.CARDS_CHANGED, cb);
    const cards = [{ uuid: 'c1', userID: 'u1' }] as any[];
    store.setCards(cards);
    expect(cb).toHaveBeenCalledWith(cards);
  });

  it('addCard prepends to list', () => {
    const existing = { uuid: 'c1', userID: 'u1' } as any;
    store.setCards([existing]);
    const newCard = { uuid: 'c2', userID: 'u1' } as any;
    store.addCard(newCard);
    expect(store.getCards()).toEqual([newCard, existing]);
    expect(mockedCache.setCards).toHaveBeenCalledWith([newCard, existing]);
  });

  it('addCard fires CARDS_CHANGED', () => {
    const cb = jest.fn();
    store.on(StoreEvent.CARDS_CHANGED, cb);
    const card = { uuid: 'c1' } as any;
    store.addCard(card);
    expect(cb).toHaveBeenCalledWith([card]);
  });

  it('getCardById returns matching card', () => {
    const card1 = { uuid: 'c1', userID: 'u1' } as any;
    const card2 = { uuid: 'c2', userID: 'u1' } as any;
    store.setCards([card1, card2]);
    expect(store.getCardById('c1')).toEqual(card1);
    expect(store.getCardById('c2')).toEqual(card2);
  });

  it('getCardById returns null when not found', () => {
    const card = { uuid: 'c1', userID: 'u1' } as any;
    store.setCards([card]);
    expect(store.getCardById('nonexistent')).toBeNull();
  });
});

describe('clearAll', () => {
  it('resets all state to defaults', () => {
    store.setUser({ id: '1' } as any);
    store.setStudyRooms([{ id: 'r1' }] as any[]);
    store.setMyReservations([{ id: 'res1' }] as any[]);
    store.setCurrentCheckIn({ isCheckedIn: true, checkInRecord: { id: 'c1' } as any });
    store.setCheckInRecords([{ id: 'cr1' }] as any[]);
    store.setAnnouncements([{ id: 'a1' }] as any[]);
    store.setRules([{ id: 'rule1' }] as any[]);
    store.setReservationRules({ maxHours: 4 } as any);
    store.setCards([{ uuid: 'c1' }] as any[]);

    store.clearAll();

    expect(store.getUser()).toBeNull();
    expect(store.isLoggedIn()).toBe(false);
    expect(store.getStudyRooms()).toEqual([]);
    expect(store.getMyReservations()).toEqual([]);
    expect(store.getCurrentCheckIn()).toEqual({ isCheckedIn: false, checkInRecord: null });
    expect(store.getCheckInRecords()).toEqual([]);
    expect(store.getAnnouncements()).toEqual([]);
    expect(store.getRules()).toEqual([]);
    expect(store.getReservationRules()).toBeNull();
    expect(store.getCards()).toEqual([]);
    expect(mockedCache.clearAll).toHaveBeenCalled();
  });
});

describe('Event system', () => {
  it('on returns unsubscribe function', () => {
    const cb = jest.fn();
    const unsub = store.on(StoreEvent.USER_CHANGED, cb);
    expect(typeof unsub).toBe('function');
  });

  it('unsubscribe stops callbacks', () => {
    const cb = jest.fn();
    const unsub = store.on(StoreEvent.USER_CHANGED, cb);
    unsub();
    store.setUser({ id: '1' } as any);
    expect(cb).not.toHaveBeenCalled();
  });

  it('supports multiple listeners on same event', () => {
    const cb1 = jest.fn();
    const cb2 = jest.fn();
    store.on(StoreEvent.USER_CHANGED, cb1);
    store.on(StoreEvent.USER_CHANGED, cb2);
    const user = { id: '1' } as any;
    store.setUser(user);
    expect(cb1).toHaveBeenCalledWith(user);
    expect(cb2).toHaveBeenCalledWith(user);
  });

  it('unsubscribing one listener does not affect others', () => {
    const cb1 = jest.fn();
    const cb2 = jest.fn();
    const unsub1 = store.on(StoreEvent.USER_CHANGED, cb1);
    store.on(StoreEvent.USER_CHANGED, cb2);
    unsub1();
    const user = { id: '1' } as any;
    store.setUser(user);
    expect(cb1).not.toHaveBeenCalled();
    expect(cb2).toHaveBeenCalledWith(user);
  });
});
