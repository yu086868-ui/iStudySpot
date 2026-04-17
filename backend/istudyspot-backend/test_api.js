const axios = require('axios');

const BASE_URL = 'http://localhost:8080';
let token = '';

// 测试用户登录
test('用户登录', async () => {
  const response = await axios.post(`${BASE_URL}/api/auth/login`, {
    username: 'user001',
    password: '123'
  });
  expect(response.status).toBe(200);
  expect(response.data.code).toBe(200);
  expect(response.data.data.token).toBeDefined();
  expect(response.data.data.refreshToken).toBeDefined();
  expect(response.data.data.user).toBeDefined();
  token = response.data.data.token;
});

// 测试用户注册
test('用户注册', async () => {
  const username = `test_user_${Date.now()}`;
  const response = await axios.post(`${BASE_URL}/api/auth/register`, {
    username: username,
    password: '123',
    nickname: '测试用户',
    phone: '13800138000',
    studentId: '20240001'
  });
  expect(response.status).toBe(200);
  expect(response.data.code).toBe(200);
  expect(response.data.data.userId).toBeDefined();
});

// 测试获取自习室列表
test('获取自习室列表', async () => {
  const response = await axios.get(`${BASE_URL}/api/studyrooms`);
  expect(response.status).toBe(200);
  expect(response.data.code).toBe(200);
  expect(response.data.data.list).toBeDefined();
  expect(response.data.data.total).toBeDefined();
  expect(response.data.data.page).toBeDefined();
  expect(response.data.data.pageSize).toBeDefined();
});

// 测试获取自习室详情
test('获取自习室详情', async () => {
  const response = await axios.get(`${BASE_URL}/api/studyrooms/1`);
  expect(response.status).toBe(200);
  expect(response.data.code).toBe(200);
  expect(response.data.data.id).toBeDefined();
  expect(response.data.data.name).toBeDefined();
  expect(response.data.data.description).toBeDefined();
});

// 测试获取座位列表
test('获取座位列表', async () => {
  const response = await axios.get(`${BASE_URL}/api/studyrooms/1/seats`);
  expect(response.status).toBe(200);
  expect(response.data.code).toBe(200);
  expect(Array.isArray(response.data.data)).toBe(true);
});

// 测试获取座位详情
test('获取座位详情', async () => {
  const response = await axios.get(`${BASE_URL}/api/seats/1`);
  expect(response.status).toBe(200);
  expect(response.data.code).toBe(200);
  expect(response.data.data.id).toBeDefined();
  expect(response.data.data.studyRoomId).toBeDefined();
  expect(response.data.data.seatNumber).toBeDefined();
});

// 测试获取公告列表
test('获取公告列表', async () => {
  const response = await axios.get(`${BASE_URL}/api/announcements`);
  expect(response.status).toBe(200);
  expect(response.data.code).toBe(200);
  expect(response.data.data.list).toBeDefined();
  expect(response.data.data.total).toBeDefined();
  expect(response.data.data.page).toBeDefined();
  expect(response.data.data.pageSize).toBeDefined();
});

// 测试获取公告详情
test('获取公告详情', async () => {
  const response = await axios.get(`${BASE_URL}/api/announcements/1`);
  expect(response.status).toBe(200);
  expect(response.data.code).toBe(200);
  expect(response.data.data.id).toBeDefined();
  expect(response.data.data.title).toBeDefined();
  expect(response.data.data.content).toBeDefined();
});

// 测试获取规则列表
test('获取规则列表', async () => {
  const response = await axios.get(`${BASE_URL}/api/rules`);
  expect(response.status).toBe(200);
  expect(response.data.code).toBe(200);
  expect(Array.isArray(response.data.data)).toBe(true);
});

// 测试获取规则详情
test('获取规则详情', async () => {
  const response = await axios.get(`${BASE_URL}/api/rules/1`);
  expect(response.status).toBe(200);
  expect(response.data.code).toBe(200);
  expect(response.data.data.id).toBeDefined();
  expect(response.data.data.title).toBeDefined();
  expect(response.data.data.content).toBeDefined();
});

// 测试需要认证的接口
describe('需要认证的接口', () => {
  beforeAll(async () => {
    // 确保获取token
    if (!token) {
      const response = await axios.post(`${BASE_URL}/api/auth/login`, {
        username: 'user001',
        password: '123'
      });
      token = response.data.data.token;
    }
  });

  test('获取用户信息', async () => {
    const response = await axios.get(`${BASE_URL}/api/users/me`, {
      headers: {
        Authorization: `Bearer ${token}`
      }
    });
    expect(response.status).toBe(200);
    expect(response.data.code).toBe(200);
    expect(response.data.data.id).toBeDefined();
    expect(response.data.data.username).toBeDefined();
  });

  test('获取我的预约列表', async () => {
    const response = await axios.get(`${BASE_URL}/api/reservations/my`, {
      headers: {
        Authorization: `Bearer ${token}`
      }
    });
    expect(response.status).toBe(200);
    expect(response.data.code).toBe(200);
    expect(response.data.data.list).toBeDefined();
    expect(response.data.data.total).toBeDefined();
  });

  test('获取签到记录', async () => {
    const response = await axios.get(`${BASE_URL}/api/checkin/records`, {
      headers: {
        Authorization: `Bearer ${token}`
      }
    });
    expect(response.status).toBe(200);
    expect(response.data.code).toBe(200);
    expect(response.data.data.list).toBeDefined();
    expect(response.data.data.total).toBeDefined();
  });

  test('获取当前签到状态', async () => {
    const response = await axios.get(`${BASE_URL}/api/checkin/current`, {
      headers: {
        Authorization: `Bearer ${token}`
      }
    });
    expect(response.status).toBe(200);
    expect(response.data.code).toBe(200);
    expect(response.data.data.isCheckedIn).toBeDefined();
  });
});
