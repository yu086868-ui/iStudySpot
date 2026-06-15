import type { ApiResponse, StreamCallbacks } from '../typings/api';
import mockData, { generateCard } from './data';

const ENABLE_MOCK = true;

function parseQueryParam(queryStr: string, key: string): string {
  if (!queryStr) return '';
  const pairs = queryStr.split('&');
  for (let i = 0; i < pairs.length; i++) {
    const kv = pairs[i].split('=');
    if (kv[0] === key) {
      return decodeURIComponent(kv[1] || '');
    }
  }
  return '';
}

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

  requestStream(
    url: string,
    method: string,
    data: unknown,
    callbacks: StreamCallbacks
  ): () => void {
    if (url === '/card/generate/stream' && method === 'POST') {
      var params = (data || {}) as { userID?: string; studyDuration?: number };
      var card = generateCard(String(params.userID || 1), params.studyDuration || 30);

      // 模拟 init 事件
      setTimeout(function () {
        callbacks.onInit({
          type: 'init',
          rarity: card.rarity,
          themeCategory: card.themeCategory,
          borderTheme: card.borderTheme,
          cardTheme: card.cardTheme
        });
      }, 100);

      // 模拟 text 事件：逐字发送
      var markdown = card.markdown;
      var charIndex = 0;
      var textInterval = setInterval(function () {
        if (charIndex < markdown.length) {
          callbacks.onText(markdown[charIndex]);
          charIndex++;
        } else {
          clearInterval(textInterval);
          // 模拟 complete 事件
          if (mockData && mockData.cards) {
            mockData.cards.unshift(card);
          }
          callbacks.onComplete(card);
        }
      }, 50);

      // 返回取消函数
      return function () {
        clearInterval(textInterval);
      };
    }

    // 其他流式接口暂不支持
    callbacks.onError('Mock 流式接口未实现');
    return function () {};
  }

  private handleRequest<T = unknown>(url: string, method: string, data?: unknown): ApiResponse<T> {
    const timestamp = Date.now();

    if (url === '/user/login' && method === 'POST') {
      const users = (mockData && mockData.users) ? mockData.users : [];
      const user = users[0] || null;

      if (user) {
        return {
          code: 200,
          message: '登录成功',
          data: {
            isNewUser: false,
            user: user
          } as T,
          timestamp
        };
      }

      return {
        code: 10001,
        message: '登录失败',
        data: null,
        timestamp
      };
    }

    if (url === '/user/profile' && method === 'GET') {
      const users = (mockData && mockData.users) ? mockData.users : [];
      const currentUser = users[0] || null;

      if (!currentUser) {
        return {
          code: 10002,
          message: '用户不存在',
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

    if (url === '/user/profile' && method === 'PUT') {
      const params = (data || {}) as { nickname?: string };
      const users = (mockData && mockData.users) ? mockData.users : [];
      const user = users[0] || null;

      if (!user) {
        return {
          code: 10002,
          message: '用户不存在',
          data: null,
          timestamp
        };
      }

      if (params.nickname) user.nickname = params.nickname;

      return {
        code: 200,
        message: '更新成功',
        data: null as T,
        timestamp
      };
    }

    if (url === '/user/avatar' && method === 'POST') {
      return {
        code: 200,
        message: '上传成功',
        data: { avatarUrl: '/avatar/avatar_mock.jpg' } as T,
        timestamp
      };
    }

    if (url === '/user/home' && method === 'GET') {
      const users = (mockData && mockData.users) ? mockData.users : [];
      const currentUser = users[0] || null;

      if (!currentUser) {
        return {
          code: 10002,
          message: '用户不存在',
          data: null,
          timestamp
        };
      }

      return {
        code: 200,
        message: 'success',
        data: {
          user: {
            id: currentUser.id,
            nickname: currentUser.nickname,
            avatarUrl: currentUser.avatarUrl
          },
          reservationCount: 12,
          studyHours: 156,
          creditScore: 100
        } as T,
        timestamp
      };
    }

    if (url === '/studyrooms' && method === 'GET') {
      const params = (data || {}) as { status?: string; floor?: number; keyword?: string; page?: number; pageSize?: number };
      let filteredRooms = [...((mockData && mockData.studyRooms) ? mockData.studyRooms : [])];

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

    if (url.includes('/studyrooms/') && url.includes('/seats') && method === 'GET') {
      const parts = url.split('/');
      const studyRoomId = parts[2];
      const params = (data || {}) as { status?: string; type?: string; row?: number; col?: number };
      const allSeats = (mockData && mockData.seats) ? mockData.seats : [];
      
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

    if (url.startsWith('/studyrooms/') && method === 'GET') {
      const roomId = url.split('/')[2];
      const studyRooms = (mockData && mockData.studyRooms) ? mockData.studyRooms : [];
      const room = studyRooms.find(r => r.id === roomId);

      if (room) {
        const rules = (mockData && mockData.rules) ? mockData.rules : [];
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

    if (url.startsWith('/seats/') && method === 'GET') {
      const seatId = url.split('/')[2];
      const seats = (mockData && mockData.seats) ? mockData.seats : [];
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
      const users = (mockData && mockData.users) ? mockData.users : [];
      const currentUser = users[0] || null;
      const reservations = (mockData && mockData.reservations) ? mockData.reservations : [];
      const seats = (mockData && mockData.seats) ? mockData.seats : [];

      if (!currentUser) {
        return {
          code: 50001,
          message: '用户数据不存在',
          data: null,
          timestamp
        };
      }

      const activeReservations = reservations.filter(
        r => r.userId === currentUser.id && 
             (r.status === 'confirmed' || r.status === 'checked_in')
      );
      
      if (activeReservations.length > 0) {
        return {
          code: 40001,
          message: '您已有进行中的预约，请先取消或完成',
          data: null,
          timestamp
        };
      }

      const seat = seats.find(s => s.id === params.seatId);
      if (seat && seat.status !== 'available') {
        return {
          code: 30002,
          message: '该座位已被占用或预约',
          data: null,
          timestamp
        };
      }

      const newReservation = {
        id: `res_${reservations.length + 1}`,
        userId: currentUser.id,
        studyRoomId: params.studyRoomId || '',
        seatId: params.seatId || '',
        startTime: params.startTime || '',
        endTime: params.endTime || '',
        status: 'confirmed' as const,
        checkInTime: null,
        checkOutTime: null,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString()
      };

      if (mockData && mockData.reservations) {
        mockData.reservations.push(newReservation);
      }

      if (seat) {
        seat.status = 'reserved';
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
      const users = (mockData && mockData.users) ? mockData.users : [];
      const currentUser = users[0] || null;
      const reservations = (mockData && mockData.reservations) ? mockData.reservations : [];

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
      const reservations = (mockData && mockData.reservations) ? mockData.reservations : [];
      const seats = (mockData && mockData.seats) ? mockData.seats : [];
      const reservation = reservations.find(r => r.id === reservationId);

      if (reservation) {
        if (reservation.status === 'checked_in') {
          return {
            code: 40007,
            message: '预约已签到，无法取消',
            data: null,
            timestamp
          };
        }

        reservation.status = 'cancelled';
        reservation.updatedAt = new Date().toISOString();

        const seat = seats.find(s => s.id === reservation.seatId);
        if (seat && (seat.status === 'reserved' || seat.status === 'occupied')) {
          seat.status = 'available';
        }

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
      
      const users = (mockData && mockData.users) ? mockData.users : [];
      const currentUser = users[0] || null;
      const reservations = (mockData && mockData.reservations) ? mockData.reservations : [];
      const checkInRecords = (mockData && mockData.checkInRecords) ? mockData.checkInRecords : [];
      const seats = (mockData && mockData.seats) ? mockData.seats : [];

      if (!currentUser) {
        return {
          code: 50001,
          message: '用户数据不存在',
          data: null,
          timestamp
        };
      }

      const activeCheckIn = checkInRecords.find(
        r => r.userId === currentUser.id && r.status === 'active'
      );
      
      if (activeCheckIn) {
        return {
          code: 50002,
          message: '已经签到，无需重复签到',
          data: null,
          timestamp
        };
      }

      const reservation = reservations.find(r => r.id === params.reservationId);
      
      if (reservation) {
        reservation.status = 'checked_in';
        reservation.checkInTime = new Date().toISOString();
        reservation.updatedAt = new Date().toISOString();
      }

      const seat = seats.find(s => s.id === params.seatId);
      if (seat) {
        seat.status = 'occupied';
      }

      const newCheckInRecord = {
        id: `checkin_${checkInRecords.length + 1}`,
        userId: currentUser.id,
        reservationId: params.reservationId || '',
        studyRoomId: reservation && reservation.studyRoomId ? reservation.studyRoomId : '',
        seatId: params.seatId || '',
        checkInTime: new Date().toISOString(),
        checkOutTime: null,
        duration: 0,
        status: 'active' as const
      };

      if (mockData && mockData.checkInRecords) {
        mockData.checkInRecords.push(newCheckInRecord);
      }

      return {
        code: 200,
        message: '签到成功',
        data: {
          checkInRecordId: newCheckInRecord.id,
          checkInTime: newCheckInRecord.checkInTime,
          reservationId: params.reservationId || '',
          seatId: params.seatId || ''
        } as T,
        timestamp
      };
    }

    if (url === '/checkout' && method === 'POST') {
      const params = (data || {}) as { checkInRecordId?: string };
      
      const checkInRecords = (mockData && mockData.checkInRecords) ? mockData.checkInRecords : [];
      const reservations = (mockData && mockData.reservations) ? mockData.reservations : [];
      const seats = (mockData && mockData.seats) ? mockData.seats : [];
      const record = checkInRecords.find(r => r.id === params.checkInRecordId);

      if (record) {
        if (record.status === 'completed') {
          return {
            code: 50004,
            message: '已经签退',
            data: null,
            timestamp
          };
        }

        record.checkOutTime = new Date().toISOString();
        record.duration = Math.floor((new Date(record.checkOutTime).getTime() - new Date(record.checkInTime).getTime()) / 60000);
        record.status = 'completed';

        const reservation = reservations.find(r => r.id === record.reservationId);
        if (reservation) {
          reservation.status = 'completed';
          reservation.checkOutTime = record.checkOutTime;
          reservation.updatedAt = new Date().toISOString();
        }

        const seat = seats.find(s => s.id === record.seatId);
        if (seat) {
          seat.status = 'available';
        }

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
      const users = (mockData && mockData.users) ? mockData.users : [];
      const currentUser = users[0] || null;
      const checkInRecords = (mockData && mockData.checkInRecords) ? mockData.checkInRecords : [];

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
      const users = (mockData && mockData.users) ? mockData.users : [];
      const currentUser = users[0] || null;
      const checkInRecords = (mockData && mockData.checkInRecords) ? mockData.checkInRecords : [];
      const activeRecord = currentUser
        ? checkInRecords.find(r => r.userId === currentUser.id && r.status === 'active')
        : undefined;

      return {
        code: 200,
        message: 'success',
        data: {
          isCheckedIn: !!activeRecord,
          checkInRecord: activeRecord || null
        } as T,
        timestamp
      };
    }

    if (url === '/announcements' && method === 'GET') {
      const params = (data || {}) as { type?: string; priority?: string; page?: number; pageSize?: number };
      let filteredAnnouncements = [...((mockData && mockData.announcements) ? mockData.announcements : [])];

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
      const announcements = (mockData && mockData.announcements) ? mockData.announcements : [];
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
      let filteredRules = [...((mockData && mockData.rules) ? mockData.rules : [])];

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
      const rules = (mockData && mockData.rules) ? mockData.rules : [];
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

    // ==================== 卡片系统路由 ====================

    if (url === '/card/generate' && method === 'POST') {
      const params = (data || {}) as { userID?: string; studyDuration?: number };
      const users = (mockData && mockData.users) ? mockData.users : [];

      if (!params.userID) {
        return {
          code: 80001,
          message: '参数错误：缺少userID',
          data: null,
          timestamp
        };
      }

      if (!params.studyDuration || params.studyDuration <= 0) {
        return {
          code: 80001,
          message: '参数错误：studyDuration必须大于0',
          data: null,
          timestamp
        };
      }

      const user = users.find(u => String(u.id) === String(params.userID));
      if (!user) {
        return {
          code: 50001,
          message: '用户数据不存在',
          data: null,
          timestamp
        };
      }

      const card = generateCard(String(params.userID), params.studyDuration);

      if (mockData && mockData.cards) {
        mockData.cards.unshift(card);
      }

      return {
        code: 200,
        message: 'generate success',
        data: card as T,
        timestamp
      };
    }

    if (url.startsWith('/card/detail') && method === 'GET') {
      const queryStr = url.indexOf('?') !== -1 ? url.split('?')[1] : '';
      const idParam = parseQueryParam(queryStr, 'id');
      const cards = (mockData && mockData.cards) ? mockData.cards : [];

      if (!idParam) {
        return {
          code: 80001,
          message: '参数错误：缺少id',
          data: null,
          timestamp
        };
      }

      const card = cards.find(c => c.uuid === idParam);
      if (card) {
        return {
          code: 200,
          message: 'success',
          data: card as T,
          timestamp
        };
      }

      return {
        code: 80002,
        message: '卡片不存在',
        data: null,
        timestamp
      };
    }

    if (url.startsWith('/card/list') && method === 'GET') {
      const params = (data || {}) as { userID?: string };
      const userID = params.userID ? String(params.userID) : '';
      const cards = (mockData && mockData.cards) ? mockData.cards : [];

      let filteredCards = userID
        ? cards.filter(c => String(c.userID) === userID)
        : cards;

      filteredCards = filteredCards.sort((a, b) =>
        new Date(b.createTime).getTime() - new Date(a.createTime).getTime()
      );

      return {
        code: 200,
        message: 'success',
        data: filteredCards as T,
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
