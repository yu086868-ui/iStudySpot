/**
 * 数据转换测试 (Data Transform Testing)
 * 测试数据在不同层之间的转换
 */
jest.mock('../../../miniprogram/utils/request', () => ({
  __esModule: true,
  default: { get: jest.fn(), post: jest.fn(), put: jest.fn(), delete: jest.fn() }
}));
jest.mock('../../../miniprogram/utils/mock', () => ({
  __esModule: true,
  default: { isEnabled: jest.fn(), request: jest.fn() }
}));
jest.mock('../../../miniprogram/utils/store', () => ({
  __esModule: true,
  default: {
    getUser: jest.fn(), setUser: jest.fn(), clearUser: jest.fn(),
    getMyReservations: jest.fn().mockReturnValue([]), setMyReservations: jest.fn(),
    addReservation: jest.fn(), updateReservation: jest.fn(), removeReservation: jest.fn(),
    getCurrentCheckIn: jest.fn().mockReturnValue({ isCheckedIn: false, checkInRecord: null }),
    setCurrentCheckIn: jest.fn(), getCheckInRecords: jest.fn().mockReturnValue([]),
    setCheckInRecords: jest.fn(), getStudyRooms: jest.fn().mockReturnValue([]),
    setStudyRooms: jest.fn(), getStudyRoomDetail: jest.fn().mockReturnValue(null),
    setStudyRoomDetail: jest.fn(), getSeats: jest.fn().mockReturnValue(null),
    setSeats: jest.fn(), getAnnouncements: jest.fn().mockReturnValue([]),
    setAnnouncements: jest.fn(), getRules: jest.fn().mockReturnValue([]),
    setRules: jest.fn(), getReservationRules: jest.fn().mockReturnValue(null),
    setReservationRules: jest.fn(), getCards: jest.fn().mockReturnValue([]),
    setCards: jest.fn(), addCard: jest.fn(), getCardById: jest.fn().mockReturnValue(null)
  }
}));

import { reservationApi } from '../../../miniprogram/services/reservation';
import { checkInApi } from '../../../miniprogram/services/checkin';
import { userApi } from '../../../miniprogram/services/user';
import { SeatLayoutUtil } from '../../../miniprogram/utils/seat-layout';
import { splitByDivider, validateBlock, processContent } from '../../../miniprogram/utils/markdown-contract';
import { rollRarity, rollThemeCategory, generateCard, generateMarkdown, getImageURL } from '../../../miniprogram/utils/mock-data';
import mockManager from '../../../miniprogram/utils/mock';
import store from '../../../miniprogram/utils/store';
import type { Seat, Reservation, Card, CardRarity, ThemeCategory } from '../../../miniprogram/typings/api';

const mockedMock = mockManager as jest.Mocked<typeof mockManager>;
const mockedStore = store as jest.Mocked<typeof store>;

beforeEach(() => {
  jest.clearAllMocks();
});

// ==================== API 响应到 Store 数据的转换 ====================

describe('API 响应数据到 Store 数据的转换', () => {
  it('reservation 的 status 从 API 响应正确映射到 store', async () => {
    const reservation: Reservation = {
      id: 'res1', userId: 'u1', studyRoomId: 'room1', seatId: 's1',
      startTime: '2024-01-01T09:00:00Z', endTime: '2024-01-01T12:00:00Z',
      status: 'confirmed', checkInTime: null, checkOutTime: null,
      createdAt: '2024-01-01T00:00:00Z', updatedAt: '2024-01-01T00:00:00Z'
    };
    const apiResponse = { code: 200, message: 'success', data: reservation, timestamp: Date.now() };

    (mockedMock.isEnabled as jest.Mock).mockReturnValue(true);
    (mockedMock.request as jest.Mock).mockResolvedValue(apiResponse);

    await reservationApi.createReservation({
      studyRoomId: 'room1', seatId: 's1',
      startTime: '2024-01-01T09:00:00Z', endTime: '2024-01-01T12:00:00Z'
    });

    expect(mockedStore.addReservation).toHaveBeenCalledWith(
      expect.objectContaining({ status: 'confirmed' })
    );
  });

  it('签到后 reservation status 从 confirmed 转换为 checked_in', async () => {
    const reservation: Reservation = {
      id: 'res1', userId: 'u1', studyRoomId: 'room1', seatId: 's1',
      startTime: '', endTime: '', status: 'confirmed',
      checkInTime: null, checkOutTime: null, createdAt: '', updatedAt: ''
    };
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(true);
    (mockedStore.getUser as jest.Mock).mockReturnValue({ id: 1, openId: 'o1', nickname: 'Test', avatarUrl: '', status: 'normal', createdAt: '', updatedAt: '' });
    (mockedStore.getMyReservations as jest.Mock).mockReturnValue([reservation]);
    (mockedMock.request as jest.Mock).mockResolvedValue({
      code: 200, message: '签到成功',
      data: { checkInRecordId: 'cr1', checkInTime: '2024-01-01T09:00:00Z', reservationId: 'res1', seatId: 's1' },
      timestamp: Date.now()
    });

    await checkInApi.checkIn({ reservationId: 'res1', seatId: 's1' });

    expect(mockedStore.updateReservation).toHaveBeenCalledWith(
      expect.objectContaining({ status: 'checked_in' })
    );
  });

  it('签退后 reservation status 从 checked_in 转换为 completed', async () => {
    const checkInRecord = {
      id: 'cr1', userId: '1', reservationId: 'res1', studyRoomId: 'room1', seatId: 's1',
      checkInTime: '2024-01-01T09:00:00Z', checkOutTime: null, duration: 0, status: 'active' as const
    };
    const reservation: Reservation = {
      id: 'res1', userId: 'u1', studyRoomId: 'room1', seatId: 's1',
      startTime: '', endTime: '', status: 'checked_in',
      checkInTime: '2024-01-01T09:00:00Z', checkOutTime: null, createdAt: '', updatedAt: ''
    };
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(true);
    (mockedStore.getCurrentCheckIn as jest.Mock).mockReturnValue({ isCheckedIn: true, checkInRecord });
    (mockedStore.getMyReservations as jest.Mock).mockReturnValue([reservation]);
    (mockedMock.request as jest.Mock).mockResolvedValue({
      code: 200, message: '签退成功',
      data: { checkOutTime: '2024-01-01T12:00:00Z', duration: 180 },
      timestamp: Date.now()
    });

    await checkInApi.checkOut({ checkInRecordId: 'cr1' });

    expect(mockedStore.updateReservation).toHaveBeenCalledWith(
      expect.objectContaining({ status: 'completed' })
    );
  });
});

// ==================== 缓存数据到 API 响应格式的转换 ====================

describe('缓存数据到 API 响应格式的转换', () => {
  it('getMyReservations 返回 { list, total } 格式', async () => {
    const reservations: Reservation[] = [
      { id: 'res1', userId: 'u1', studyRoomId: 'room1', seatId: 's1', startTime: '', endTime: '', status: 'confirmed', checkInTime: null, checkOutTime: null, createdAt: '', updatedAt: '' },
      { id: 'res2', userId: 'u1', studyRoomId: 'room1', seatId: 's2', startTime: '', endTime: '', status: 'cancelled', checkInTime: null, checkOutTime: null, createdAt: '', updatedAt: '' }
    ];
    (mockedStore.getMyReservations as jest.Mock).mockReturnValue(reservations);

    const result = await reservationApi.getMyReservations();

    expect(result.data).toHaveProperty('list');
    expect(result.data).toHaveProperty('total');
    expect(result.data.list).toEqual(reservations);
    expect(result.data.total).toBe(2);
  });

  it('getMyReservations 按 status 过滤后返回正确的 { list, total }', async () => {
    const reservations: Reservation[] = [
      { id: 'res1', userId: 'u1', studyRoomId: 'room1', seatId: 's1', startTime: '', endTime: '', status: 'confirmed', checkInTime: null, checkOutTime: null, createdAt: '', updatedAt: '' },
      { id: 'res2', userId: 'u1', studyRoomId: 'room1', seatId: 's2', startTime: '', endTime: '', status: 'cancelled', checkInTime: null, checkOutTime: null, createdAt: '', updatedAt: '' }
    ];
    (mockedStore.getMyReservations as jest.Mock).mockReturnValue(reservations);

    const result = await reservationApi.getMyReservations({ status: 'confirmed' });

    expect(result.data.list).toHaveLength(1);
    expect(result.data.list[0].status).toBe('confirmed');
    expect(result.data.total).toBe(1);
  });

  it('getMyCheckInRecords 返回 { list, total } 格式', async () => {
    const records = [
      { id: 'cr1', userId: '1', reservationId: 'res1', studyRoomId: 'room1', seatId: 's1', checkInTime: '', checkOutTime: '', duration: 180, status: 'completed' as const }
    ];
    (mockedStore.getCheckInRecords as jest.Mock).mockReturnValue(records);

    const result = await checkInApi.getMyCheckInRecords();

    expect(result.data).toHaveProperty('list');
    expect(result.data).toHaveProperty('total');
    expect(result.data.list).toEqual(records);
  });
});

// ==================== 座位布局数据转换 ====================

describe('座位布局数据转换', () => {
  const flatSeats: Seat[] = [
    { id: 's1', studyRoomId: 'room1', row: 1, col: 1, seatNumber: 'A1', type: 'normal', status: 'available', facilities: [], lastUsedAt: '' },
    { id: 's2', studyRoomId: 'room1', row: 1, col: 2, seatNumber: 'A2', type: 'normal', status: 'available', facilities: [], lastUsedAt: '' },
    { id: 's3', studyRoomId: 'room1', row: 2, col: 1, seatNumber: 'B1', type: 'vip', status: 'available', facilities: [], lastUsedAt: '' },
    { id: 's4', studyRoomId: 'room1', row: 2, col: 2, seatNumber: 'B2', type: 'vip', status: 'occupied', facilities: [], lastUsedAt: '' }
  ];

  it('flat array 转换为按行分组的布局', () => {
    const layout = SeatLayoutUtil.createSeatLayout(flatSeats);

    expect(layout.rows).toHaveLength(2);
    expect(layout.rows[0].rowNumber).toBe(1);
    expect(layout.rows[0].seats).toHaveLength(2);
    expect(layout.rows[1].rowNumber).toBe(2);
    expect(layout.rows[1].seats).toHaveLength(2);
  });

  it('布局中 totalRows 和 totalCols 正确', () => {
    const layout = SeatLayoutUtil.createSeatLayout(flatSeats);

    expect(layout.totalRows).toBe(2);
    expect(layout.totalCols).toBe(2);
  });

  it('空数组返回空布局', () => {
    const layout = SeatLayoutUtil.createSeatLayout([]);

    expect(layout.rows).toEqual([]);
    expect(layout.totalRows).toBe(0);
    expect(layout.totalCols).toBe(0);
  });

  it('座位按 col 排序', () => {
    const unsortedSeats: Seat[] = [
      { id: 's1', studyRoomId: 'room1', row: 1, col: 3, seatNumber: 'A3', type: 'normal', status: 'available', facilities: [], lastUsedAt: '' },
      { id: 's2', studyRoomId: 'room1', row: 1, col: 1, seatNumber: 'A1', type: 'normal', status: 'available', facilities: [], lastUsedAt: '' },
      { id: 's3', studyRoomId: 'room1', row: 1, col: 2, seatNumber: 'A2', type: 'normal', status: 'available', facilities: [], lastUsedAt: '' }
    ];
    const layout = SeatLayoutUtil.createSeatLayout(unsortedSeats);

    expect(layout.rows[0].seats.map(s => s.col)).toEqual([1, 2, 3]);
  });

  it('splitIntoGroups 按列范围分组', () => {
    const groupConfig = [
      { startCol: 1, endCol: 1, name: 'left' },
      { startCol: 2, endCol: 2, name: 'right' }
    ];
    const groups = SeatLayoutUtil.splitIntoGroups(flatSeats, groupConfig);

    expect(groups).toHaveLength(2);
    expect(groups[0].name).toBe('left');
    expect(groups[0].rows[0].seats).toHaveLength(1);
    expect(groups[1].name).toBe('right');
    expect(groups[1].rows[0].seats).toHaveLength(1);
  });
});

// ==================== 卡片稀有度和主题生成规则 ====================

describe('卡片稀有度和主题生成规则', () => {
  it('studyDuration < 10 分钟只能生成 N 稀有度', () => {
    // 多次采样验证不会出现高于 N 的稀有度
    for (let i = 0; i < 50; i++) {
      const rarity = rollRarity(5);
      expect(rarity).toBe('N');
    }
  });

  it('studyDuration 10-30 分钟可能生成 N 或 R', () => {
    const rarities = new Set<CardRarity>();
    for (let i = 0; i < 100; i++) {
      rarities.add(rollRarity(20));
    }
    // 在 10-30 分钟区间，可能出现的稀有度为 N, R, SR
    expect(['N', 'R', 'SR']).toEqual(expect.arrayContaining(Array.from(rarities)));
  });

  it('studyDuration >= 240 分钟可能生成 LR', () => {
    const rarities = new Set<CardRarity>();
    for (let i = 0; i < 200; i++) {
      rarities.add(rollRarity(300));
    }
    // 240+ 分钟区间理论上所有稀有度都可能出现
    expect(rarities.size).toBeGreaterThanOrEqual(3);
  });

  it('hidden 主题只有 UR 及以上稀有度才能出现', () => {
    // N 稀有度不应获得 hidden 主题
    for (let i = 0; i < 50; i++) {
      const theme = rollThemeCategory('N');
      expect(theme).not.toBe('hidden');
    }
    // R 稀有度也不应获得 hidden 主题
    for (let i = 0; i < 50; i++) {
      const theme = rollThemeCategory('R');
      expect(theme).not.toBe('hidden');
    }
  });

  it('UR 稀有度可以出现 hidden 主题', () => {
    let foundHidden = false;
    for (let i = 0; i < 200; i++) {
      if (rollThemeCategory('UR') === 'hidden') {
        foundHidden = true;
        break;
      }
    }
    expect(foundHidden).toBe(true);
  });

  it('generateCard 生成完整卡片数据', () => {
    const card = generateCard('1', 60);

    expect(card).toHaveProperty('uuid');
    expect(card).toHaveProperty('userID', '1');
    expect(card).toHaveProperty('studyDuration', 60);
    expect(card).toHaveProperty('rarity');
    expect(card).toHaveProperty('borderTheme');
    expect(card).toHaveProperty('cardTheme');
    expect(card).toHaveProperty('themeCategory');
    expect(card).toHaveProperty('markdown');
    expect(card).toHaveProperty('imageURL');
    expect(['N', 'R', 'SR', 'SSR', 'UR', 'LR']).toContain(card.rarity);
  });

  it('generateCard 的 borderTheme 与 rarity 对应', () => {
    const borderMap: Record<CardRarity, string> = {
      'N': 'white', 'R': 'green', 'SR': 'blue', 'SSR': 'purple', 'UR': 'gold', 'LR': 'red'
    };
    // 通过多次生成验证映射关系
    for (let i = 0; i < 20; i++) {
      const card = generateCard('1', 60);
      expect(card.borderTheme).toBe(borderMap[card.rarity]);
    }
  });
});

// ==================== Markdown 内容分割和验证 ====================

describe('Markdown 内容分割和验证', () => {
  it('splitByDivider 按 --- 分割内容', () => {
    const content = '# Title\n\nSome text\n\n---\n\nMore text\n\n---\n\nFinal text';
    const blocks = splitByDivider(content);

    expect(blocks).toHaveLength(3);
    expect(blocks[0]).toContain('# Title');
    expect(blocks[1]).toContain('More text');
    expect(blocks[2]).toContain('Final text');
  });

  it('splitByDivider 过滤空块', () => {
    const content = '# Title\n\n---\n\n---\n\nFinal text';
    const blocks = splitByDivider(content);

    expect(blocks).toHaveLength(2);
  });

  it('validateBlock 对 h2/h3 标题发出警告', () => {
    const block = '## 二级标题\n\n一些内容';
    const warnings = validateBlock(block);

    expect(warnings.some(w => w.type === 'heading')).toBe(true);
  });

  it('validateBlock 对过长文本发出警告', () => {
    const longText = 'a'.repeat(600);
    const block = `# Title\n\n${longText}`;
    const warnings = validateBlock(block);

    expect(warnings.some(w => w.type === 'length')).toBe(true);
  });

  it('validateBlock 对 HTML 标签发出警告', () => {
    const block = '# Title\n\n<div>HTML content</div>';
    const warnings = validateBlock(block);

    expect(warnings.some(w => w.type === 'html')).toBe(true);
  });

  it('processContent 返回完整的处理结果', () => {
    const content = '# Title\n\nText\n\n---\n\n## Section\n\nMore text';
    const result = processContent(content);

    expect(result).toHaveLength(2);
    expect(result[0]).toHaveProperty('html');
    expect(result[0]).toHaveProperty('index', 1);
    expect(result[0]).toHaveProperty('warnings');
    expect(result[1]).toHaveProperty('index', 2);
  });
});

// ==================== 用户头像 URL 处理 ====================

describe('用户头像 URL 处理', () => {
  it('mock 模式上传头像返回本地路径', async () => {
    const currentUser = {
      id: 1, openId: 'o1', nickname: 'Test', avatarUrl: '/old/avatar.png',
      status: 'normal' as const, createdAt: '', updatedAt: ''
    };
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(true);
    (mockedStore.getUser as jest.Mock).mockReturnValue(currentUser);

    const result = await userApi.uploadAvatar('/tmp/new_avatar.png');

    expect(result.code).toBe(200);
    expect(result.data.avatarUrl).toBe('/tmp/new_avatar.png');
    expect(mockedStore.setUser).toHaveBeenCalledWith(
      expect.objectContaining({ avatarUrl: '/tmp/new_avatar.png' })
    );
  });

  it('mock 模式上传头像后用户 avatarUrl 被更新', async () => {
    const currentUser = {
      id: 1, openId: 'o1', nickname: 'Test', avatarUrl: '/old/avatar.png',
      status: 'normal' as const, createdAt: '', updatedAt: ''
    };
    (mockedMock.isEnabled as jest.Mock).mockReturnValue(true);
    (mockedStore.getUser as jest.Mock).mockReturnValue(currentUser);

    await userApi.uploadAvatar('/tmp/new_avatar.png');

    expect(mockedStore.setUser).toHaveBeenCalledWith(
      expect.objectContaining({
        avatarUrl: '/tmp/new_avatar.png',
        nickname: 'Test'
      })
    );
  });
});
