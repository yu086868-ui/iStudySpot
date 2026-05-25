package com.example.scylier.istudyspot

import com.example.scylier.istudyspot.infra.network.ApiClient
import com.example.scylier.istudyspot.infra.network.ApiManager
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class ApiManagerTest {

    private val apiManager = ApiManager()

    @Test
    fun testLogin_success() = runBlocking {
        val response = apiManager.login("testuser", "password123")

        assertTrue(response is com.example.scylier.istudyspot.models.ApiResponse.Success)
        val success = response as com.example.scylier.istudyspot.models.ApiResponse.Success
        assertEquals(200, success.code)
        assertEquals("登录成功", success.message)
        assertNotNull(success.data.token)
        assertEquals("testuser", success.data.user?.username)
    }

    @Test
    fun testRegister_success() = runBlocking {
        val response = apiManager.register("newuser", "password123", "新用户")

        assertTrue(response is com.example.scylier.istudyspot.models.ApiResponse.Success)
        val success = response as com.example.scylier.istudyspot.models.ApiResponse.Success
        assertEquals(201, success.code)
        assertEquals("注册成功", success.message)
        assertNotNull(success.data.token)
        assertEquals("newuser", success.data.user?.username)
        assertEquals("新用户", success.data.user?.nickname)
    }

    @Test
    fun testRefreshToken_success() = runBlocking {
        val response = apiManager.refreshToken("test_refresh_token")

        assertTrue(response is com.example.scylier.istudyspot.models.ApiResponse.Success)
        val success = response as com.example.scylier.istudyspot.models.ApiResponse.Success
        assertEquals(200, success.code)
        assertNotNull(success.data.token)
    }

    @Test
    fun testGetStudyRooms_success() = runBlocking {
        val response = apiManager.getStudyRooms()

        assertTrue(response is com.example.scylier.istudyspot.models.ApiResponse.Success)
        val success = response as com.example.scylier.istudyspot.models.ApiResponse.Success
        assertEquals(200, success.code)
        assertNotNull(success.data.list)
        assertTrue(success.data.total >= 0)
    }

    @Test
    fun testGetStudyRooms_withPagination() = runBlocking {
        val response = apiManager.getStudyRooms(page = 2, pageSize = 5)

        assertTrue(response is com.example.scylier.istudyspot.models.ApiResponse.Success)
    }

    @Test
    fun testGetStudyRoomDetail_success() = runBlocking {
        val response = apiManager.getStudyRoomDetail(1L)

        assertTrue(response is com.example.scylier.istudyspot.models.ApiResponse.Success)
        val success = response as com.example.scylier.istudyspot.models.ApiResponse.Success
        assertEquals(1L, success.data.id)
        assertNotNull(success.data.name)
        assertNotNull(success.data.address)
        assertNotNull(success.data.openingHours)
    }

    @Test
    fun testGetStudyRoomSeats_success() = runBlocking {
        val response = apiManager.getStudyRoomSeats(1L)

        assertTrue(response is com.example.scylier.istudyspot.models.ApiResponse.Success)
        val success = response as com.example.scylier.istudyspot.models.ApiResponse.Success
        assertTrue(success.data.isNotEmpty())
    }

    @Test
    fun testGetStudyRoomSeats_seatStatuses() = runBlocking {
        val response = apiManager.getStudyRoomSeats(1L)

        assertTrue(response is com.example.scylier.istudyspot.models.ApiResponse.Success)
        val success = response as com.example.scylier.istudyspot.models.ApiResponse.Success
        val seats = success.data

        val availableSeats = seats.filter { it.status == "available" }
        val bookedSeats = seats.filter { it.status == "booked" }
        val occupiedSeats = seats.filter { it.status == "in_use" }

        assertTrue(availableSeats.isNotEmpty() || bookedSeats.isNotEmpty() || occupiedSeats.isNotEmpty())
    }

    @Test
    fun testGetSeatDetail_success() = runBlocking {
        val response = apiManager.getSeatDetail(1L)

        assertTrue(response is com.example.scylier.istudyspot.models.ApiResponse.Success)
        val success = response as com.example.scylier.istudyspot.models.ApiResponse.Success
        assertEquals(1L, success.data.id)
        assertNotNull(success.data.status)
        assertNotNull(success.data.type)
        assertTrue(success.data.pricePerHour > 0)
    }

    @Test
    fun testCreateOrder_success() = runBlocking {
        val response = apiManager.createOrder(
            studyRoomId = 1L,
            seatId = 1L,
            startTime = "2026-10-01T10:00:00",
            endTime = "2026-10-01T12:00:00",
            bookingType = "hourly"
        )

        assertTrue(response is com.example.scylier.istudyspot.models.ApiResponse.Success)
        val success = response as com.example.scylier.istudyspot.models.ApiResponse.Success
        assertEquals(201, success.code)
        assertNotNull(success.data.id)
        assertEquals(1L, success.data.seatId)
        assertEquals("pending", success.data.status)
    }

    @Test
    fun testGetUserOrders_success() = runBlocking {
        val response = apiManager.getUserOrders()

        assertTrue(response is com.example.scylier.istudyspot.models.ApiResponse.Success)
        val success = response as com.example.scylier.istudyspot.models.ApiResponse.Success
        assertNotNull(success.data.list)
        assertTrue(success.data.total >= 0)
    }

    @Test
    fun testGetUserOrders_withStatusFilter() = runBlocking {
        val response = apiManager.getUserOrders(status = "pending")

        assertTrue(response is com.example.scylier.istudyspot.models.ApiResponse.Success)
    }

    @Test
    fun testGetOrderDetail_success() = runBlocking {
        val response = apiManager.getOrderDetail(1L)

        assertTrue(response is com.example.scylier.istudyspot.models.ApiResponse.Success)
        val success = response as com.example.scylier.istudyspot.models.ApiResponse.Success
        assertEquals(1L, success.data.id)
        assertNotNull(success.data.seatId)
        assertNotNull(success.data.studyRoomName)
        assertNotNull(success.data.status)
    }

    @Test
    fun testCancelOrder_success() = runBlocking {
        val response = apiManager.cancelOrder(1L)

        assertTrue(response is com.example.scylier.istudyspot.models.ApiResponse.Success)
        val success = response as com.example.scylier.istudyspot.models.ApiResponse.Success
        assertEquals(1L, success.data.id)
        assertEquals("cancelled", success.data.status)
    }

    @Test
    fun testCheckin_success() = runBlocking {
        val response = apiManager.checkin(1L, 1L)

        assertTrue(response is com.example.scylier.istudyspot.models.ApiResponse.Success)
        val success = response as com.example.scylier.istudyspot.models.ApiResponse.Success
        assertEquals(1L, success.data.id)
        assertNotNull(success.data.checkinTime)
        assertEquals("in_use", success.data.status)
    }

    @Test
    fun testCheckout_success() = runBlocking {
        val response = apiManager.checkout(1L)

        assertTrue(response is com.example.scylier.istudyspot.models.ApiResponse.Success)
        val success = response as com.example.scylier.istudyspot.models.ApiResponse.Success
        assertEquals(1L, success.data.id)
        assertNotNull(success.data.checkoutTime)
        assertTrue(success.data.actualDuration > 0)
        assertTrue(success.data.actualPrice > 0)
        assertEquals("completed", success.data.status)
    }

    @Test
    fun testGetUserInfo_success() = runBlocking {
        val response = apiManager.getUserInfo()

        assertTrue(response is com.example.scylier.istudyspot.models.ApiResponse.Success)
        val success = response as com.example.scylier.istudyspot.models.ApiResponse.Success
        assertNotNull(success.data.id)
        assertNotNull(success.data.username)
        assertNotNull(success.data.nickname)
    }

    @Test
    fun testUpdateUserInfo_success() = runBlocking {
        val response = apiManager.updateUserInfo(
            nickname = "新昵称",
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
    fun testUpdateUserInfo_partialUpdate() = runBlocking {
        val response = apiManager.updateUserInfo(nickname = "只更新昵称")

        assertTrue(response is com.example.scylier.istudyspot.models.ApiResponse.Success)
        val success = response as com.example.scylier.istudyspot.models.ApiResponse.Success
        assertEquals("只更新昵称", success.data.nickname)
    }

    @Test
    fun testChangePassword_success() = runBlocking {
        val response = apiManager.changePassword("oldPassword", "newPassword")

        assertTrue(response is com.example.scylier.istudyspot.models.ApiResponse.Success)
        val success = response as com.example.scylier.istudyspot.models.ApiResponse.Success
        assertEquals(200, success.code)
        assertEquals("密码修改成功", success.message)
    }

    @Test
    fun testCreatePayment_success() = runBlocking {
        val response = apiManager.createPayment(
            orderId = 1L,
            amount = 20.0,
            paymentMethod = "wechat"
        )

        assertTrue(response is com.example.scylier.istudyspot.models.ApiResponse.Success)
        val success = response as com.example.scylier.istudyspot.models.ApiResponse.Success
        assertEquals(201, success.code)
        assertNotNull(success.data.id)
        assertEquals(1L, success.data.orderId)
        assertEquals(20.0, success.data.amount, 0.01)
        assertEquals("wechat", success.data.paymentMethod)
    }

    @Test
    fun testGetPaymentStatus_success() = runBlocking {
        val response = apiManager.getPaymentStatus(1L)

        assertTrue(response is com.example.scylier.istudyspot.models.ApiResponse.Success)
        val success = response as com.example.scylier.istudyspot.models.ApiResponse.Success
        assertEquals(1L, success.data.id)
        assertNotNull(success.data.status)
        assertTrue(success.data.amount > 0)
    }

    @Test
    fun testGetStudyRoomStatistics_success() = runBlocking {
        val response = apiManager.getStudyRoomStatistics(
            id = 1L,
            startDate = "2026-10-01",
            endDate = "2026-10-07"
        )

        assertTrue(response is com.example.scylier.istudyspot.models.ApiResponse.Success)
        val success = response as com.example.scylier.istudyspot.models.ApiResponse.Success
        assertEquals(1L, success.data.studyRoomId)
        assertTrue(success.data.totalSeats > 0)
        assertTrue(success.data.avgOccupancyRate >= 0)
        assertTrue(success.data.totalBookings >= 0)
        assertTrue(success.data.dailyData.isNotEmpty())
    }

    @Test
    fun testApiManager_withToken() = runBlocking {
        ApiClient.currentToken = "test_token"
        val apiManagerWithToken = ApiManager()
        val response = apiManagerWithToken.getStudyRooms()

        assertTrue(response is com.example.scylier.istudyspot.models.ApiResponse.Success)
        ApiClient.currentToken = null
    }
}
