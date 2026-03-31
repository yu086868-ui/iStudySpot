export interface ApiResponse<T = unknown> {
  code: number;
  message: string;
  data: T;
  timestamp: number;
}

export interface PaginatedResponse<T> {
  list: T[];
  total: number;
  page: number;
  pageSize: number;
}

export interface User {
  id: string;
  username: string;
  nickname: string;
  avatar: string;
  phone: string;
  email: string;
  studentId: string;
  creditScore: number;
  status: 'active' | 'banned';
  createdAt: string;
  updatedAt: string;
}

export interface StudyRoom {
  id: string;
  name: string;
  description: string;
  location: string;
  floor: number;
  capacity: number;
  openTime: string;
  closeTime: string;
  facilities: string[];
  image: string;
  status: 'open' | 'closed' | 'maintenance';
}

export interface StudyRoomDetail extends StudyRoom {
  rules: Rule[];
  createdAt: string;
  updatedAt: string;
}

export interface Seat {
  id: string;
  studyRoomId: string;
  row: number;
  col: number;
  seatNumber: string;
  type: 'normal' | 'vip' | 'quiet';
  status: 'available' | 'occupied' | 'reserved' | 'maintenance';
  facilities: string[];
  lastUsedAt: string;
}

export interface Reservation {
  id: string;
  userId: string;
  studyRoomId: string;
  seatId: string;
  startTime: string;
  endTime: string;
  status: 'pending' | 'confirmed' | 'checked_in' | 'completed' | 'cancelled' | 'expired';
  checkInTime: string | null;
  checkOutTime: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface CheckInRecord {
  id: string;
  userId: string;
  reservationId: string;
  studyRoomId: string;
  seatId: string;
  checkInTime: string;
  checkOutTime: string | null;
  duration: number;
  status: 'active' | 'completed';
}

export interface Announcement {
  id: string;
  title: string;
  content: string;
  type: 'notice' | 'maintenance' | 'event' | 'emergency';
  priority: 'low' | 'medium' | 'high';
  publishTime: string;
  expireTime: string | null;
  author: string;
  status: 'published' | 'draft' | 'archived';
}

export interface Rule {
  id: string;
  studyRoomId: string | null;
  category: 'booking' | 'usage' | 'penalty' | 'general';
  title: string;
  content: string;
  priority: number;
  createdAt: string;
  updatedAt: string;
}

export interface ReservationRules {
  maxAdvanceDays: number;
  maxDailyReservations: number;
  maxDurationHours: number;
  minDurationMinutes: number;
  cancellationDeadlineMinutes: number;
  noShowPenalty: number;
}

export interface LoginParams {
  username: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  refreshToken: string;
  user: {
    id: string;
    username: string;
    nickname: string;
    avatar: string;
  };
}

export interface RegisterParams {
  username: string;
  password: string;
  nickname: string;
  phone: string;
  studentId: string;
}

export interface UpdateUserParams {
  nickname?: string;
  avatar?: string;
  phone?: string;
  email?: string;
}

export interface ChangePasswordParams {
  oldPassword: string;
  newPassword: string;
}

export interface CreateReservationParams {
  studyRoomId: string;
  seatId: string;
  startTime: string;
  endTime: string;
}

export interface CheckInParams {
  reservationId: string;
  seatId: string;
}

export interface CheckOutParams {
  checkInRecordId: string;
}

export interface StudyRoomListParams {
  status?: 'open' | 'closed';
  floor?: number;
  keyword?: string;
  page?: number;
  pageSize?: number;
}

export interface SeatListParams {
  status?: 'available' | 'occupied' | 'reserved';
  type?: 'normal' | 'vip' | 'quiet';
  row?: number;
  col?: number;
}

export interface ReservationListParams {
  status?: 'pending' | 'confirmed' | 'checked_in' | 'completed' | 'cancelled' | 'expired';
  startDate?: string;
  endDate?: string;
  page?: number;
  pageSize?: number;
}

export interface CheckInRecordListParams {
  startDate?: string;
  endDate?: string;
  page?: number;
  pageSize?: number;
}

export interface AnnouncementListParams {
  type?: 'notice' | 'maintenance' | 'event' | 'emergency';
  priority?: 'low' | 'medium' | 'high';
  page?: number;
  pageSize?: number;
}

export interface RuleListParams {
  studyRoomId?: string;
  category?: 'booking' | 'usage' | 'penalty' | 'general';
}
