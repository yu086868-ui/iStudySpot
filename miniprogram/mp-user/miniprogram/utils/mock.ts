import type { ApiResponse } from '../typings/api';
import mockData from './data';

const ENABLE_MOCK = true;

interface MockRequest {
  url: string;
  method: string;
  data?: unknown;
}

class MockManager {
  private enabled: boolean;

  constructor(enabled: boolean) {
    this.enabled = enabled;
  }

  isEnabled(): boolean {
    return this.enabled;
  }

  setEnabled(enabled: boolean): void {
    this.enabled = enabled;
  }

  async request<T = unknown>(config: MockRequest): Promise<ApiResponse<T>> {
    const { url, method, data } = config;

    return new Promise((resolve) => {
      setTimeout(() => {
        const response = this.handleRequest<T>(url, method, data);
        resolve(response);
      }, 300);
    });
  }

  private handleRequest<T = unknown>(url: string, method: string, data?: unknown): ApiResponse<T> {
    const timestamp = Date.now();

    if (url === '/auth/login' && method === 'POST') {
      const params = (data || {}) as { username?: string; password?: string };
      const users = mockData?.users ?? [];
      const user = users.find(u => u.username === params.username);

      if (user) {
        return {
          code: 200,
          message: '登录成功',
          data: {
            token: `mock_token_${user.id}`,
            refreshToken: `mock_refresh_token_${user.id}`,
            user: {
              id: user.id,
              username: user.username,
              nickname: user.nickname,
              avatar: user.avatar
            }
          } as T,
          timestamp
        };
      }

      return {
        code: 10001,
        message: '用户名或密码错误',
        data: null,
        timestamp
      };
    }

    if (url === '/auth/register' && method === 'POST') {
      const params = (data || {}) as { username?: string; password?: string; nickname?: string; phone?: string; studentId?: string };
      const users = mockData?.users ?? [];
      const existingUser = users.find(u => u.username === params.username);

      if (existingUser) {
        return {
          code: 10002,
          message: '用户已存在',
          data: null,
          timestamp
        };
      }

      const newUser = {
        id: `user_${(users?.length ?? 0) + 1}`,
        username: params.username ?? '',
        nickname: params.nickname ?? '',
        avatar: 'https://example.com/default-avatar.jpg',
        phone: params.phone ?? '',
        email: '',
        studentId: params.studentId ?? '',
        creditScore: 100,
        status: 'active' as const,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString()
      };

      if (mockData?.users) {
        mockData.users.push(newUser);
      }

      return {
        code: 200,
        message: '注册成功',
        data: { userId: newUser.id } as T,
        timestamp
      };
    }

    if (url === '/auth/refresh' && method === 'POST') {
      return {
        code: 200,
        message: '刷新成功',
        data: {
          token: 'new_mock_token',
          refreshToken: 'new_mock_refresh_token'
        } as T,
        timestamp
      };
    }

    if (url === '/auth/logout' && method === 'POST') {
      return {
        code: 200,
        message: '登出成功',
        data: null,
        timestamp
      };
    }

    if (url === '/users/me' && method === 'GET') {
      const users = mockData?.users ?? [];
      const currentUser = users[0] ?? null;

      if (!currentUser) {
        return {
          code: 50001,
          message: '用户数据不存在',
          data: null,
          timestamp
        };
      }

      return {
        code: 200,
        message: 'success',
        data: currentUser as T,
        timestamp
      };
    }

    if (url === '/users/me' && method === 'PUT') {
      const params = (data || {}) as { nickname?: string; avatar?: string; phone?: string; email?: string };
      const users = mockData?.users ?? [];
      const user = users[0] ?? null;

      if (!user) {
        return {
          code: 50001,
          message: '用户数据不存在',
          data: null,
          timestamp
        };
      }

      if (params.nickname) user.nickname = params.nickname;
      if (params.avatar) user.avatar = params.avatar;
      if (params.phone) user.phone = params.phone;
      if (params.email) user.email = params.email;

      return {
        code: 200,
        message: '更新成功',
        data: user as T,
        timestamp
      };
    }

    if (url === '/users/me/password' && method === 'PUT') {
      return {
        code: 200,
        message: '密码修改成功',
        data: null,
        timestamp
      };
    }

    if (url === '/studyrooms' && method === 'GET') {
      const params = (data || {}) as { status?: string; floor?: number; keyword?: string; page?: number; pageSize?: number };
      let filteredRooms = [...(mockData?.studyRooms ?? [])];

      if (params.status) {
        filteredRooms = filteredRooms.filter(room => room.status === params.status);
      }

      if (params.floor) {
        filteredRooms = filteredRooms.filter(room => room.floor === params.floor);
      }

      if (params.keyword) {
        filteredRooms = filteredRooms.filter(room =>
          room.name.includes(params.keyword!) || room.description.includes(params.keyword!)
        );
      }

      const page = params.page || 1;
      const pageSize = params.pageSize || 20;
      const start = (page - 1) * pageSize;
      const end = start + pageSize;

      return {
        code: 200,
        message: 'success',
        data: {
          list: filteredRooms.slice(start, end),
          total: filteredRooms.length,
          page,
          pageSize
        } as T,
        timestamp
      };
    }

    if (url.startsWith('/studyrooms/') && method === 'GET') {
      const roomId = url.split('/')[2];
      const studyRooms = mockData?.studyRooms ?? [];
      const room = studyRooms.find(r => r.id === roomId);

      if (room) {
        const rules = mockData?.rules ?? [];
        return {
          code: 200,
          message: 'success',
          data: {
            ...room,
            rules: rules.filter(r => r.studyRoomId === roomId || r.studyRoomId === null),
            createdAt: new Date().toISOString(),
            updatedAt: new Date().toISOString()
          } as T,
          timestamp
        };
      }

      return {
        code: 20001,
        message: '自习室不存在',
        data: null,
        timestamp
      };
    }

    if (url.includes('/seats') && method === 'GET') {
      const studyRoomId = url.split('/')[2];
      const params = (data || {}) as { status?: string; type?: string; row?: number; col?: number };
      const allSeats = mockData?.seats ?? [];
      let seats = allSeats.filter(s => s.studyRoomId === studyRoomId);

      if (params.status) {
        seats = seats.filter(seat => seat.status === params.status);
      }

      if (params.type) {
        seats = seats.filter(seat => seat.type === params.type);
      }

      if (params.row) {
        seats = seats.filter(seat => seat.row === params.row);
      }

      if (params.col) {
        seats = seats.filter(seat => seat.col === params.col);
      }

      return {
        code: 200,
        message: 'success',
        data: seats as T,
        timestamp
      };
    }

    if (url.startsWith('/seats/') && method === 'GET') {
      const seatId = url.split('/')[2];
      const seats = mockData?.seats ?? [];
      const seat = seats.find(s => s.id === seatId);

      if (seat) {
        return {
          code: 200,
          message: 'success',
          data: seat as T,
          timestamp
        };
      }

      return {
        code: 30001,
        message: '座位不存在',
        data: null,
        timestamp
      };
    }

    if (url === '/reservations' && method === 'POST') {
      const params = (data || {}) as { studyRoomId?: string; seatId?: string; startTime?: string; endTime?: string };
      const users = mockData?.users ?? [];
      const currentUser = users[0] ?? null;

      if (!currentUser) {
        return {
          code: 50001,
          message: '用户数据不存在',
          data: null,
          timestamp
        };
      }

      const reservations = mockData?.reservations ?? [];
      const newReservation = {
        id: `res_${reservations.length + 1}`,
        userId: currentUser.id,
        studyRoomId: params.studyRoomId ?? '',
        seatId: params.seatId ?? '',
        startTime: params.startTime ?? '',
        endTime: params.endTime ?? '',
        status: 'confirmed' as const,
        checkInTime: null,
        checkOutTime: null,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString()
      };

      if (mockData?.reservations) {
        mockData.reservations.push(newReservation);
      }

      return {
        code: 200,
        message: '预约成功',
        data: newReservation as T,
        timestamp
      };
    }

    if (url === '/reservations/my' && method === 'GET') {
      const params = (data || {}) as { status?: string; startDate?: string; endDate?: string; page?: number; pageSize?: number };
      const users = mockData?.users ?? [];
      const currentUser = users[0] ?? null;
      const reservations = mockData?.reservations ?? [];

      let filteredReservations = currentUser
        ? reservations.filter(r => r.userId === currentUser.id)
        : [];

      if (params.status) {
        filteredReservations = filteredReservations.filter(r => r.status === params.status);
      }

      if (params.startDate) {
        filteredReservations = filteredReservations.filter(r => r.startTime >= params.startDate!);
      }

      if (params.endDate) {
        filteredReservations = filteredReservations.filter(r => r.endTime <= params.endDate!);
      }

      const page = params.page || 1;
      const pageSize = params.pageSize || 20;
      const start = (page - 1) * pageSize;
      const end = start + pageSize;

      return {
        code: 200,
        message: 'success',
        data: {
          list: filteredReservations.slice(start, end),
          total: filteredReservations.length,
          page,
          pageSize
        } as T,
        timestamp
      };
    }

    if (url.startsWith('/reservations/') && url.endsWith('/cancel') && method === 'POST') {
      const reservationId = url.split('/')[2];
      const reservations = mockData?.reservations ?? [];
      const reservation = reservations.find(r => r.id === reservationId);

      if (reservation) {
        reservation.status = 'cancelled';
        return {
          code: 200,
          message: '预约已取消',
          data: null,
          timestamp
        };
      }

      return {
        code: 40004,
        message: '预约不存在',
        data: null,
        timestamp
      };
    }

    if (url === '/reservations/rules' && method === 'GET') {
      return {
        code: 200,
        message: 'success',
        data: {
          maxAdvanceDays: 7,
          maxDailyReservations: 2,
          maxDurationHours: 4,
          minDurationMinutes: 30,
          cancellationDeadlineMinutes: 15,
          noShowPenalty: 5
        } as T,
        timestamp
      };
    }

    if (url === '/checkin' && method === 'POST') {
      const params = (data || {}) as { reservationId?: string; seatId?: string };
      const users = mockData?.users ?? [];
      const currentUser = users[0] ?? null;
      const reservations = mockData?.reservations ?? [];
      const checkInRecords = mockData?.checkInRecords ?? [];

      if (!currentUser) {
        return {
          code: 50001,
          message: '用户数据不存在',
          data: null,
          timestamp
        };
      }

      const reservation = reservations.find(r => r.id === params.reservationId);
      const newCheckInRecord = {
        id: `checkin_${checkInRecords.length + 1}`,
        userId: currentUser.id,
        reservationId: params.reservationId ?? '',
        studyRoomId: reservation?.studyRoomId ?? '',
        seatId: params.seatId ?? '',
        checkInTime: new Date().toISOString(),
        checkOutTime: null,
        duration: 0,
        status: 'active' as const
      };

      if (mockData?.checkInRecords) {
        mockData.checkInRecords.push(newCheckInRecord);
      }

      return {
        code: 200,
        message: '签到成功',
        data: {
          checkInRecordId: newCheckInRecord.id,
          checkInTime: newCheckInRecord.checkInTime,
          reservationId: params.reservationId ?? '',
          seatId: params.seatId ?? ''
        } as T,
        timestamp
      };
    }

    if (url === '/checkout' && method === 'POST') {
      const params = (data || {}) as { checkInRecordId?: string };
      const checkInRecords = mockData?.checkInRecords ?? [];
      const record = checkInRecords.find(r => r.id === params.checkInRecordId);

      if (record) {
        record.checkOutTime = new Date().toISOString();
        record.duration = Math.floor((new Date(record.checkOutTime).getTime() - new Date(record.checkInTime).getTime()) / 60000);
        record.status = 'completed';

        return {
          code: 200,
          message: '签退成功',
          data: {
            checkOutTime: record.checkOutTime,
            duration: record.duration
          } as T,
          timestamp
        };
      }

      return {
        code: 50003,
        message: '签到记录不存在',
        data: null,
        timestamp
      };
    }

    if (url === '/checkin/records' && method === 'GET') {
      const params = (data || {}) as { startDate?: string; endDate?: string; page?: number; pageSize?: number };
      const users = mockData?.users ?? [];
      const currentUser = users[0] ?? null;
      const checkInRecords = mockData?.checkInRecords ?? [];

      let filteredRecords = currentUser
        ? checkInRecords.filter(r => r.userId === currentUser.id)
        : [];

      if (params.startDate) {
        filteredRecords = filteredRecords.filter(r => r.checkInTime >= params.startDate!);
      }

      if (params.endDate) {
        filteredRecords = filteredRecords.filter(r => r.checkInTime <= params.endDate!);
      }

      const page = params.page || 1;
      const pageSize = params.pageSize || 20;
      const start = (page - 1) * pageSize;
      const end = start + pageSize;

      return {
        code: 200,
        message: 'success',
        data: {
          list: filteredRecords.slice(start, end),
          total: filteredRecords.length,
          page,
          pageSize
        } as T,
        timestamp
      };
    }

    if (url === '/checkin/current' && method === 'GET') {
      const users = mockData?.users ?? [];
      const currentUser = users[0] ?? null;
      const checkInRecords = mockData?.checkInRecords ?? [];
      const activeRecord = currentUser
        ? checkInRecords.find(r => r.userId === currentUser.id && r.status === 'active')
        : undefined;

      return {
        code: 200,
        message: 'success',
        data: {
          isCheckedIn: !!activeRecord,
          checkInRecord: activeRecord ?? null
        } as T,
        timestamp
      };
    }

    if (url === '/announcements' && method === 'GET') {
      const params = (data || {}) as { type?: string; priority?: string; page?: number; pageSize?: number };
      let filteredAnnouncements = [...(mockData?.announcements ?? [])];

      if (params.type) {
        filteredAnnouncements = filteredAnnouncements.filter(a => a.type === params.type);
      }

      if (params.priority) {
        filteredAnnouncements = filteredAnnouncements.filter(a => a.priority === params.priority);
      }

      const page = params.page || 1;
      const pageSize = params.pageSize || 20;
      const start = (page - 1) * pageSize;
      const end = start + pageSize;

      return {
        code: 200,
        message: 'success',
        data: {
          list: filteredAnnouncements.slice(start, end),
          total: filteredAnnouncements.length,
          page,
          pageSize
        } as T,
        timestamp
      };
    }

    if (url.startsWith('/announcements/') && method === 'GET') {
      const announcementId = url.split('/')[2];
      const announcements = mockData?.announcements ?? [];
      const announcement = announcements.find(a => a.id === announcementId);

      if (announcement) {
        return {
          code: 200,
          message: 'success',
          data: announcement as T,
          timestamp
        };
      }

      return {
        code: 60001,
        message: '公告不存在',
        data: null,
        timestamp
      };
    }

    if (url === '/rules' && method === 'GET') {
      const params = (data || {}) as { studyRoomId?: string; category?: string };
      let filteredRules = [...(mockData?.rules ?? [])];

      if (params.studyRoomId) {
        filteredRules = filteredRules.filter(r => r.studyRoomId === params.studyRoomId || r.studyRoomId === null);
      }

      if (params.category) {
        filteredRules = filteredRules.filter(r => r.category === params.category);
      }

      return {
        code: 200,
        message: 'success',
        data: filteredRules as T,
        timestamp
      };
    }

    if (url.startsWith('/rules/') && method === 'GET') {
      const ruleId = url.split('/')[2];
      const rules = mockData?.rules ?? [];
      const rule = rules.find(r => r.id === ruleId);

      if (rule) {
        return {
          code: 200,
          message: 'success',
          data: rule as T,
          timestamp
        };
      }

      return {
        code: 70001,
        message: '规则不存在',
        data: null,
        timestamp
      };
    }

    return {
      code: 404,
      message: '接口未找到',
      data: null,
      timestamp
    };
  }
}

const mockManager = new MockManager(ENABLE_MOCK);

export default mockManager;
