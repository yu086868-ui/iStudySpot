import type { ApiResponse } from '../typings/api';
import { mockData } from './data';

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
      const params = data as { username: string; password: string };
      const user = mockData.users.find(u => u.username === params.username);

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
      const params = data as { username: string; password: string; nickname: string; phone: string; studentId: string };
      const existingUser = mockData.users.find(u => u.username === params.username);

      if (existingUser) {
        return {
          code: 10002,
          message: '用户已存在',
          data: null,
          timestamp
        };
      }

      const newUser = {
        id: `user_${mockData.users.length + 1}`,
        username: params.username,
        nickname: params.nickname,
        avatar: 'https://example.com/default-avatar.jpg',
        phone: params.phone,
        email: '',
        studentId: params.studentId,
        creditScore: 100,
        status: 'active' as const,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString()
      };

      mockData.users.push(newUser);

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
      return {
        code: 200,
        message: 'success',
        data: mockData.users[0] as T,
        timestamp
      };
    }

    if (url === '/users/me' && method === 'PUT') {
      const params = data as { nickname?: string; avatar?: string; phone?: string; email?: string };
      const user = mockData.users[0];

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
      const params = data as { status?: string; floor?: number; keyword?: string; page?: number; pageSize?: number };
      let filteredRooms = [...mockData.studyRooms];

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
      const room = mockData.studyRooms.find(r => r.id === roomId);

      if (room) {
        return {
          code: 200,
          message: 'success',
          data: {
            ...room,
            rules: mockData.rules.filter(r => r.studyRoomId === roomId || r.studyRoomId === null),
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
      const params = data as { status?: string; type?: string; row?: number; col?: number };
      let seats = mockData.seats.filter(s => s.studyRoomId === studyRoomId);

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
      const seat = mockData.seats.find(s => s.id === seatId);

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
      const params = data as { studyRoomId: string; seatId: string; startTime: string; endTime: string };
      const newReservation = {
        id: `res_${mockData.reservations.length + 1}`,
        userId: mockData.users[0].id,
        studyRoomId: params.studyRoomId,
        seatId: params.seatId,
        startTime: params.startTime,
        endTime: params.endTime,
        status: 'confirmed' as const,
        checkInTime: null,
        checkOutTime: null,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString()
      };

      mockData.reservations.push(newReservation);

      return {
        code: 200,
        message: '预约成功',
        data: newReservation as T,
        timestamp
      };
    }

    if (url === '/reservations/my' && method === 'GET') {
      const params = data as { status?: string; startDate?: string; endDate?: string; page?: number; pageSize?: number };
      let filteredReservations = mockData.reservations.filter(r => r.userId === mockData.users[0].id);

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
      const reservation = mockData.reservations.find(r => r.id === reservationId);

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
      const params = data as { reservationId: string; seatId: string };
      const newCheckInRecord = {
        id: `checkin_${mockData.checkInRecords.length + 1}`,
        userId: mockData.users[0].id,
        reservationId: params.reservationId,
        studyRoomId: mockData.reservations.find(r => r.id === params.reservationId)?.studyRoomId || '',
        seatId: params.seatId,
        checkInTime: new Date().toISOString(),
        checkOutTime: null,
        duration: 0,
        status: 'active' as const
      };

      mockData.checkInRecords.push(newCheckInRecord);

      return {
        code: 200,
        message: '签到成功',
        data: {
          checkInRecordId: newCheckInRecord.id,
          checkInTime: newCheckInRecord.checkInTime,
          reservationId: params.reservationId,
          seatId: params.seatId
        } as T,
        timestamp
      };
    }

    if (url === '/checkout' && method === 'POST') {
      const params = data as { checkInRecordId: string };
      const record = mockData.checkInRecords.find(r => r.id === params.checkInRecordId);

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
      const params = data as { startDate?: string; endDate?: string; page?: number; pageSize?: number };
      let filteredRecords = mockData.checkInRecords.filter(r => r.userId === mockData.users[0].id);

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
      const activeRecord = mockData.checkInRecords.find(r => r.userId === mockData.users[0].id && r.status === 'active');

      return {
        code: 200,
        message: 'success',
        data: {
          isCheckedIn: !!activeRecord,
          checkInRecord: activeRecord
        } as T,
        timestamp
      };
    }

    if (url === '/announcements' && method === 'GET') {
      const params = data as { type?: string; priority?: string; page?: number; pageSize?: number };
      let filteredAnnouncements = [...mockData.announcements];

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
      const announcement = mockData.announcements.find(a => a.id === announcementId);

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
      const params = data as { studyRoomId?: string; category?: string };
      let filteredRules = [...mockData.rules];

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
      const rule = mockData.rules.find(r => r.id === ruleId);

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
