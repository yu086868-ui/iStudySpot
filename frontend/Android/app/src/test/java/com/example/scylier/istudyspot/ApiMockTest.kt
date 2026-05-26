package com.example.scylier.istudyspot

import com.example.scylier.istudyspot.infra.network.ApiService
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.example.scylier.istudyspot.models.auth.LoginRequest
import com.example.scylier.istudyspot.models.auth.RegisterRequest
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals

class ApiMockTest {
    private lateinit var mockWebServer: MockWebServer
    private lateinit var apiService: ApiService

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        val retrofit = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(ApiService::class.java)
    }

    @After
    fun teardown() {
        mockWebServer.shutdown()
    }

    @Test
    fun testLoginSuccess() = runBlocking {
        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setBody("""
                {
                    "code": 200,
                    "message": "登录成功",
                    "data": {
                        "token": "test_token",
                        "user": {
                            "id": 1,
                            "username": "test",
                            "nickname": "测试用户",
                            "avatar": "https://example.com/avatar.jpg"
                        }
                    }
                }
            """)

        mockWebServer.enqueue(mockResponse)

        val loginRequest = LoginRequest("test", "password")
        val response = apiService.login(loginRequest)

        assertEquals(200, response.code())
        val body = response.body()
        assertEquals(200, body?.code)
        assertEquals("登录成功", body?.message)
        assertEquals("test_token", body?.data?.token)
        assertEquals("test", body?.data?.user?.username)
        assertEquals("测试用户", body?.data?.user?.nickname)
    }

    @Test
    fun testRegisterSuccess() = runBlocking {
        val mockResponse = MockResponse()
            .setResponseCode(201)
            .setBody("""
                {
                    "code": 201,
                    "message": "注册成功",
                    "data": {
                        "token": "test_token",
                        "user": {
                            "id": 1,
                            "username": "test",
                            "nickname": "测试用户"
                        }
                    }
                }
            """)

        mockWebServer.enqueue(mockResponse)

        val registerRequest = RegisterRequest("test", "password", "测试用户")
        val response = apiService.register(registerRequest)

        assertEquals(201, response.code())
        val body = response.body()
        assertEquals(201, body?.code)
        assertEquals("注册成功", body?.message)
        assertEquals("test_token", body?.data?.token)
        assertEquals("test", body?.data?.user?.username)
        assertEquals("测试用户", body?.data?.user?.nickname)
    }

    @Test
    fun testGetUserInfo() = runBlocking {
        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setBody("""
                {
                    "code": 200,
                    "message": "获取成功",
                    "data": {
                        "id": 1,
                        "username": "test",
                        "nickname": "测试用户",
                        "avatar": "https://example.com/avatar.jpg",
                        "phone": "13800138000",
                        "email": "test@example.com"
                    }
                }
            """)

        mockWebServer.enqueue(mockResponse)

        val response = apiService.getUserInfo()

        assertEquals(200, response.code())
        val body = response.body()
        assertEquals(200, body?.code)
        assertEquals("获取成功", body?.message)
        assertEquals(1L, body?.data?.id)
        assertEquals("test", body?.data?.username)
        assertEquals("测试用户", body?.data?.nickname)
        assertEquals("13800138000", body?.data?.phone)
        assertEquals("test@example.com", body?.data?.email)
    }

    @Test
    fun testGetStudyRooms() = runBlocking {
        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setBody("""
                {
                    "code": 200,
                    "message": "获取成功",
                    "data": {
                        "total": 2,
                        "list": [
                            {
                                "id": 1,
                                "name": "自习室1",
                                "address": "图书馆三楼",
                                "openTime": "08:00",
                                "closeTime": "22:00",
                                "imageUrl": "https://example.com/room1.jpg"
                            },
                            {
                                "id": 2,
                                "name": "自习室2",
                                "address": "图书馆四楼",
                                "openTime": "08:00",
                                "closeTime": "22:00",
                                "imageUrl": "https://example.com/room2.jpg"
                            }
                        ]
                    }
                }
            """)

        mockWebServer.enqueue(mockResponse)

        val response = apiService.getStudyRooms()

        assertEquals(200, response.code())
        val body = response.body()
        assertEquals(200, body?.code)
        assertEquals("获取成功", body?.message)
        assertEquals(2, body?.data?.total)
        assertEquals(2, body?.data?.list?.size)
        assertEquals("自习室1", body?.data?.list?.get(0)?.name)
        assertEquals("自习室2", body?.data?.list?.get(1)?.name)
    }

    @Test
    fun testLoginFailure() = runBlocking {
        val mockResponse = MockResponse()
            .setResponseCode(401)
            .setBody("""
                {
                    "code": 401,
                    "message": "用户名或密码错误"
                }
            """)

        mockWebServer.enqueue(mockResponse)

        val loginRequest = LoginRequest("test", "wrong_password")
        val response = apiService.login(loginRequest)

        assertEquals(401, response.code())
    }
}
