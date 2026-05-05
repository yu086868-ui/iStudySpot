package com.example.scylier.istudyspot

import com.example.scylier.istudyspot.infra.network.ApiClient
import com.example.scylier.istudyspot.infra.network.ApiService
import org.junit.Assert.*
import org.junit.Test

class ApiClientTest {

    @Test
    fun testCreateService_withoutToken() {
        val service = ApiClient.createService(ApiService::class.java)
        assertNotNull(service)
    }

    @Test
    fun testCreateService_withToken() {
        val service = ApiClient.createService(ApiService::class.java, "test_token")
        assertNotNull(service)
    }

    @Test
    fun testCreateService_returnsSameType() {
        val service = ApiClient.createService(ApiService::class.java)
        assertTrue(service is ApiService)
    }

    @Test
    fun testCreateService_withEmptyToken() {
        val service = ApiClient.createService(ApiService::class.java, "")
        assertNotNull(service)
    }

    @Test
    fun testCreateService_withNullToken() {
        val service = ApiClient.createService(ApiService::class.java, null)
        assertNotNull(service)
    }

    @Test
    fun testCreateService_multipleCalls_returnsDifferentInstances() {
        val service1 = ApiClient.createService(ApiService::class.java)
        val service2 = ApiClient.createService(ApiService::class.java)

        assertNotSame(service1, service2)
    }

    @Test
    fun testCreateService_withDifferentTokens() {
        val service1 = ApiClient.createService(ApiService::class.java, "token1")
        val service2 = ApiClient.createService(ApiService::class.java, "token2")

        assertNotNull(service1)
        assertNotNull(service2)
    }
}
