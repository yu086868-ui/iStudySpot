import mockManager from '../../../miniprogram/utils/mock';
import mockData from '../../../miniprogram/utils/data';

async function flushRequest<T = unknown>(config: { url: string; method: string; data?: unknown }) {
  const promise = mockManager.request<T>(config);
  jest.advanceTimersByTime(300);
  return promise;
}

describe('MockManager', () => {
  let savedState: string;

  beforeEach(() => {
    savedState = JSON.stringify({
      users: mockData.users,
      reservations: mockData.reservations,
      seats: mockData.seats,
      checkInRecords: mockData.checkInRecords,
      cards: mockData.cards,
    });
    jest.useFakeTimers();
  });

  afterEach(() => {
    const restored = JSON.parse(savedState);
    mockData.users = restored.users;
    mockData.reservations = restored.reservations;
    mockData.seats = restored.seats;
    mockData.checkInRecords = restored.checkInRecords;
    mockData.cards = restored.cards;
    jest.useRealTimers();
  });

  describe('isEnabled / setEnabled', () => {
    it('returns initial enabled state', () => {
      expect(mockManager.isEnabled()).toBe(true);
    });

    it('toggles enabled state', () => {
      mockManager.setEnabled(false);
      expect(mockManager.isEnabled()).toBe(false);
      mockManager.setEnabled(true);
      expect(mockManager.isEnabled()).toBe(true);
    });
  });

  describe('User', () => {
    describe('POST /user/login', () => {
      it('succeeds for existing user', async () => {
        const res = await flushRequest({
          url: '/user/login',
          method: 'POST',
          data: { code: 'mock_wx_code' },
        });
        expect(res.code).toBe(200);
        expect(res.data.isNewUser).toBe(false);
        expect(res.data.user.nickname).toBe('张三');
      });

      it('fails when no user data', async () => {
        const originalUsers = mockData.users;
        mockData.users = [];
        const res = await flushRequest({
          url: '/user/login',
          method: 'POST',
          data: { code: 'mock_wx_code' },
        });
        expect(res.code).toBe(10001);
        expect(res.data).toBeNull();
        mockData.users = originalUsers;
      });
    });

    describe('GET /user/profile', () => {
      it('returns current user profile', async () => {
        const res = await flushRequest({
          url: '/user/profile',
          method: 'GET',
        });
        expect(res.code).toBe(200);
        expect(res.data.id).toBe(1);
        expect(res.data.openId).toBe('mock_open_id_001');
        expect(res.data.nickname).toBe('张三');
        expect(res.data.status).toBe('normal');
      });

      it('fails when user does not exist', async () => {
        const originalUsers = mockData.users;
        mockData.users = [];
        const res = await flushRequest({
          url: '/user/profile',
          method: 'GET',
        });
        expect(res.code).toBe(10002);
        expect(res.data).toBeNull();
        mockData.users = originalUsers;
      });
    });

    describe('PUT /user/profile', () => {
      it('updates nickname successfully', async () => {
        const res = await flushRequest({
          url: '/user/profile',
          method: 'PUT',
          data: { nickname: '新昵称' },
        });
        expect(res.code).toBe(200);
        expect(mockData.users[0].nickname).toBe('新昵称');
      });

      it('fails when user does not exist', async () => {
        const originalUsers = mockData.users;
        mockData.users = [];
        const res = await flushRequest({
          url: '/user/profile',
          method: 'PUT',
          data: { nickname: '测试' },
        });
        expect(res.code).toBe(10002);
        expect(res.data).toBeNull();
        mockData.users = originalUsers;
      });
    });

    describe('POST /user/avatar', () => {
      it('returns avatar url', async () => {
        const res = await flushRequest({
          url: '/user/avatar',
          method: 'POST',
        });
        expect(res.code).toBe(200);
        expect(res.data.avatarUrl).toBeDefined();
      });
    });

    describe('GET /user/home', () => {
      it('returns home data with user info', async () => {
        const res = await flushRequest({
          url: '/user/home',
          method: 'GET',
        });
        expect(res.code).toBe(200);
        expect(res.data.user).toBeDefined();
        expect(res.data.user.nickname).toBe('张三');
        expect(res.data.reservationCount).toBeDefined();
        expect(res.data.studyHours).toBeDefined();
        expect(res.data.creditScore).toBeDefined();
      });

      it('fails when user does not exist', async () => {
        const originalUsers = mockData.users;
        mockData.users = [];
        const res = await flushRequest({
          url: '/user/home',
          method: 'GET',
        });
        expect(res.code).toBe(10002);
        expect(res.data).toBeNull();
        mockData.users = originalUsers;
      });
    });
  });

  describe('Reservations', () => {
    describe('POST /reservations', () => {
      it('creates reservation successfully', async () => {
        const availableSeat = mockData.seats.find(s => s.status === 'available')!;
        const res = await flushRequest({
          url: '/reservations',
          method: 'POST',
          data: {
            studyRoomId: availableSeat.studyRoomId,
            seatId: availableSeat.id,
            startTime: '2026-06-02T10:00:00Z',
            endTime: '2026-06-02T12:00:00Z',
          },
        });
        expect(res.code).toBe(200);
        expect(res.data.status).toBe('confirmed');
        expect(res.data.seatId).toBe(availableSeat.id);
      });

      it('fails when user already has active reservation', async () => {
        const seat1 = mockData.seats.find(s => s.status === 'available')!;
        await flushRequest({
          url: '/reservations',
          method: 'POST',
          data: {
            studyRoomId: seat1.studyRoomId,
            seatId: seat1.id,
            startTime: '2026-06-02T10:00:00Z',
            endTime: '2026-06-02T12:00:00Z',
          },
        });

        const seat2 = mockData.seats.find(s => s.status === 'available')!;
        const res = await flushRequest({
          url: '/reservations',
          method: 'POST',
          data: {
            studyRoomId: seat2.studyRoomId,
            seatId: seat2.id,
            startTime: '2026-06-02T14:00:00Z',
            endTime: '2026-06-02T16:00:00Z',
          },
        });
        expect(res.code).toBe(40001);
      });
    });

    describe('POST /reservations/:id/cancel', () => {
      it('cancels a confirmed reservation', async () => {
        const seat = mockData.seats.find(s => s.status === 'available')!;
        const createRes = await flushRequest({
          url: '/reservations',
          method: 'POST',
          data: {
            studyRoomId: seat.studyRoomId,
            seatId: seat.id,
            startTime: '2026-06-02T10:00:00Z',
            endTime: '2026-06-02T12:00:00Z',
          },
        });

        const res = await flushRequest({
          url: `/reservations/${createRes.data.id}/cancel`,
          method: 'POST',
        });
        expect(res.code).toBe(200);
      });

      it('fails when reservation is checked_in', async () => {
        const seat = mockData.seats.find(s => s.status === 'available')!;
        const createRes = await flushRequest({
          url: '/reservations',
          method: 'POST',
          data: {
            studyRoomId: seat.studyRoomId,
            seatId: seat.id,
            startTime: '2026-06-02T10:00:00Z',
            endTime: '2026-06-02T12:00:00Z',
          },
        });

        await flushRequest({
          url: '/checkin',
          method: 'POST',
          data: { reservationId: createRes.data.id, seatId: seat.id },
        });

        const res = await flushRequest({
          url: `/reservations/${createRes.data.id}/cancel`,
          method: 'POST',
        });
        expect(res.code).toBe(40007);
      });
    });
  });

  describe('Checkin / Checkout', () => {
    async function createReservationAndCheckin() {
      const seat = mockData.seats.find(s => s.status === 'available')!;
      const createRes = await flushRequest({
        url: '/reservations',
        method: 'POST',
        data: {
          studyRoomId: seat.studyRoomId,
          seatId: seat.id,
          startTime: '2026-06-02T10:00:00Z',
          endTime: '2026-06-02T12:00:00Z',
        },
      });
      const checkinRes = await flushRequest({
        url: '/checkin',
        method: 'POST',
        data: { reservationId: createRes.data.id, seatId: seat.id },
      });
      return { createRes, checkinRes, seatId: seat.id };
    }

    describe('POST /checkin', () => {
      it('succeeds', async () => {
        const seat = mockData.seats.find(s => s.status === 'available')!;
        const createRes = await flushRequest({
          url: '/reservations',
          method: 'POST',
          data: {
            studyRoomId: seat.studyRoomId,
            seatId: seat.id,
            startTime: '2026-06-02T10:00:00Z',
            endTime: '2026-06-02T12:00:00Z',
          },
        });

        const res = await flushRequest({
          url: '/checkin',
          method: 'POST',
          data: { reservationId: createRes.data.id, seatId: seat.id },
        });
        expect(res.code).toBe(200);
        expect(res.data.checkInRecordId).toBeDefined();
      });

      it('fails on duplicate checkin', async () => {
        await createReservationAndCheckin();

        const res = await flushRequest({
          url: '/checkin',
          method: 'POST',
          data: { reservationId: 'any', seatId: 'any' },
        });
        expect(res.code).toBe(50002);
      });
    });

    describe('POST /checkout', () => {
      it('succeeds', async () => {
        const { checkinRes } = await createReservationAndCheckin();

        const res = await flushRequest({
          url: '/checkout',
          method: 'POST',
          data: { checkInRecordId: checkinRes.data.checkInRecordId },
        });
        expect(res.code).toBe(200);
        expect(res.data.checkOutTime).toBeDefined();
      });

      it('fails when already completed', async () => {
        const { checkinRes } = await createReservationAndCheckin();

        await flushRequest({
          url: '/checkout',
          method: 'POST',
          data: { checkInRecordId: checkinRes.data.checkInRecordId },
        });

        const res = await flushRequest({
          url: '/checkout',
          method: 'POST',
          data: { checkInRecordId: checkinRes.data.checkInRecordId },
        });
        expect(res.code).toBe(50004);
      });
    });
  });

  describe('Card', () => {
    describe('POST /card/generate', () => {
      it('succeeds with valid params', async () => {
        const res = await flushRequest({
          url: '/card/generate',
          method: 'POST',
          data: { userID: 'user_001', studyDuration: 60 },
        });
        expect(res.code).toBe(200);
        expect(res.data.uuid).toBeDefined();
        expect(res.data.userID).toBe('user_001');
        expect(res.data.studyDuration).toBe(60);
      });

      it('fails with missing userID', async () => {
        const res = await flushRequest({
          url: '/card/generate',
          method: 'POST',
          data: { studyDuration: 60 },
        });
        expect(res.code).toBe(80001);
      });

      it('fails with invalid studyDuration', async () => {
        const res = await flushRequest({
          url: '/card/generate',
          method: 'POST',
          data: { userID: 'user_001', studyDuration: 0 },
        });
        expect(res.code).toBe(80001);
      });
    });

    describe('GET /card/detail', () => {
      it('returns card when found', async () => {
        const card = mockData.cards[0];
        const res = await flushRequest({
          url: `/card/detail?id=${card.uuid}`,
          method: 'GET',
        });
        expect(res.code).toBe(200);
        expect(res.data.uuid).toBe(card.uuid);
      });

      it('fails when card not found', async () => {
        const res = await flushRequest({
          url: '/card/detail?id=nonexistent',
          method: 'GET',
        });
        expect(res.code).toBe(80002);
      });
    });
  });

  describe('Unknown route', () => {
    it('returns 404', async () => {
      const res = await flushRequest({
        url: '/unknown/route',
        method: 'GET',
      });
      expect(res.code).toBe(404);
    });
  });
});
