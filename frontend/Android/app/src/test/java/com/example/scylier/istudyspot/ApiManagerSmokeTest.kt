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
    fun getStudyRoomSeatLayoutShouldReturnHybridMockLayout() = runBlocking {
        val response = apiManager.getStudyRoomSeatLayout(2L)

        assertTrue(response is ApiResponse.Success)
        val success = response as ApiResponse.Success
        assertEquals("hybrid", success.data?.layoutMode)
        assertTrue((success.data?.items?.size ?: 0) > 0)
        assertTrue((success.data?.seats?.size ?: 0) > 0)
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

    @Test
    fun getUserOrdersShouldPreferSeatNumbers() = runBlocking {
        val response = apiManager.getUserOrders()

        assertTrue(response is ApiResponse.Success)
        val success = response as ApiResponse.Success
        val orders = success.data?.list.orEmpty()
        assertTrue(orders.isNotEmpty())
        assertEquals("A01", orders.first().seatNumber)
        assertEquals("A01", orders.first().displaySeat)
        assertEquals("B03", orders.getOrNull(1)?.displaySeat)
    }

    @Test
    fun executeAgentSeatToolShouldReturnNormalizedSeatLabels() = runBlocking {
        val response = apiManager.executeAgentTool(
            tool = "list_room_seats",
            arguments = mapOf("studyRoomId" to 1L)
        )

        assertTrue(response is ApiResponse.Success)
        val success = response as ApiResponse.Success
        val items = success.data?.data?.get("items") as? List<Map<String, Any?>>
        assertEquals("A01", items?.firstOrNull()?.get("seatNumber"))
        assertEquals("A02", items?.getOrNull(1)?.get("seatNumber"))
    }

    @Test
    fun executeAgentReservationToolShouldReturnSeatNumberForSingleSeat() = runBlocking {
        val response = apiManager.executeAgentTool(tool = "get_my_reservations")

        assertTrue(response is ApiResponse.Success)
        val success = response as ApiResponse.Success
        val items = success.data?.data?.get("items") as? List<Map<String, Any?>>
        val firstItem = items?.firstOrNull()
        assertEquals("A03", firstItem?.get("seatNumber"))
        assertEquals("A03", firstItem?.get("seatPosition"))
    }
}
