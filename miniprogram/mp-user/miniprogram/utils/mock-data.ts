import type { User, StudyRoom, Seat, Reservation, CheckInRecord, Announcement, Rule } from '../typings/api';

export interface MockDataType {
  users: User[];
  studyRooms: StudyRoom[];
  seats: Seat[];
  reservations: Reservation[];
  checkInRecords: CheckInRecord[];
  announcements: Announcement[];
  rules: Rule[];
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
  ] as Rule[]
};

const generateSeats = (): void => {
  console.log('generateSeats called, current seats length:', mockData.seats.length);
  
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
  
  console.log('generateSeats finished, seats length:', mockData.seats.length);
};

const generateReservations = (): void => {
  const now = new Date();
  for (let i = 0; i < 10; i++) {
    const startTime = new Date(now.getTime() + (i + 1) * 24 * 60 * 60 * 1000);
    const endTime = new Date(startTime.getTime() + 3 * 60 * 60 * 1000);
    const status = i < 5 ? 'confirmed' : (i < 8 ? 'completed' : 'cancelled');

    mockData.reservations.push({
      id: `res_00${i + 1}`,
      userId: mockData.users[0].id,
      studyRoomId: mockData.studyRooms[i % mockData.studyRooms.length].id,
      seatId: mockData.seats[i % mockData.seats.length].id,
      startTime: startTime.toISOString(),
      endTime: endTime.toISOString(),
      status: status as any,
      checkInTime: status === 'completed' ? startTime.toISOString() : null,
      checkOutTime: status === 'completed' ? endTime.toISOString() : null,
      createdAt: new Date(now.getTime() - i * 24 * 60 * 60 * 1000).toISOString(),
      updatedAt: new Date(now.getTime() - i * 24 * 60 * 60 * 1000).toISOString()
    });
  }
};

const generateCheckInRecords = (): void => {
  const now = new Date();
  for (let i = 0; i < 5; i++) {
    const checkInTime = new Date(now.getTime() - (i + 1) * 24 * 60 * 60 * 1000);
    const checkOutTime = i < 3 ? new Date(checkInTime.getTime() + 3 * 60 * 60 * 1000) : null;
    const duration = checkOutTime ? 180 : 0;
    const status = checkOutTime ? 'completed' : 'active';

    mockData.checkInRecords.push({
      id: `checkin_00${i + 1}`,
      userId: mockData.users[0].id,
      reservationId: mockData.reservations[i].id,
      studyRoomId: mockData.reservations[i].studyRoomId,
      seatId: mockData.reservations[i].seatId,
      checkInTime: checkInTime.toISOString(),
      checkOutTime: checkOutTime?.toISOString() || null,
      duration,
      status: status as 'active' | 'completed'
    });
  }
};

console.log('mock-data.ts: Before generating data');
console.log('mockData.seats before generation:', mockData.seats.length);

generateSeats();
generateReservations();
generateCheckInRecords();

console.log('mock-data.ts: After generating data');
console.log('mockData.seats after generation:', mockData.seats.length);
console.log('mockData.seats isArray:', Array.isArray(mockData.seats));
