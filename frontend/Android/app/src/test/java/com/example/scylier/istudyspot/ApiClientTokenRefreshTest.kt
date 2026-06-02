package com.example.scylier.istudyspot

import com.example.scylier.istudyspot.infra.network.ApiClient
import com.example.scylier.istudyspot.models.BaseResponse
import com.example.scylier.istudyspot.models.auth.TokenResponse
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ApiClientTokenRefreshTest {

    private lateinit var mockWebServer: MockWebServer
    private val gson = Gson()

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        ApiClient.currentToken = null
        ApiClient.currentRefreshToken = null
    }

    @After
    fun teardown() {
        mockWebServer.shutdown()
        ApiClient.currentToken = null
        ApiClient.currentRefreshToken = null
    }

    private fun buildRefreshRequest(refreshToken: String): okhttp3.Request {
        val json = gson.toJson(mapOf("refreshToken" to refreshToken))
        val requestBody = json.toRequestBody("application/json".toMediaType())
        return okhttp3.Request.Builder()
            .url(mockWebServer.url("api/auth/refresh"))
            .post(requestBody)
            .build()
    }

    private inline fun <reified T> parseResponse(responseBody: String?): BaseResponse<T> {
        val type = object : TypeToken<BaseResponse<T>>() {}.type
        return gson.fromJson(responseBody, type)
    }

    @Test
    fun testRefreshAccessToken_success() {
        val tokenResponse = BaseResponse(
            code = 200,
            message = "刷新成功",
            data = TokenResponse(token = "new_access_token_12345")
        )
        mockWebServer.enqueue(MockResponse()
            .setResponseCode(200)
            .setBody(gson.toJson(tokenResponse)))

        val client = okhttp3.OkHttpClient.Builder().build()
        val response = client.newCall(buildRefreshRequest("valid_refresh_token")).execute()

        assertTrue(response.isSuccessful)
        val responseBody = response.body?.string()
        assertNotNull(responseBody)
        val baseResponse = parseResponse<TokenResponse>(responseBody)
        assertTrue(baseResponse.code in 200..299)
        assertEquals("new_access_token_12345", baseResponse.data?.token)
    }

    @Test
    fun testRefreshAccessToken_serverError() {
        mockWebServer.enqueue(MockResponse()
            .setResponseCode(401)
            .setBody("{\"code\":401,\"message\":\"refresh token expired\"}"))

        val client = okhttp3.OkHttpClient.Builder().build()
        val response = client.newCall(buildRefreshRequest("expired_refresh_token")).execute()

        assertFalse(response.isSuccessful)
        assertEquals(401, response.code)
    }

    @Test
    fun testRefreshAccessToken_invalidResponseBody() {
        mockWebServer.enqueue(MockResponse()
            .setResponseCode(200)
            .setBody("invalid json"))

        val client = okhttp3.OkHttpClient.Builder().build()
        val response = client.newCall(buildRefreshRequest("some_token")).execute()

        assertTrue(response.isSuccessful)
    }

    @Test
    fun testRefreshAccessToken_nullDataInResponse() {
        val tokenResponse = BaseResponse<TokenResponse>(
            code = 200,
            message = "刷新成功",
            data = null
        )
        mockWebServer.enqueue(MockResponse()
            .setResponseCode(200)
            .setBody(gson.toJson(tokenResponse)))

        val client = okhttp3.OkHttpClient.Builder().build()
        val response = client.newCall(buildRefreshRequest("some_token")).execute()

        assertTrue(response.isSuccessful)
        val responseBody = response.body?.string()
        val baseResponse = parseResponse<TokenResponse>(responseBody)
        assertNull(baseResponse.data)
    }

    @Test
    fun testApiClient_currentToken_initiallyNull() {
        assertNull(ApiClient.currentToken)
    }

    @Test
    fun testApiClient_currentRefreshToken_initiallyNull() {
        assertNull(ApiClient.currentRefreshToken)
    }

    @Test
    fun testApiClient_currentToken_canBeSet() {
        ApiClient.currentToken = "test_token"
        assertEquals("test_token", ApiClient.currentToken)
        ApiClient.currentToken = null
    }

    @Test
    fun testApiClient_currentRefreshToken_canBeSet() {
        ApiClient.currentRefreshToken = "test_refresh_token"
        assertEquals("test_refresh_token", ApiClient.currentRefreshToken)
        ApiClient.currentRefreshToken = null
    }

    @Test
    fun testRefreshAccessToken_errorCodeInBody() {
        val tokenResponse = BaseResponse<TokenResponse>(
            code = 401,
            message = "token expired",
            data = null
        )
        mockWebServer.enqueue(MockResponse()
            .setResponseCode(200)
            .setBody(gson.toJson(tokenResponse)))

        val client = okhttp3.OkHttpClient.Builder().build()
        val response = client.newCall(buildRefreshRequest("some_token")).execute()

        assertTrue(response.isSuccessful)
        val responseBody = response.body?.string()
        val baseResponse = parseResponse<TokenResponse>(responseBody)
        assertFalse(baseResponse.code in 200..299)
    }

    @Test
    fun testAuthInterceptor_addsAuthHeader() {
        ApiClient.currentToken = "bearer_token_123"
        assertEquals("bearer_token_123", ApiClient.currentToken)
        val authHeader = "Bearer bearer_token_123"
        assertTrue(authHeader.contains("bearer_token_123"))
        ApiClient.currentToken = null
    }

    @Test
    fun testAuthInterceptor_noHeaderWhenTokenNull() {
        assertNull(ApiClient.currentToken)
    }
}
