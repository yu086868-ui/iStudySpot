import pytest
import requests
import json
import time

BASE_URL = "http://localhost:8080"

# 测试用户登录
def test_login():
    url = f"{BASE_URL}/api/auth/login"
    data = {
        "username": "user001",
        "password": "newpassword123"
    }
    response = requests.post(url, json=data)
    print(f"Login response status: {response.status_code}")
    print(f"Login response body: {response.json()}")
    assert response.status_code == 200
    result = response.json()
    assert result["code"] == 200
    assert "token" in result["data"]
    assert "refreshToken" in result["data"]
    assert "user" in result["data"]
    return result["data"]["token"]

# 测试用户注册
def test_register():
    url = f"{BASE_URL}/api/auth/register"
    data = {
        "username": f"test_user_{int(time.time())}",
        "password": "123",
        "nickname": "测试用户",
        "phone": "13800138000",
        "studentId": "20240001"
    }
    response = requests.post(url, json=data)
    assert response.status_code == 200
    result = response.json()
    assert result["code"] == 200
    assert "userId" in result["data"]

# 测试获取自习室列表
def test_get_studyroom_list():
    url = f"{BASE_URL}/api/studyrooms"
    response = requests.get(url)
    assert response.status_code == 200
    result = response.json()
    assert result["code"] == 200
    assert "list" in result["data"]
    assert "total" in result["data"]
    assert "page" in result["data"]
    assert "pageSize" in result["data"]

# 测试获取自习室详情
def test_get_studyroom_detail():
    url = f"{BASE_URL}/api/studyrooms/1"
    response = requests.get(url)
    assert response.status_code == 200
    result = response.json()
    assert result["code"] == 200
    assert "id" in result["data"]
    assert "name" in result["data"]
    assert "description" in result["data"]

# 测试获取座位列表
def test_get_seat_list():
    url = f"{BASE_URL}/api/studyrooms/1/seats"
    response = requests.get(url)
    assert response.status_code == 200
    result = response.json()
    assert result["code"] == 200
    assert isinstance(result["data"], list)

# 测试获取座位详情
def test_get_seat_detail():
    url = f"{BASE_URL}/api/seats/1"
    response = requests.get(url)
    assert response.status_code == 200
    result = response.json()
    assert result["code"] == 200
    assert "id" in result["data"]
    assert "roomId" in result["data"]
    assert "seatNumber" in result["data"]

# 测试获取公告列表
def test_get_announcement_list():
    url = f"{BASE_URL}/api/announcements"
    response = requests.get(url)
    assert response.status_code == 200
    result = response.json()
    assert result["code"] == 200
    assert "list" in result["data"]
    assert "total" in result["data"]
    assert "page" in result["data"]
    assert "pageSize" in result["data"]

# 测试获取公告详情
def test_get_announcement_detail():
    url = f"{BASE_URL}/api/announcements/1"
    response = requests.get(url)
    assert response.status_code == 200
    result = response.json()
    assert result["code"] == 200
    assert "id" in result["data"]
    assert "title" in result["data"]
    assert "content" in result["data"]

# 测试获取规则列表
def test_get_rules_list():
    url = f"{BASE_URL}/api/rules"
    response = requests.get(url)
    assert response.status_code == 200
    result = response.json()
    assert result["code"] == 200
    assert isinstance(result["data"], list)

# 测试获取规则详情
def test_get_rule_detail():
    url = f"{BASE_URL}/api/rules/1"
    response = requests.get(url)
    assert response.status_code == 200
    result = response.json()
    assert result["code"] == 200
    assert "id" in result["data"]
    assert "title" in result["data"]
    assert "content" in result["data"]

# 测试需要认证的接口
def test_protected_endpoints():
    # 获取token
    token = test_login()
    
    # 测试获取用户信息
    url = f"{BASE_URL}/api/users/me"
    headers = {"Authorization": f"Bearer {token}"}
    response = requests.get(url, headers=headers)
    assert response.status_code == 200
    result = response.json()
    assert result["code"] == 200
    assert "id" in result["data"]
    assert "username" in result["data"]
    
    # 测试获取我的预约列表
    url = f"{BASE_URL}/api/reservations/my"
    response = requests.get(url, headers=headers)
    assert response.status_code == 200
    result = response.json()
    assert result["code"] == 200
    assert "list" in result["data"]
    assert "total" in result["data"]
    
    # 测试获取签到记录
    url = f"{BASE_URL}/api/checkin/records"
    response = requests.get(url, headers=headers)
    assert response.status_code == 200
    result = response.json()
    assert result["code"] == 200
    assert "list" in result["data"]
    assert "total" in result["data"]
    
    # 测试获取当前签到状态
    url = f"{BASE_URL}/api/checkin/current"
    response = requests.get(url, headers=headers)
    assert response.status_code == 200
    result = response.json()
    assert result["code"] == 200
    assert "isCheckedIn" in result["data"]

if __name__ == "__main__":
    import time
    pytest.main([__file__])
