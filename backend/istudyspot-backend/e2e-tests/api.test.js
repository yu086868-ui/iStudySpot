const { test, expect } = require('@playwright/test');

const BASE_URL = 'http://localhost:8080/api';

let token;

// 测试认证接口
test('认证接口测试', async ({ request }) => {
  // 登录测试用户（使用已存在的测试用户）
  const loginResponse = await request.post(`${BASE_URL}/auth/login`, {
    data: {
      username: 'testuser',
      password: '123456'
    }
  });
  expect(loginResponse.status()).toBe(200);
  const loginData = await loginResponse.json();
  expect(loginData.code).toBe(200);
  expect(loginData.data).toHaveProperty('token');
  token = loginData.data.token;
});

// 测试用户接口
test('用户接口测试', async ({ request }) => {
  // 获取用户信息
  const getUserResponse = await request.get(`${BASE_URL}/users/me`, {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });
  expect(getUserResponse.status()).toBe(200);
  const getUserData = await getUserResponse.json();
  expect(getUserData.code).toBe(200);
  expect(getUserData.data).toHaveProperty('id');
  expect(getUserData.data).toHaveProperty('username');

  // 更新用户信息
  const updateUserResponse = await request.put(`${BASE_URL}/users/me`, {
    headers: {
      'Authorization': `Bearer ${token}`
    },
    data: {
      nickname: 'Updated Test User',
      phone: '13900139000'
    }
  });
  expect(updateUserResponse.status()).toBe(200);
  const updateUserData = await updateUserResponse.json();
  expect(updateUserData.code).toBe(200);
  expect(updateUserData.data.nickname).toBe('Updated Test User');
  expect(updateUserData.data.phone).toBe('13900139000');
});

// 测试测试接口
test('测试接口测试', async ({ request }) => {
  const testResponse = await request.get(`${BASE_URL}/test`);
  expect(testResponse.status()).toBe(200);
  const testData = await testResponse.json();
  expect(testData.code).toBe(200);
  expect(testData.message).toBe('操作成功');
});

// 测试AI接口
test('AI接口测试', async ({ request }) => {
  // 获取角色列表
  const getCharactersResponse = await request.get(`${BASE_URL}/characters`);
  expect(getCharactersResponse.status()).toBe(200);
  const getCharactersData = await getCharactersResponse.json();
  expect(Array.isArray(getCharactersData)).toBe(true);
});

// 测试客户服务接口
test('客户服务接口测试', async ({ request }) => {
  // 获取欢迎信息
  const getWelcomeResponse = await request.get(`${BASE_URL}/customer-service/welcome`);
  expect(getWelcomeResponse.status()).toBe(200);
  const getWelcomeData = await getWelcomeResponse.json();
  expect(getWelcomeData).toHaveProperty('welcomeMessage');
  expect(getWelcomeData).toHaveProperty('recommendedQuestions');
});

// 测试规则接口
test('规则接口测试', async ({ request }) => {
  // 获取规则列表
  const getRulesResponse = await request.get(`${BASE_URL}/rules`);
  expect(getRulesResponse.status()).toBe(200);
  const getRulesData = await getRulesResponse.json();
  expect(getRulesData.code).toBe(200);
  expect(Array.isArray(getRulesData.data)).toBe(true);

  // 获取规则详情
  if (getRulesData.data.length > 0) {
    const ruleId = getRulesData.data[0].id;
    const getRuleResponse = await request.get(`${BASE_URL}/rules/${ruleId}`);
    expect(getRuleResponse.status()).toBe(200);
    const getRuleData = await getRuleResponse.json();
    expect(getRuleData.code).toBe(200);
    expect(getRuleData.data.id).toBe(ruleId);
  }
});
