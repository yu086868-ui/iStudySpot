package com.example.scylier.istudyspot

import com.example.scylier.istudyspot.infra.network.ApiManager
import com.example.scylier.istudyspot.models.ApiResponse
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ApiManagerSmokeTest {

    private val apiManager = ApiManager(useMockData = true)

    @Test
    fun loginShouldReturnMockUserAndToken() = runBlocking {
        val response = apiManager.login("testuser", "password123")

        assertTrue(response is ApiResponse.Success)
        val success = response as ApiResponse.Success
        assertEquals(200, success.code)
        assertNotNull(success.data?.token)
        assertEquals("testuser", success.data?.user?.username)
    }

    @Test
    fun getStudyRoomsShouldReturnRoomList() = runBlocking {
        val response = apiManager.getStudyRooms()

        assertTrue(response is ApiResponse.Success)
        val success = response as ApiResponse.Success
        assertTrue((success.data?.list?.size ?: 0) > 0)
        assertTrue((success.data?.total ?: 0) >= 0)
    }

    @Test
    fun getStudyRoomDetailShouldExposeBasicFields() = runBlocking {
        val response = apiManager.getStudyRoomDetail(1L)

        assertTrue(response is ApiResponse.Success)
        val success = response as ApiResponse.Success
        assertEquals(1L, success.data?.id)
        assertNotNull(success.data?.name)
        assertNotNull(success.data?.address)
    }

    @Test
    fun createOrderShouldReturnPendingOrder() = runBlocking {
        val response = apiManager.createOrder(
            studyRoomId = 1L,
            seatId = 1L,
            startTime = "2026-10-01T10:00:00",
            endTime = "2026-10-01T12:00:00",
            bookingType = "hourly"
        )

        assertTrue(response is ApiResponse.Success)
        val success = response as ApiResponse.Success
        assertEquals("pending", success.data?.status)
        assertEquals(1L, success.data?.seatId)
    }
}
