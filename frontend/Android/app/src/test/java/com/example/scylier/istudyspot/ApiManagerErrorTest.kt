package com.example.scylier.istudyspot

import com.example.scylier.istudyspot.infra.network.ApiManager
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class ApiManagerErrorTest {

    @Test
    fun testApiManager_withNullToken() = runBlocking {
        val apiManager = ApiManager(token = null)
        val response = apiManager.getStudyRooms()

        assertTrue(response is com.example.scylier.istudyspot.models.ApiResponse.Success)
    }

    @Test
    fun testApiManager_withEmptyToken() = runBlocking {
        val apiManager = ApiManager(token = "")
        val response = apiManager.getStudyRooms()

        assertTrue(response is com.example.scylier.istudyspot.models.ApiResponse.Success)
    }

    @Test
    fun testApiManager_withContext() = runBlocking {
        val apiManager = ApiManager(token = "test_token", context = null)
        val response = apiManager.getStudyRooms()

        assertTrue(response is com.example.scylier.istudyspot.models.ApiResponse.Success)
    }

    @Test
    fun testApiManager_login_withDifferentCredentials() = runBlocking {
        val apiManager = ApiManager()

        val response1 = apiManager.login("user1", "pass1")
        val response2 = apiManager.login("user2", "pass2")

        assertTrue(response1 is com.example.scylier.istudyspot.models.ApiResponse.Success)
        assertTrue(response2 is com.example.scylier.istudyspot.models.ApiResponse.Success)

        val success1 = response1 as com.example.scylier.istudyspot.models.ApiResponse.Success
        val success2 = response2 as com.example.scylier.istudyspot.models.ApiResponse.Success

        assertEquals("user1", success1.data.user.username)
        assertEquals("user2", success2.data.user.username)
    }

    @Test
    fun testApiManager_register_withDifferentData() = runBlocking {
        val apiManager = ApiManager()

        val response = apiManager.register("newuser", "password", "新用户昵称")

        assertTrue(response is com.example.scylier.istudyspot.models.ApiResponse.Success)
        val success = response as com.example.scylier.istudyspot.models.ApiResponse.Success
        assertEquals("newuser", success.data.user.username)
        assertEquals("新用户昵称", success.data.user.nickname)
    }

    @Test
    fun testApiManager_updateUserInfo_allFields() = runBlocking {
        val apiManager = ApiManager()

        val response = apiManager.updateUserInfo(
            nickname = "新昵称",
            avatar = "https://example.com/new_avatar.jpg",
            phone = "13900139000",
            email = "new@example.com"
        )

        assertTrue(response is com.example.scylier.istudyspot.models.ApiResponse.Success)
        val success = response as com.example.scylier.istudyspot.models.ApiResponse.Success
        assertEquals("新昵称", success.data.nickname)
        assertEquals("13900139000", success.data.phone)
        assertEquals("new@example.com", success.data.email)
    }

    @Test
    fun testApiManager_updateUserInfo_onlyNickname() = runBlocking {
        val apiManager = ApiManager()

        val response = apiManager.updateUserInfo(nickname = "只更新昵称")

        assertTrue(response is com.example.scylier.istudyspot.models.ApiResponse.Success)
        val success = response as com.example.scylier.istudyspot.models.ApiResponse.Success
        assertEquals("只更新昵称", success.data.nickname)
    }

    @Test
    fun testApiManager_updateUserInfo_onlyPhone() = runBlocking {
        val apiManager = ApiManager()

        val response = apiManager.updateUserInfo(phone = "13800138001")

        assertTrue(response is com.example.scylier.istudyspot.models.ApiResponse.Success)
        val success = response as com.example.scylier.istudyspot.models.ApiResponse.Success
        assertEquals("13800138001", success.data.phone)
    }

    @Test
    fun testApiManager_updateUserInfo_onlyEmail() = runBlocking {
        val apiManager = ApiManager()

        val response = apiManager.updateUserInfo(email = "email@test.com")

        assertTrue(response is com.example.scylier.istudyspot.models.ApiResponse.Success)
        val success = response as com.example.scylier.istudyspot.models.ApiResponse.Success
        assertEquals("email@test.com", success.data.email)
    }

    @Test
    fun testApiManager_getStudyRoomSeats_seatCount() = runBlocking {
        val apiManager = ApiManager()

        val response = apiManager.getStudyRoomSeats("1")

        assertTrue(response is com.example.scylier.istudyspot.models.ApiResponse.Success)
        val success = response as com.example.scylier.istudyspot.models.ApiResponse.Success
        assertEquals(40, success.data.seats.size) // 5 rows * 8 cols = 40 seats
    }

    @Test
    fun testApiManager_getStudyRoomSeats_differentStatuses() = runBlocking {
        val apiManager = ApiManager()

        val response = apiManager.getStudyRoomSeats("1")

        assertTrue(response is com.example.scylier.istudyspot.models.ApiResponse.Success)
        val success = response as com.example.scylier.istudyspot.models.ApiResponse.Success

        val availableSeats = success.data.seats.filter { it.status == "available" }
        val bookedSeats = success.data.seats.filter { it.status == "booked" }
        val occupiedSeats = success.data.seats.filter { it.status == "occupied" }

        assertTrue(availableSeats.isNotEmpty() || bookedSeats.isNotEmpty() || occupiedSeats.isNotEmpty())
    }

    @Test
    fun testApiManager_getStudyRoomSeats_vipSeats() = runBlocking {
        val apiManager = ApiManager()

        val response = apiManager.getStudyRoomSeats("1")

        assertTrue(response is com.example.scylier.istudyspot.models.ApiResponse.Success)
        val success = response as com.example.scylier.istudyspot.models.ApiResponse.Success

        val vipSeats = success.data.seats.filter { it.type == "vip" }
        assertTrue(vipSeats.isNotEmpty())
        vipSeats.forEach { seat ->
            assertEquals(15.0, seat.pricePerHour, 0.01)
        }
    }

    @Test
    fun testApiManager_getStudyRoomSeats_normalSeats() = runBlocking {
        val apiManager = ApiManager()

        val response = apiManager.getStudyRoomSeats("1")

        assertTrue(response is com.example.scylier.istudyspot.models.ApiResponse.Success)
        val success = response as com.example.scylier.istudyspot.models.ApiResponse.Success

        val normalSeats = success.data.seats.filter { it.type == "normal" }
        assertTrue(normalSeats.isNotEmpty())
        normalSeats.forEach { seat ->
            assertEquals(10.0, seat.pricePerHour, 0.01)
        }
    }

    @Test
    fun testApiManager_createOrder_withDifferentBookingTypes() = runBlocking {
        val apiManager = ApiManager()

        val response1 = apiManager.createOrder("seat1", "2026-10-01T10:00:00", "2026-10-01T12:00:00", "hourly")
        val response2 = apiManager.createOrder("seat2", "2026-10-01T14:00:00", "2026-10-01T18:00:00", "half_day")

        assertTrue(response1 is com.example.scylier.istudyspot.models.ApiResponse.Success)
        assertTrue(response2 is com.example.scylier.istudyspot.models.ApiResponse.Success)
    }

    @Test
    fun testApiManager_getUserOrders_withStatus() = runBlocking {
        val apiManager = ApiManager()

        val response = apiManager.getUserOrders(status = "pending")

        assertTrue(response is com.example.scylier.istudyspot.models.ApiResponse.Success)
    }

    @Test
    fun testApiManager_getUserOrders_withPagination() = runBlocking {
        val apiManager = ApiManager()

        val response = apiManager.getUserOrders(page = 1, size = 5)

        assertTrue(response is com.example.scylier.istudyspot.models.ApiResponse.Success)
    }

    @Test
    fun testApiManager_createPayment_differentMethods() = runBlocking {
        val apiManager = ApiManager()

        val response1 = apiManager.createPayment("order1", 20.0, "wechat")
        val response2 = apiManager.createPayment("order2", 30.0, "alipay")

        assertTrue(response1 is com.example.scylier.istudyspot.models.ApiResponse.Success)
        assertTrue(response2 is com.example.scylier.istudyspot.models.ApiResponse.Success)

        val success1 = response1 as com.example.scylier.istudyspot.models.ApiResponse.Success
        val success2 = response2 as com.example.scylier.istudyspot.models.ApiResponse.Success

        assertEquals("wechat", success1.data.paymentMethod)
        assertEquals("alipay", success2.data.paymentMethod)
    }

    @Test
    fun testApiManager_getStudyRoomStatistics_dailyData() = runBlocking {
        val apiManager = ApiManager()

        val response = apiManager.getStudyRoomStatistics("1", "2026-10-01", "2026-10-07")

        assertTrue(response is com.example.scylier.istudyspot.models.ApiResponse.Success)
        val success = response as com.example.scylier.istudyspot.models.ApiResponse.Success

        assertTrue(success.data.dailyData.isNotEmpty())
        success.data.dailyData.forEach { daily ->
            assertTrue(daily.occupancyRate in 0.0..1.0)
            assertTrue(daily.bookings >= 0)
            assertTrue(daily.revenue >= 0)
        }
    }
}
