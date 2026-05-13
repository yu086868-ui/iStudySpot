package com.example.scylier.istudyspot

import com.example.scylier.istudyspot.models.ApiResponse
import com.example.scylier.istudyspot.infra.network.ApiManager
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class ApiManagerComprehensiveTest {

    private val apiManager = ApiManager()

    @Test
    fun testLogin_returnsSuccessResponse() = runBlocking {
        val response = apiManager.login("testuser", "password")

        assertTrue(response is ApiResponse.Success)
    }

    @Test
    fun testLogin_responseContainsToken() = runBlocking {
        val response = apiManager.login("testuser", "password") as ApiResponse.Success

        assertNotNull(response.data.token)
        assertTrue(response.data.token.isNotEmpty())
    }

    @Test
    fun testLogin_responseContainsUserInfo() = runBlocking {
        val response = apiManager.login("testuser", "password") as ApiResponse.Success

        assertNotNull(response.data.user)
        assertEquals("testuser", response.data.user.username)
    }

    @Test
    fun testRegister_returnsSuccessResponse() = runBlocking {
        val response = apiManager.register("newuser", "password", "nickname")

        assertTrue(response is ApiResponse.Success)
    }

    @Test
    fun testRegister_responseContainsToken() = runBlocking {
        val response = apiManager.register("newuser", "password", "nickname") as ApiResponse.Success

        assertNotNull(response.data.token)
    }

    @Test
    fun testRegister_responseContainsUserInfo() = runBlocking {
        val response = apiManager.register("newuser", "password", "nickname") as ApiResponse.Success

        assertEquals("newuser", response.data.user.username)
        assertEquals("nickname", response.data.user.nickname)
    }

    @Test
    fun testRefreshToken_returnsSuccessResponse() = runBlocking {
        val response = apiManager.refreshToken("test_refresh_token")

        assertTrue(response is ApiResponse.Success)
    }

    @Test
    fun testRefreshToken_responseContainsToken() = runBlocking {
        val response = apiManager.refreshToken("test_refresh_token") as ApiResponse.Success

        assertNotNull(response.data.token)
    }

    @Test
    fun testGetStudyRooms_returnsSuccessResponse() = runBlocking {
        val response = apiManager.getStudyRooms()

        assertTrue(response is ApiResponse.Success)
    }

    @Test
    fun testGetStudyRooms_responseContainsList() = runBlocking {
        val response = apiManager.getStudyRooms() as ApiResponse.Success

        assertNotNull(response.data.list)
        assertTrue(response.data.list.isNotEmpty())
    }

    @Test
    fun testGetStudyRooms_responseContainsTotal() = runBlocking {
        val response = apiManager.getStudyRooms() as ApiResponse.Success

        assertTrue(response.data.total >= 0)
    }

    @Test
    fun testGetStudyRoomDetail_returnsSuccessResponse() = runBlocking {
        val response = apiManager.getStudyRoomDetail("1")

        assertTrue(response is ApiResponse.Success)
    }

    @Test
    fun testGetStudyRoomDetail_responseContainsCorrectId() = runBlocking {
        val response = apiManager.getStudyRoomDetail("1") as ApiResponse.Success

        assertEquals("1", response.data.id)
    }

    @Test
    fun testGetStudyRoomDetail_responseContainsName() = runBlocking {
        val response = apiManager.getStudyRoomDetail("1") as ApiResponse.Success

        assertNotNull(response.data.name)
    }

    @Test
    fun testGetStudyRoomDetail_responseContainsAddress() = runBlocking {
        val response = apiManager.getStudyRoomDetail("1") as ApiResponse.Success

        assertNotNull(response.data.address)
    }

    @Test
    fun testGetStudyRoomDetail_responseContainsOpeningHours() = runBlocking {
        val response = apiManager.getStudyRoomDetail("1") as ApiResponse.Success

        assertNotNull(response.data.openingHours)
    }

    @Test
    fun testGetStudyRoomSeats_returnsSuccessResponse() = runBlocking {
        val response = apiManager.getStudyRoomSeats("1")

        assertTrue(response is ApiResponse.Success)
    }

    @Test
    fun testGetStudyRoomSeats_responseContainsCorrectStudyRoomId() = runBlocking {
        val response = apiManager.getStudyRoomSeats("1") as ApiResponse.Success

        assertEquals("1", response.data.studyRoomId)
    }

    @Test
    fun testGetStudyRoomSeats_responseContainsRows() = runBlocking {
        val response = apiManager.getStudyRoomSeats("1") as ApiResponse.Success

        assertTrue(response.data.rows > 0)
    }

    @Test
    fun testGetStudyRoomSeats_responseContainsCols() = runBlocking {
        val response = apiManager.getStudyRoomSeats("1") as ApiResponse.Success

        assertTrue(response.data.cols > 0)
    }

    @Test
    fun testGetStudyRoomSeats_responseContainsSeats() = runBlocking {
        val response = apiManager.getStudyRoomSeats("1") as ApiResponse.Success

        assertTrue(response.data.seats.isNotEmpty())
    }

    @Test
    fun testGetSeatDetail_returnsSuccessResponse() = runBlocking {
        val response = apiManager.getSeatDetail("seat_1_1")

        assertTrue(response is ApiResponse.Success)
    }

    @Test
    fun testGetSeatDetail_responseContainsCorrectId() = runBlocking {
        val response = apiManager.getSeatDetail("seat_1_1") as ApiResponse.Success

        assertEquals("seat_1_1", response.data.id)
    }

    @Test
    fun testGetSeatDetail_responseContainsStatus() = runBlocking {
        val response = apiManager.getSeatDetail("seat_1_1") as ApiResponse.Success

        assertNotNull(response.data.status)
    }

    @Test
    fun testGetSeatDetail_responseContainsType() = runBlocking {
        val response = apiManager.getSeatDetail("seat_1_1") as ApiResponse.Success

        assertNotNull(response.data.type)
    }

    @Test
    fun testGetSeatDetail_responseContainsPrice() = runBlocking {
        val response = apiManager.getSeatDetail("seat_1_1") as ApiResponse.Success

        assertTrue(response.data.pricePerHour > 0)
    }

    @Test
    fun testCreateOrder_returnsSuccessResponse() = runBlocking {
        val response = apiManager.createOrder("1", "seat1", "2026-10-01T10:00:00", "2026-10-01T12:00:00", "hourly")

        assertTrue(response is ApiResponse.Success)
    }

    @Test
    fun testCreateOrder_responseContainsId() = runBlocking {
        val response = apiManager.createOrder("1", "seat1", "2026-10-01T10:00:00", "2026-10-01T12:00:00", "hourly") as ApiResponse.Success

        assertNotNull(response.data.id)
    }

    @Test
    fun testCreateOrder_responseContainsCorrectSeatId() = runBlocking {
        val response = apiManager.createOrder("1", "seat1", "2026-10-01T10:00:00", "2026-10-01T12:00:00", "hourly") as ApiResponse.Success

        assertEquals("seat1", response.data.seatId)
    }

    @Test
    fun testCreateOrder_responseContainsStatus() = runBlocking {
        val response = apiManager.createOrder("1", "seat1", "2026-10-01T10:00:00", "2026-10-01T12:00:00", "hourly") as ApiResponse.Success

        assertEquals("pending", response.data.status)
    }

    @Test
    fun testGetUserOrders_returnsSuccessResponse() = runBlocking {
        val response = apiManager.getUserOrders()

        assertTrue(response is ApiResponse.Success)
    }

    @Test
    fun testGetUserOrders_responseContainsList() = runBlocking {
        val response = apiManager.getUserOrders() as ApiResponse.Success

        assertNotNull(response.data.list)
    }

    @Test
    fun testGetUserOrders_responseContainsTotal() = runBlocking {
        val response = apiManager.getUserOrders() as ApiResponse.Success

        assertTrue(response.data.total >= 0)
    }

    @Test
    fun testGetOrderDetail_returnsSuccessResponse() = runBlocking {
        val response = apiManager.getOrderDetail("order1")

        assertTrue(response is ApiResponse.Success)
    }

    @Test
    fun testGetOrderDetail_responseContainsCorrectId() = runBlocking {
        val response = apiManager.getOrderDetail("order1") as ApiResponse.Success

        assertEquals("order1", response.data.id)
    }

    @Test
    fun testGetOrderDetail_responseContainsStudyRoomName() = runBlocking {
        val response = apiManager.getOrderDetail("order1") as ApiResponse.Success

        assertNotNull(response.data.studyRoomName)
    }

    @Test
    fun testGetOrderDetail_responseContainsStatus() = runBlocking {
        val response = apiManager.getOrderDetail("order1") as ApiResponse.Success

        assertNotNull(response.data.status)
    }

    @Test
    fun testCancelOrder_returnsSuccessResponse() = runBlocking {
        val response = apiManager.cancelOrder("order1")

        assertTrue(response is ApiResponse.Success)
    }

    @Test
    fun testCancelOrder_responseContainsCorrectId() = runBlocking {
        val response = apiManager.cancelOrder("order1") as ApiResponse.Success

        assertEquals("order1", response.data.id)
    }

    @Test
    fun testCancelOrder_responseContainsCancelledStatus() = runBlocking {
        val response = apiManager.cancelOrder("order1") as ApiResponse.Success

        assertEquals("cancelled", response.data.status)
    }

    @Test
    fun testCheckin_returnsSuccessResponse() = runBlocking {
        val response = apiManager.checkin("order1", "123456")

        assertTrue(response is ApiResponse.Success)
    }

    @Test
    fun testCheckin_responseContainsCorrectId() = runBlocking {
        val response = apiManager.checkin("order1", "123456") as ApiResponse.Success

        assertEquals("order1", response.data.id)
    }

    @Test
    fun testCheckin_responseContainsCheckinTime() = runBlocking {
        val response = apiManager.checkin("order1", "123456") as ApiResponse.Success

        assertNotNull(response.data.checkinTime)
    }

    @Test
    fun testCheckin_responseContainsInUseStatus() = runBlocking {
        val response = apiManager.checkin("order1", "123456") as ApiResponse.Success

        assertEquals("in_use", response.data.status)
    }

    @Test
    fun testCheckout_returnsSuccessResponse() = runBlocking {
        val response = apiManager.checkout("order1")

        assertTrue(response is ApiResponse.Success)
    }

    @Test
    fun testCheckout_responseContainsCorrectId() = runBlocking {
        val response = apiManager.checkout("order1") as ApiResponse.Success

        assertEquals("order1", response.data.id)
    }

    @Test
    fun testCheckout_responseContainsCheckoutTime() = runBlocking {
        val response = apiManager.checkout("order1") as ApiResponse.Success

        assertNotNull(response.data.checkoutTime)
    }

    @Test
    fun testCheckout_responseContainsActualDuration() = runBlocking {
        val response = apiManager.checkout("order1") as ApiResponse.Success

        assertTrue(response.data.actualDuration > 0)
    }

    @Test
    fun testCheckout_responseContainsActualPrice() = runBlocking {
        val response = apiManager.checkout("order1") as ApiResponse.Success

        assertTrue(response.data.actualPrice > 0)
    }

    @Test
    fun testCheckout_responseContainsCompletedStatus() = runBlocking {
        val response = apiManager.checkout("order1") as ApiResponse.Success

        assertEquals("completed", response.data.status)
    }

    @Test
    fun testGetUserInfo_returnsSuccessResponse() = runBlocking {
        val response = apiManager.getUserInfo()

        assertTrue(response is ApiResponse.Success)
    }

    @Test
    fun testGetUserInfo_responseContainsUsername() = runBlocking {
        val response = apiManager.getUserInfo() as ApiResponse.Success

        assertNotNull(response.data.username)
    }

    @Test
    fun testGetUserInfo_responseContainsNickname() = runBlocking {
        val response = apiManager.getUserInfo() as ApiResponse.Success

        assertNotNull(response.data.nickname)
    }

    @Test
    fun testUpdateUserInfo_returnsSuccessResponse() = runBlocking {
        val response = apiManager.updateUserInfo(nickname = "新昵称")

        assertTrue(response is ApiResponse.Success)
    }

    @Test
    fun testUpdateUserInfo_responseContainsUpdatedNickname() = runBlocking {
        val response = apiManager.updateUserInfo(nickname = "新昵称") as ApiResponse.Success

        assertEquals("新昵称", response.data.nickname)
    }

    @Test
    fun testChangePassword_returnsSuccessResponse() = runBlocking {
        val response = apiManager.changePassword("oldPass", "newPass")

        assertTrue(response is ApiResponse.Success)
    }

    @Test
    fun testCreatePayment_returnsSuccessResponse() = runBlocking {
        val response = apiManager.createPayment("order1", 20.0, "wechat")

        assertTrue(response is ApiResponse.Success)
    }

    @Test
    fun testCreatePayment_responseContainsPaymentId() = runBlocking {
        val response = apiManager.createPayment("order1", 20.0, "wechat") as ApiResponse.Success

        assertNotNull(response.data.paymentId)
    }

    @Test
    fun testCreatePayment_responseContainsCorrectOrderId() = runBlocking {
        val response = apiManager.createPayment("order1", 20.0, "wechat") as ApiResponse.Success

        assertEquals("order1", response.data.orderId)
    }

    @Test
    fun testCreatePayment_responseContainsCorrectAmount() = runBlocking {
        val response = apiManager.createPayment("order1", 20.0, "wechat") as ApiResponse.Success

        assertEquals(20.0, response.data.amount, 0.01)
    }

    @Test
    fun testCreatePayment_responseContainsCorrectPaymentMethod() = runBlocking {
        val response = apiManager.createPayment("order1", 20.0, "wechat") as ApiResponse.Success

        assertEquals("wechat", response.data.paymentMethod)
    }

    @Test
    fun testGetPaymentStatus_returnsSuccessResponse() = runBlocking {
        val response = apiManager.getPaymentStatus("payment1")

        assertTrue(response is ApiResponse.Success)
    }

    @Test
    fun testGetPaymentStatus_responseContainsCorrectId() = runBlocking {
        val response = apiManager.getPaymentStatus("payment1") as ApiResponse.Success

        assertEquals("payment1", response.data.id)
    }

    @Test
    fun testGetPaymentStatus_responseContainsStatus() = runBlocking {
        val response = apiManager.getPaymentStatus("payment1") as ApiResponse.Success

        assertNotNull(response.data.status)
    }

    @Test
    fun testGetStudyRoomStatistics_returnsSuccessResponse() = runBlocking {
        val response = apiManager.getStudyRoomStatistics("1", "2026-10-01", "2026-10-07")

        assertTrue(response is ApiResponse.Success)
    }

    @Test
    fun testGetStudyRoomStatistics_responseContainsCorrectStudyRoomId() = runBlocking {
        val response = apiManager.getStudyRoomStatistics("1", "2026-10-01", "2026-10-07") as ApiResponse.Success

        assertEquals("1", response.data.studyRoomId)
    }

    @Test
    fun testGetStudyRoomStatistics_responseContainsTotalSeats() = runBlocking {
        val response = apiManager.getStudyRoomStatistics("1", "2026-10-01", "2026-10-07") as ApiResponse.Success

        assertTrue(response.data.totalSeats > 0)
    }

    @Test
    fun testGetStudyRoomStatistics_responseContainsAvgOccupancyRate() = runBlocking {
        val response = apiManager.getStudyRoomStatistics("1", "2026-10-01", "2026-10-07") as ApiResponse.Success

        assertTrue(response.data.avgOccupancyRate >= 0)
    }

    @Test
    fun testGetStudyRoomStatistics_responseContainsTotalBookings() = runBlocking {
        val response = apiManager.getStudyRoomStatistics("1", "2026-10-01", "2026-10-07") as ApiResponse.Success

        assertTrue(response.data.totalBookings >= 0)
    }

    @Test
    fun testGetStudyRoomStatistics_responseContainsDailyData() = runBlocking {
        val response = apiManager.getStudyRoomStatistics("1", "2026-10-01", "2026-10-07") as ApiResponse.Success

        assertTrue(response.data.dailyData.isNotEmpty())
    }
}
