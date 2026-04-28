import type { 
  User, 
  Seat, 
  StudyRoom, 
  Reservation, 
  CheckInRecord,
  Announcement,
  Rule,
  LoginResponse,
  ApiResponse,
  PaginatedResponse
} from '../../miniprogram/typings/api';

export class TestDataFactory {
  static createUser(overrides: Partial<User> = {}): User {
    return {
      id: 'user_001',
      username: 'testuser',
      nickname: '测试用户',
      avatar: 'https://example.com/avatar.jpg',
      phone: '13800138000',
      email: 'test@example.com',
      studentId: '2020001',
      creditScore: 100,
      status: 'active',
      createdAt: '2024-01-01T00:00:00.000Z',
      updatedAt: '2024-01-01T00:00:00.000Z',
      ...overrides
    };
  }

  static createSeat(overrides: Partial<Seat> = {}): Seat {
    return {
      id: 'seat_001',
      studyRoomId: 'room_001',
      row: 1,
      col: 1,
      seatNumber: 'A1',
      type: 'normal',
      status: 'available',
      facilities: ['插座', '台灯'],
      lastUsedAt: '2024-01-01T00:00:00.000Z',
      ...overrides
    };
  }

  static createSeats(count: number, overrides: Partial<Seat> = {}): Seat[] {
    const seats: Seat[] = [];
    for (let i = 1; i <= count; i++) {
      const row = Math.ceil(i / 6);
      const col = ((i - 1) % 6) + 1;
      seats.push(this.createSeat({
        id: `seat_${i}`,
        row,
        col,
        seatNumber: `${String.fromCharCode(64 + row)}${col}`,
        ...overrides
      }));
    }
    return seats;
  }

  static createStudyRoom(overrides: Partial<StudyRoom> = {}): StudyRoom {
    return {
      id: 'room_001',
      name: '图书馆自习室',
      description: '安静舒适的学习环境',
      location: '图书馆3楼',
      floor: 3,
      capacity: 100,
      openTime: '08:00',
      closeTime: '22:00',
      facilities: ['WiFi', '空调', '饮水机'],
      image: 'https://example.com/room.jpg',
      status: 'open',
      ...overrides
    };
  }

  static createReservation(overrides: Partial<Reservation> = {}): Reservation {
    const now = new Date();
    const startTime = new Date(now.getTime() + 60 * 60 * 1000);
    const endTime = new Date(now.getTime() + 3 * 60 * 60 * 1000);

    return {
      id: 'res_001',
      userId: 'user_001',
      studyRoomId: 'room_001',
      seatId: 'seat_001',
      startTime: startTime.toISOString(),
      endTime: endTime.toISOString(),
      status: 'confirmed',
      checkInTime: null,
      checkOutTime: null,
      createdAt: now.toISOString(),
      updatedAt: now.toISOString(),
      ...overrides
    };
  }

  static createCheckInRecord(overrides: Partial<CheckInRecord> = {}): CheckInRecord {
    const now = new Date();
    return {
      id: 'checkin_001',
      userId: 'user_001',
      reservationId: 'res_001',
      studyRoomId: 'room_001',
      seatId: 'seat_001',
      checkInTime: now.toISOString(),
      checkOutTime: null,
      duration: 0,
      status: 'active',
      ...overrides
    };
  }

  static createAnnouncement(overrides: Partial<Announcement> = {}): Announcement {
    return {
      id: 'ann_001',
      title: '系统维护通知',
      content: '系统将于今晚进行维护',
      type: 'maintenance',
      priority: 'high',
      publishTime: '2024-01-01T00:00:00.000Z',
      expireTime: null,
      author: '管理员',
      status: 'published',
      ...overrides
    };
  }

  static createRule(overrides: Partial<Rule> = {}): Rule {
    return {
      id: 'rule_001',
      studyRoomId: null,
      category: 'general',
      title: '自习室使用规则',
      content: '请保持安静',
      priority: 1,
      createdAt: '2024-01-01T00:00:00.000Z',
      updatedAt: '2024-01-01T00:00:00.000Z',
      ...overrides
    };
  }

  static createLoginResponse(overrides: Partial<LoginResponse> = {}): LoginResponse {
    return {
      token: 'mock_token_123',
      refreshToken: 'mock_refresh_token_123',
      user: {
        id: 'user_001',
        username: 'testuser',
        nickname: '测试用户',
        avatar: 'https://example.com/avatar.jpg'
      },
      ...overrides
    };
  }

  static createSuccessResponse<T>(data: T, message = 'success'): ApiResponse<T> {
    return {
      code: 200,
      message,
      data,
      timestamp: Date.now()
    };
  }

  static createErrorResponse(code: number, message: string): ApiResponse<null> {
    return {
      code,
      message,
      data: null,
      timestamp: Date.now()
    };
  }

  static createPaginatedResponse<T>(list: T[], total?: number): PaginatedResponse<T> {
    return {
      list,
      total: total || list.length,
      page: 1,
      pageSize: 20
    };
  }
}
