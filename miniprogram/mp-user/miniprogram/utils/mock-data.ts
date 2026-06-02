import type { User, StudyRoom, Seat, Reservation, CheckInRecord, Announcement, Rule, Card, CardRarity, ThemeCategory } from '../typings/api';

export interface MockDataType {
  users: User[];
  studyRooms: StudyRoom[];
  seats: Seat[];
  reservations: Reservation[];
  checkInRecords: CheckInRecord[];
  announcements: Announcement[];
  rules: Rule[];
  cards: Card[];
}

export const mockData: MockDataType = {
  users: [
    {
      id: 'user_001',
      username: 'user001',
      nickname: '张三',
      avatar: 'https://example.com/avatar1.jpg',
      phone: '13800138000',
      email: 'zhangsan@example.com',
      studentId: '20240001',
      creditScore: 100,
      status: 'active' as const,
      createdAt: '2024-01-01T00:00:00Z',
      updatedAt: '2024-01-01T00:00:00Z'
    },
    {
      id: 'user_002',
      username: 'user002',
      nickname: '李四',
      avatar: 'https://example.com/avatar2.jpg',
      phone: '13800138001',
      email: 'lisi@example.com',
      studentId: '20240002',
      creditScore: 95,
      status: 'active' as const,
      createdAt: '2024-01-02T00:00:00Z',
      updatedAt: '2024-01-02T00:00:00Z'
    }
  ],

  studyRooms: [
    {
      id: 'room_001',
      name: '图书馆一楼自习室',
      description: '安静舒适的学习环境，配备现代化设施',
      location: '图书馆一楼东侧',
      floor: 1,
      capacity: 50,
      openTime: '08:00',
      closeTime: '22:00',
      facilities: ['WiFi', '空调', '插座', '台灯'],
      image: 'https://example.com/room1.jpg',
      status: 'open' as const
    },
    {
      id: 'room_002',
      name: '图书馆二楼自习室',
      description: '宽敞明亮，适合小组讨论',
      location: '图书馆二楼西侧',
      floor: 2,
      capacity: 40,
      openTime: '08:00',
      closeTime: '22:00',
      facilities: ['WiFi', '空调', '投影仪', '白板'],
      image: 'https://example.com/room2.jpg',
      status: 'open' as const
    },
    {
      id: 'room_003',
      name: '图书馆三楼静音自习室',
      description: '绝对安静，专注学习',
      location: '图书馆三楼北侧',
      floor: 3,
      capacity: 30,
      openTime: '08:00',
      closeTime: '22:00',
      facilities: ['WiFi', '空调', '插座', '静音舱'],
      image: 'https://example.com/room3.jpg',
      status: 'open' as const
    },
    {
      id: 'room_004',
      name: '图书馆四楼VIP自习室',
      description: '高端配置，尊享体验',
      location: '图书馆四楼南侧',
      floor: 4,
      capacity: 20,
      openTime: '09:00',
      closeTime: '21:00',
      facilities: ['WiFi', '空调', '独立电源', '人体工学椅', '书架'],
      image: 'https://example.com/room4.jpg',
      status: 'maintenance' as const
    }
  ],

  seats: [] as Seat[],
  reservations: [] as Reservation[],
  checkInRecords: [] as CheckInRecord[],
  announcements: [
    {
      id: 'ann_001',
      title: '自习室开放时间调整通知',
      content: '从即日起，图书馆一楼自习室开放时间调整为08:00-22:00，请同学们合理安排学习时间。',
      type: 'notice' as const,
      priority: 'high' as const,
      publishTime: '2024-03-25T00:00:00Z',
      expireTime: '2024-04-30T23:59:59Z',
      author: '管理员',
      status: 'published' as const
    },
    {
      id: 'ann_002',
      title: '系统维护通知',
      content: '系统将于2024年4月1日凌晨2:00-4:00进行维护，届时将无法预约座位，请提前做好安排。',
      type: 'maintenance' as const,
      priority: 'medium' as const,
      publishTime: '2024-03-28T00:00:00Z',
      expireTime: '2024-04-02T23:59:59Z',
      author: '技术部',
      status: 'published' as const
    },
    {
      id: 'ann_003',
      title: '读书分享会活动',
      content: '本周六下午2点，图书馆三楼将举办读书分享会，欢迎各位同学参加！',
      type: 'event' as const,
      priority: 'low' as const,
      publishTime: '2024-03-29T00:00:00Z',
      expireTime: '2024-04-06T23:59:59Z',
      author: '活动部',
      status: 'published' as const
    },
    {
      id: 'ann_004',
      title: '紧急通知：空调故障',
      content: '图书馆二楼自习室空调出现故障，正在紧急维修中，预计今日下午恢复，请同学们谅解。',
      type: 'emergency' as const,
      priority: 'high' as const,
      publishTime: '2024-03-30T10:00:00Z',
      expireTime: '2024-03-30T23:59:59Z',
      author: '管理员',
      status: 'published' as const
    }
  ] as Announcement[],
  rules: [
    {
      id: 'rule_001',
      studyRoomId: null,
      category: 'booking' as const,
      title: '预约规则',
      content: '1. 每人每天最多预约2次\n2. 每次预约时长不超过4小时\n3. 需提前15分钟签到\n4. 超时未签到将扣除5分信用分',
      priority: 1,
      createdAt: '2024-01-01T00:00:00Z',
      updatedAt: '2024-01-01T00:00:00Z'
    },
    {
      id: 'rule_002',
      studyRoomId: null,
      category: 'usage' as const,
      title: '使用规则',
      content: '1. 保持安静，不大声喧哗\n2. 爱护公共设施\n3. 离开时带走个人物品\n4. 禁止占座',
      priority: 2,
      createdAt: '2024-01-01T00:00:00Z',
      updatedAt: '2024-01-01T00:00:00Z'
    },
    {
      id: 'rule_003',
      studyRoomId: null,
      category: 'penalty' as const,
      title: '违规处罚',
      content: '1. 违规占座：扣10分\n2. 损坏设施：按原价赔偿并扣20分\n3. 扰乱秩序：警告并扣15分\n4. 信用分低于60分将暂停预约权限',
      priority: 3,
      createdAt: '2024-01-01T00:00:00Z',
      updatedAt: '2024-01-01T00:00:00Z'
    },
    {
      id: 'rule_004',
      studyRoomId: 'room_003',
      category: 'general' as const,
      title: '静音自习室特别规定',
      content: '1. 严禁发出任何噪音\n2. 手机需调至静音\n3. 如需交流请到讨论区\n4. 违反者将被请离自习室',
      priority: 1,
      createdAt: '2024-01-01T00:00:00Z',
      updatedAt: '2024-01-01T00:00:00Z'
    }
  ] as Rule[],
  cards: [] as Card[]
};

const generateSeats = (): void => {
  mockData.studyRooms.forEach(room => {
    const rows = Math.ceil(room.capacity / 10);
    const cols = 10;
    
    for (let row = 1; row <= rows; row++) {
      for (let col = 1; col <= cols; col++) {
        const seatId = `seat_${room.id}_${row}_${col}`;
        const seatNumber = `${String.fromCharCode(64 + row)}${col}`;
        
        let seatType: 'normal' | 'vip' | 'quiet' = 'normal';
        if (col <= 2) {
          seatType = 'vip';
        } else if (col <= 5) {
          seatType = 'quiet';
        }
        
        let status: 'available' | 'occupied' | 'reserved' | 'maintenance' = 'available';
        const rand = Math.random();
        if (rand > 0.7) {
          status = 'occupied';
        } else if (rand > 0.5) {
          status = 'reserved';
        } else if (rand > 0.95) {
          status = 'maintenance';
        }
        
        let facilities: string[] = ['插座'];
        if (seatType === 'vip') {
          facilities = ['插座', '台灯', '人体工学椅', '书架', '独立电源'];
        } else if (seatType === 'quiet') {
          facilities = ['插座', '台灯', '隔板'];
        }
        
        mockData.seats.push({
          id: seatId,
          studyRoomId: room.id,
          row,
          col,
          seatNumber,
          type: seatType,
          status,
          facilities,
          lastUsedAt: new Date(Date.now() - Math.random() * 86400000 * 7).toISOString()
        });
      }
    }
  });
};

const generateReservations = (): void => {
  const now = new Date();

  for (let i = 0; i < 5; i++) {
    const startTime = new Date(now.getTime() - (i + 2) * 24 * 60 * 60 * 1000);
    const endTime = new Date(startTime.getTime() + 3 * 60 * 60 * 1000);
    
    mockData.reservations.push({
      id: `res_completed_${i}`,
      userId: mockData.users[0].id,
      studyRoomId: mockData.studyRooms[i % mockData.studyRooms.length].id,
      seatId: `seat_${mockData.studyRooms[i % mockData.studyRooms.length].id}_${i + 1}_${i + 1}`,
      startTime: startTime.toISOString(),
      endTime: endTime.toISOString(),
      status: 'completed' as const,
      checkInTime: startTime.toISOString(),
      checkOutTime: endTime.toISOString(),
      createdAt: new Date(startTime.getTime() - 60 * 60 * 1000).toISOString(),
      updatedAt: endTime.toISOString()
    });
  }

  for (let i = 0; i < 3; i++) {
    const startTime = new Date(now.getTime() - (i + 7) * 24 * 60 * 60 * 1000);
    const endTime = new Date(startTime.getTime() + 3 * 60 * 60 * 1000);
    
    mockData.reservations.push({
      id: `res_cancelled_${i}`,
      userId: mockData.users[0].id,
      studyRoomId: mockData.studyRooms[i % mockData.studyRooms.length].id,
      seatId: `seat_${mockData.studyRooms[i % mockData.studyRooms.length].id}_${i + 1}_${i + 2}`,
      startTime: startTime.toISOString(),
      endTime: endTime.toISOString(),
      status: 'cancelled' as const,
      checkInTime: null,
      checkOutTime: null,
      createdAt: new Date(startTime.getTime() - 60 * 60 * 1000).toISOString(),
      updatedAt: new Date(startTime.getTime() - 30 * 60 * 1000).toISOString()
    });
  }
};

const syncSeatStatusWithReservations = (): void => {
  mockData.reservations.forEach(reservation => {
    const seat = mockData.seats.find(s => s.id === reservation.seatId);
    if (!seat) return;

    if (reservation.status === 'confirmed') {
      seat.status = 'reserved';
    } else if (reservation.status === 'checked_in') {
      seat.status = 'occupied';
    } else if (reservation.status === 'completed' || reservation.status === 'cancelled') {
      if (seat.status === 'reserved' || seat.status === 'occupied') {
        seat.status = 'available';
      }
    }
  });
};

const generateCheckInRecords = (): void => {
  const now = new Date();
  const completedReservations = mockData.reservations.filter(r => r.status === 'completed');
  
  completedReservations.forEach((reservation, index) => {
    const checkInTime = new Date(reservation.startTime);
    const checkOutTime = new Date(reservation.endTime!);
    const duration = Math.floor((checkOutTime.getTime() - checkInTime.getTime()) / 60000);

    mockData.checkInRecords.push({
      id: `checkin_completed_${index}`,
      userId: reservation.userId,
      reservationId: reservation.id,
      studyRoomId: reservation.studyRoomId,
      seatId: reservation.seatId,
      checkInTime: checkInTime.toISOString(),
      checkOutTime: checkOutTime.toISOString(),
      duration,
      status: 'completed' as const
    });
  });
};

generateSeats();
generateReservations();
syncSeatStatusWithReservations();
generateCheckInRecords();

// ==================== 卡片系统 Mock 数据 ====================

const RARITY_BORDER_MAP: Record<CardRarity, string> = {
  'N': 'white',
  'R': 'green',
  'SR': 'blue',
  'SSR': 'purple',
  'UR': 'gold',
  'LR': 'red'
};

const RARITY_CARD_THEME_MAP: Record<CardRarity, string> = {
  'N': 'normal',
  'R': 'normal',
  'SR': 'normal',
  'SSR': 'normal',
  'UR': 'special_gold',
  'LR': 'special_red'
};

interface RarityPool {
  rarity: CardRarity;
  weight: number;
}

const RARITY_POOLS: Record<string, RarityPool[]> = {
  '0-10': [{ rarity: 'N', weight: 100 }],
  '10-30': [
    { rarity: 'N', weight: 45 },
    { rarity: 'R', weight: 45 },
    { rarity: 'SR', weight: 10 }
  ],
  '30-60': [
    { rarity: 'N', weight: 20 },
    { rarity: 'R', weight: 45 },
    { rarity: 'SR', weight: 25 },
    { rarity: 'SSR', weight: 10 }
  ],
  '60-120': [
    { rarity: 'N', weight: 10 },
    { rarity: 'R', weight: 35 },
    { rarity: 'SR', weight: 30 },
    { rarity: 'SSR', weight: 20 },
    { rarity: 'UR', weight: 5 }
  ],
  '120-240': [
    { rarity: 'N', weight: 5 },
    { rarity: 'R', weight: 25 },
    { rarity: 'SR', weight: 30 },
    { rarity: 'SSR', weight: 25 },
    { rarity: 'UR', weight: 14 },
    { rarity: 'LR', weight: 1 }
  ],
  '240+': [
    { rarity: 'N', weight: 3 },
    { rarity: 'R', weight: 20 },
    { rarity: 'SR', weight: 27 },
    { rarity: 'SSR', weight: 30 },
    { rarity: 'UR', weight: 18 },
    { rarity: 'LR', weight: 2 }
  ]
};

interface ThemePool {
  category: ThemeCategory;
  weight: number;
  minRarity?: CardRarity;
}

const THEME_POOLS: ThemePool[] = [
  { category: 'growth', weight: 25 },
  { category: 'history', weight: 15 },
  { category: 'philosophy', weight: 20 },
  { category: 'nature', weight: 15 },
  { category: 'tech', weight: 10 },
  { category: 'companion', weight: 10 },
  { category: 'hidden', weight: 5, minRarity: 'UR' }
];

const RARITY_ORDER: CardRarity[] = ['N', 'R', 'SR', 'SSR', 'UR', 'LR'];

function getRarityPool(duration: number): RarityPool[] {
  if (duration < 10) return RARITY_POOLS['0-10'];
  if (duration < 30) return RARITY_POOLS['10-30'];
  if (duration < 60) return RARITY_POOLS['30-60'];
  if (duration < 120) return RARITY_POOLS['60-120'];
  if (duration < 240) return RARITY_POOLS['120-240'];
  return RARITY_POOLS['240+'];
}

export function rollRarity(duration: number): CardRarity {
  const pool = getRarityPool(duration);
  const total = pool.reduce((sum, p) => sum + p.weight, 0);
  let rand = Math.random() * total;
  for (const item of pool) {
    rand -= item.weight;
    if (rand <= 0) return item.rarity;
  }
  return pool[pool.length - 1].rarity;
}

export function rollThemeCategory(rarity: CardRarity): ThemeCategory {
  const rarityIndex = RARITY_ORDER.indexOf(rarity);
  const available = THEME_POOLS.filter(t => {
    if (!t.minRarity) return true;
    const minIndex = RARITY_ORDER.indexOf(t.minRarity);
    return rarityIndex >= minIndex;
  });
  const total = available.reduce((sum, t) => sum + t.weight, 0);
  let rand = Math.random() * total;
  for (const item of available) {
    rand -= item.weight;
    if (rand <= 0) return item.category;
  }
  return available[available.length - 1].category;
}

const MARKDOWN_TEMPLATES: Record<ThemeCategory, string[]> = {
  growth: [
    '# 每一步都算数\n\n> 坚持不是天赋，是选择。\n\n---\n\n没有白走的路，每一步都在靠近。\n\n今天的你，比昨天更远。',
    '# 默默扎根\n\n> 所有看得见的生长，都来自看不见的坚持。\n\n---\n\n种子在黑暗中破土。\n\n你在安静中变强。'
  ],
  history: [
    '# 居里夫人的灯\n\n> 生活中没有什么可怕的东西，只有需要理解的东西。\n\n---\n\n她在简陋的实验室里，照亮了整个时代。\n\n而你此刻的灯光，也在照亮未来。',
    '# 爱迪生的千次\n\n> 我没有失败，我只是找到了一千种不行的方法。\n\n---\n\n每一次尝试都不是浪费。\n\n每一次重来都是积累。'
  ],
  philosophy: [
    '# 雨夜与灯火\n\n> 学习并不总是燃烧，也可以是缓慢照亮。\n\n---\n\n窗外下着雨。\n\n有人奔跑，有人停留。\n\n而你仍在向前。',
    '# 石头与河流\n\n> 不是力量最大的人走得最远，而是持续行走的人。\n\n---\n\n水不争，而能穿石。\n\n你不急，却从未停。'
  ],
  nature: [
    '# 星辰与书页\n\n> 宇宙很大，但此刻你比星光更专注。\n\n---\n\n夜空中最亮的光，不是恒星。\n\n是深夜仍亮着的那盏台灯。',
    '# 山间晨雾\n\n> 每一座山都从雾中醒来。\n\n---\n\n雾散之后，是更清晰的路。\n\n你也在慢慢变得清晰。'
  ],
  tech: [
    '# 代码与星辰\n\n> 每一行代码，都是通向未来的砖石。\n\n---\n\n屏幕上的光，是另一种星光。\n\n你正在构建的，是明天的世界。',
    '# 0与1之间\n\n> 在最简单的逻辑里，藏着最深刻的力量。\n\n---\n\n一切伟大的系统，都从零开始。\n\n你此刻就在起点。'
  ],
  companion: [
    '# 慢慢来\n\n> 不必着急，花开有时。\n\n---\n\n今天的你，已经很好了。\n\n明天，会再好一点。',
    '# 同行者\n\n> 你不是一个人在走。\n\n---\n\n总有人和你一样，在安静的角落努力。\n\n我们都在路上。'
  ],
  hidden: [
    '# 诸神的黄昏\n\n> 当神话落幕，人类才真正开始书写自己的故事。\n\n---\n\n你手中的笔，比命运更重。\n\n这一刻，属于你。',
    '# 梦境之门\n\n> 在梦与醒的边界，藏着一扇门。\n\n---\n\n推开它，是另一个宇宙。\n\n而你，恰好有钥匙。'
  ]
};

export function generateMarkdown(themeCategory: ThemeCategory): string {
  const templates = MARKDOWN_TEMPLATES[themeCategory];
  return templates[Math.floor(Math.random() * templates.length)];
}

const IMAGE_URLS: Record<ThemeCategory, string[]> = {
  growth: [
    'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=sunrise%20over%20mountain%20path%2C%20warm%20light%2C%20inspirational%20atmosphere%2C%20minimalist%20illustration&image_size=square_hd',
    'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=sprouting%20seed%20in%20morning%20light%2C%20soft%20colors%2C%20hope%20and%20growth&image_size=square_hd'
  ],
  history: [
    'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=old%20laboratory%20with%20warm%20lamp%20light%2C%20vintage%20scientific%20atmosphere&image_size=square_hd',
    'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=ancient%20scroll%20and%20candle%20light%2C%20historical%20wisdom&image_size=square_hd'
  ],
  philosophy: [
    'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=rainy%20night%20with%20warm%20window%20light%2C%20contemplative%20mood%2C%20watercolor%20style&image_size=square_hd',
    'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=river%20flowing%20through%20stones%2C%20zen%20atmosphere%2C%20ink%20painting%20style&image_size=square_hd'
  ],
  nature: [
    'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=starry%20night%20sky%20with%20reading%20lamp%2C%20cosmic%20atmosphere%2C%20dreamy&image_size=square_hd',
    'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=morning%20mist%20in%20mountains%2C%20soft%20pastel%20colors%2C%20peaceful&image_size=square_hd'
  ],
  tech: [
    'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=futuristic%20code%20on%20screen%20with%20starlight%2C%20cyberpunk%20minimal&image_size=square_hd',
    'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=digital%20binary%20world%2C%20blue%20neon%20light%2C%20clean%20tech%20aesthetic&image_size=square_hd'
  ],
  companion: [
    'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=soft%20warm%20light%20through%20window%2C%20cozy%20reading%20corner%2C%20gentle&image_size=square_hd',
    'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=two%20lanterns%20on%20a%20quiet%20path%2C%20warm%20companionship&image_size=square_hd'
  ],
  hidden: [
    'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=mythical%20gateway%20in%20cosmic%20void%2C%20epic%20surreal%20atmosphere%2C%20gold%20and%20crimson&image_size=square_hd',
    'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=dream%20door%20in%20starfield%2C%20mystical%20surreal%20art%2C%20deep%20purple%20and%20gold&image_size=square_hd'
  ]
};

export function getImageURL(themeCategory: ThemeCategory): string {
  const urls = IMAGE_URLS[themeCategory];
  return urls[Math.floor(Math.random() * urls.length)];
}

let cardCounter = 0;

export function generateCard(userID: string, studyDuration: number): Card {
  cardCounter++;
  const rarity = rollRarity(studyDuration);
  const themeCategory = rollThemeCategory(rarity);
  const markdown = generateMarkdown(themeCategory);
  const imageURL = getImageURL(themeCategory);

  return {
    uuid: `card_${Date.now()}_${cardCounter}`,
    userID,
    cardID: `tmpl_${rarity}_${themeCategory}_${cardCounter}`,
    createTime: new Date().toISOString().replace('T', ' ').substring(0, 19),
    studyDuration,
    rarity,
    borderTheme: RARITY_BORDER_MAP[rarity],
    cardTheme: RARITY_CARD_THEME_MAP[rarity],
    themeCategory,
    markdown,
    imageURL
  };
}

const generateInitialCards = (): void => {
  const user = mockData.users[0];
  if (!user) return;

  const durations = [5, 25, 45, 90, 150, 300];
  durations.forEach((duration, index) => {
    const card = generateCard(user.id, duration);
    card.uuid = `card_init_${index + 1}`;
    card.createTime = new Date(Date.now() - (durations.length - index) * 24 * 60 * 60 * 1000)
      .toISOString().replace('T', ' ').substring(0, 19);
    mockData.cards.push(card);
  });
};

generateInitialCards();
