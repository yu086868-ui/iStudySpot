package com.example.scylier.istudyspot

import com.example.scylier.istudyspot.infra.network.ApiService
import com.example.scylier.istudyspot.models.ai.AiChatRequest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AiChatApiMockTest {
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
    fun testAiChatSuccess() = kotlinx.coroutines.runBlocking {
        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setBody("""
                {
                    "code": 200,
                    "message": "success",
                    "data": {
                        "reply": "预约座位的流程如下：\n1. 打开APP，点击首页的\"预约座位\"",
                        "sessionId": "session-123"
                    }
                }
            """)

        mockWebServer.enqueue(mockResponse)

        val request = AiChatRequest(
            message = "如何预约座位？",
            sessionId = null
        )
        val response = apiService.sendAiMessage(request)

        assertEquals(200, response.code())
        val body = response.body()
        assertEquals(200, body?.code)
        assertEquals("success", body?.message)
        assertEquals("session-123", body?.data?.sessionId)
        assert(body?.data?.reply?.contains("预约座位") == true)
    }

    @Test
    fun testAiChatWithSessionId() = kotlinx.coroutines.runBlocking {
        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setBody("""
                {
                    "code": 200,
                    "message": "success",
                    "data": {
                        "reply": "签到流程非常简单：\n1. 到达自习室后，打开APP",
                        "sessionId": "session-456"
                    }
                }
            """)

        mockWebServer.enqueue(mockResponse)

        val request = AiChatRequest(
            message = "如何签到？",
            sessionId = "session-456"
        )
        val response = apiService.sendAiMessage(request)

        assertEquals(200, response.code())
        val body = response.body()
        assertEquals("session-456", body?.data?.sessionId)
    }

    @Test
    fun testAiChatServerError() = kotlinx.coroutines.runBlocking {
        val mockResponse = MockResponse()
            .setResponseCode(500)
            .setBody("""
                {
                    "code": 500,
                    "message": "Internal Server Error"
                }
            """)

        mockWebServer.enqueue(mockResponse)

        val request = AiChatRequest(
            message = "测试消息",
            sessionId = null
        )
        val response = apiService.sendAiMessage(request)

        assertEquals(500, response.code())
    }

    @Test
    fun testAiChatRateLimit() = kotlinx.coroutines.runBlocking {
        val mockResponse = MockResponse()
            .setResponseCode(429)
            .setBody("""
                {
                    "code": 429,
                    "message": "Too many requests"
                }
            """)

        mockWebServer.enqueue(mockResponse)

        val request = AiChatRequest(
            message = "测试消息",
            sessionId = null
        )
        val response = apiService.sendAiMessage(request)

        assertEquals(429, response.code())
    }
}
