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
import cache from './cache';

type EventCallback = (data?: unknown) => void;

enum StoreEvent {
  USER_CHANGED = 'user_changed',
  STUDY_ROOMS_CHANGED = 'study_rooms_changed',
  RESERVATIONS_CHANGED = 'reservations_changed',
  CHECKIN_CHANGED = 'checkin_changed',
  ANNOUNCEMENTS_CHANGED = 'announcements_changed'
}

interface StoreState {
  user: User | null;
  isLoggedIn: boolean;
  studyRooms: StudyRoom[];
  currentStudyRoom: StudyRoomDetail | null;
  seats: Seat[];
  myReservations: Reservation[];
  currentCheckIn: { isCheckedIn: boolean; checkInRecord: CheckInRecord | null };
  checkInRecords: CheckInRecord[];
  announcements: Announcement[];
  rules: Rule[];
  reservationRules: ReservationRules | null;
}

class Store {
  private state: StoreState;
  private events: Map<string, Set<EventCallback>>;

  constructor() {
    this.state = {
      user: null,
      isLoggedIn: false,
      studyRooms: [],
      currentStudyRoom: null,
      seats: [],
      myReservations: [],
      currentCheckIn: { isCheckedIn: false, checkInRecord: null },
      checkInRecords: [],
      announcements: [],
      rules: [],
      reservationRules: null
    };
    this.events = new Map();
    this.loadFromCache();
  }

  private loadFromCache(): void {
    const user = cache.getUser();
    if (user) {
      this.state.user = user;
      this.state.isLoggedIn = true;
    }

    const studyRooms = cache.getStudyRooms();
    if (studyRooms) {
      this.state.studyRooms = studyRooms;
    }

    const myReservations = cache.getMyReservations();
    if (myReservations) {
      this.state.myReservations = myReservations;
    }

    const currentCheckIn = cache.getCurrentCheckIn();
    if (currentCheckIn) {
      this.state.currentCheckIn = currentCheckIn;
    }

    const checkInRecords = cache.getCheckInRecords();
    if (checkInRecords) {
      this.state.checkInRecords = checkInRecords;
    }

    const announcements = cache.getAnnouncements();
    if (announcements) {
      this.state.announcements = announcements;
    }

    const rules = cache.getRules();
    if (rules) {
      this.state.rules = rules;
    }

    const reservationRules = cache.getReservationRules();
    if (reservationRules) {
      this.state.reservationRules = reservationRules;
    }
  }

  on(event: StoreEvent, callback: EventCallback): () => void {
    if (!this.events.has(event)) {
      this.events.set(event, new Set());
    }
    this.events.get(event)!.add(callback);

    return () => {
      const callbacks = this.events.get(event);
      if (callbacks) {
        callbacks.delete(callback);
      }
    };
  }

  private emit(event: StoreEvent, data?: unknown): void {
    const callbacks = this.events.get(event);
    if (callbacks) {
      callbacks.forEach(callback => callback(data));
    }
  }

  getUser(): User | null {
    return this.state.user;
  }

  setUser(user: User): void {
    this.state.user = user;
    this.state.isLoggedIn = true;
    cache.setUser(user);
    this.emit(StoreEvent.USER_CHANGED, user);
  }

  isLoggedIn(): boolean {
    return this.state.isLoggedIn;
  }

  clearUser(): void {
    this.state.user = null;
    this.state.isLoggedIn = false;
    cache.clearUserData();
    this.emit(StoreEvent.USER_CHANGED, null);
  }

  getStudyRooms(): StudyRoom[] {
    return this.state.studyRooms;
  }

  setStudyRooms(rooms: StudyRoom[]): void {
    this.state.studyRooms = rooms;
    cache.setStudyRooms(rooms);
    this.emit(StoreEvent.STUDY_ROOMS_CHANGED, rooms);
  }

  getStudyRoomDetail(roomId: string): StudyRoomDetail | null {
    if (this.state.currentStudyRoom && this.state.currentStudyRoom.id === roomId) {
      return this.state.currentStudyRoom;
    }
    return cache.getStudyRoomDetail(roomId);
  }

  setStudyRoomDetail(room: StudyRoomDetail): void {
    this.state.currentStudyRoom = room;
    cache.setStudyRoomDetail(room.id, room);
  }

  getSeats(studyRoomId: string): Seat[] | null {
    if (this.state.seats.length > 0 && this.state.seats[0] && this.state.seats[0].studyRoomId === studyRoomId) {
      return this.state.seats;
    }
    return cache.getSeats(studyRoomId);
  }

  setSeats(studyRoomId: string, seats: Seat[]): void {
    this.state.seats = seats;
    cache.setSeats(studyRoomId, seats);
  }

  getMyReservations(): Reservation[] {
    return this.state.myReservations;
  }

  setMyReservations(reservations: Reservation[]): void {
    this.state.myReservations = reservations;
    cache.setMyReservations(reservations);
    this.emit(StoreEvent.RESERVATIONS_CHANGED, reservations);
  }

  addReservation(reservation: Reservation): void {
    this.state.myReservations.unshift(reservation);
    cache.setMyReservations(this.state.myReservations);
    this.emit(StoreEvent.RESERVATIONS_CHANGED, this.state.myReservations);
  }

  updateReservation(updated: Reservation): void {
    const index = this.state.myReservations.findIndex(r => r.id === updated.id);
    if (index !== -1) {
      this.state.myReservations[index] = updated;
      cache.setMyReservations(this.state.myReservations);
      this.emit(StoreEvent.RESERVATIONS_CHANGED, this.state.myReservations);
    }
  }

  removeReservation(reservationId: string): void {
    this.state.myReservations = this.state.myReservations.filter(r => r.id !== reservationId);
    cache.setMyReservations(this.state.myReservations);
    this.emit(StoreEvent.RESERVATIONS_CHANGED, this.state.myReservations);
  }

  getCurrentCheckIn(): { isCheckedIn: boolean; checkInRecord: CheckInRecord | null } {
    return this.state.currentCheckIn;
  }

  setCurrentCheckIn(status: { isCheckedIn: boolean; checkInRecord: CheckInRecord | null }): void {
    this.state.currentCheckIn = status;
    cache.setCurrentCheckIn(status);
    this.emit(StoreEvent.CHECKIN_CHANGED, status);
  }

  getCheckInRecords(): CheckInRecord[] {
    return this.state.checkInRecords;
  }

  setCheckInRecords(records: CheckInRecord[]): void {
    this.state.checkInRecords = records;
    cache.setCheckInRecords(records);
  }

  getAnnouncements(): Announcement[] {
    return this.state.announcements;
  }

  setAnnouncements(announcements: Announcement[]): void {
    this.state.announcements = announcements;
    cache.setAnnouncements(announcements);
    this.emit(StoreEvent.ANNOUNCEMENTS_CHANGED, announcements);
  }

  getRules(): Rule[] {
    return this.state.rules;
  }

  setRules(rules: Rule[]): void {
    this.state.rules = rules;
    cache.setRules(rules);
  }

  getReservationRules(): ReservationRules | null {
    return this.state.reservationRules;
  }

  setReservationRules(rules: ReservationRules): void {
    this.state.reservationRules = rules;
    cache.setReservationRules(rules);
  }

  clearAll(): void {
    this.state = {
      user: null,
      isLoggedIn: false,
      studyRooms: [],
      currentStudyRoom: null,
      seats: [],
      myReservations: [],
      currentCheckIn: { isCheckedIn: false, checkInRecord: null },
      checkInRecords: [],
      announcements: [],
      rules: [],
      reservationRules: null
    };
    cache.clearAll();
  }
}

const store = new Store();

export { StoreEvent };
export default store;
