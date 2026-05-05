package com.example.scylier.istudyspot

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.scylier.istudyspot.repository.MainRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], manifest = Config.NONE)
class MainRepositoryComprehensiveTest {

    private lateinit var repository: MainRepository
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        repository = MainRepository(context)
    }

    @Test
    fun testRepository_login_success() = runBlocking {
        val response = repository.login("testuser", "password123")

        assertTrue(response is com.example.scylier.istudyspot.models.ApiResponse.Success)
        val success = response as com.example.scylier.istudyspot.models.ApiResponse.Success
        assertNotNull(success.data.token)
        assertEquals("testuser", success.data.user.username)
    }

    @Test
    fun testRepository_register_success() = runBlocking {
        val response = repository.register("newuser", "password123", "新用户")

        assertTrue(response is com.example.scylier.istudyspot.models.ApiResponse.Success)
        val success = response as com.example.scylier.istudyspot.models.ApiResponse.Success
        assertEquals("newuser", success.data.user.username)
        assertEquals("新用户", success.data.user.nickname)
    }

    @Test
    fun testRepository_refreshToken_success() = runBlocking {
        val response = repository.refreshToken("test_token")

        assertTrue(response is com.example.scylier.istudyspot.models.ApiResponse.Success)
    }

    @Test
    fun testRepository_getStudyRooms_defaultParams() = runBlocking {
        val response = repository.getStudyRooms()

        assertTrue(response is com.example.scylier.istudyspot.models.ApiResponse.Success)
        val success = response as com.example.scylier.istudyspot.models.ApiResponse.Success
        assertNotNull(success.data.list)
    }

    @Test
    fun testRepository_getStudyRooms_customParams() = runBlocking {
        val response = repository.getStudyRooms(page = 2, size = 5)

        assertTrue(response is com.example.scylier.istudyspot.models.ApiResponse.Success)
    }

    @Test
    fun testRepository_getStudyRoomDetail_success() = runBlocking {
        val response = repository.getStudyRoomDetail("1")

        assertTrue(response is com.example.scylier.istudyspot.models.ApiResponse.Success)
        val success = response as com.example.scylier.istudyspot.models.ApiResponse.Success
        assertEquals("1", success.data.id)
    }

    @Test
    fun testRepository_getStudyRoomDetail_differentIds() = runBlocking {
        val response1 = repository.getStudyRoomDetail("1")
        val response2 = repository.getStudyRoomDetail("2")

        assertTrue(response1 is com.example.scylier.istudyspot.models.ApiResponse.Success)
        assertTrue(response2 is com.example.scylier.istudyspot.models.ApiResponse.Success)

        val success1 = response1 as com.example.scylier.istudyspot.models.ApiResponse.Success
        val success2 = response2 as com.example.scylier.istudyspot.models.ApiResponse.Success

        assertEquals("1", success1.data.id)
        assertEquals("2", success2.data.id)
    }

    @Test
    fun testRepository_getStudyRoomSeats_success() = runBlocking {
        val response = repository.getStudyRoomSeats("1")

        assertTrue(response is com.example.scylier.istudyspot.models.ApiResponse.Success)
        val success = response as com.example.scylier.istudyspot.models.ApiResponse.Success
        assertEquals("1", success.data.studyRoomId)
        assertTrue(success.data.seats.isNotEmpty())
    }

    @Test
    fun testRepository_getSeatDetail_success() = runBlocking {
        val response = repository.getSeatDetail("seat_1_1")

        assertTrue(response is com.example.scylier.istudyspot.models.ApiResponse.Success)
    }

    @Test
    fun testRepository_createOrder_success() = runBlocking {
        val response = repository.createOrder(
            seatId = "seat_1_1",
            startTime = "2026-10-01T10:00:00",
            endTime = "2026-10-01T12:00:00",
            bookingType = "hourly",
            token = "test_token"
        )

        assertTrue(response is com.example.scylier.istudyspot.models.ApiResponse.Success)
        val success = response as com.example.scylier.istudyspot.models.ApiResponse.Success
        assertNotNull(success.data.id)
        assertEquals("pending", success.data.status)
    }

    @Test
    fun testRepository_getUserOrders_defaultParams() = runBlocking {
        val response = repository.getUserOrders(token = "test_token")

        assertTrue(response is com.example.scylier.istudyspot.models.ApiResponse.Success)
        val success = response as com.example.scylier.istudyspot.models.ApiResponse.Success
        assertNotNull(success.data.list)
    }

    @Test
    fun testRepository_getUserOrders_withStatus() = runBlocking {
        val response = repository.getUserOrders(status = "pending", token = "test_token")

        assertTrue(response is com.example.scylier.istudyspot.models.ApiResponse.Success)
    }

    @Test
    fun testRepository_getUserOrders_withPagination() = runBlocking {
        val response = repository.getUserOrders(page = 1, size = 5, token = "test_token")

        assertTrue(response is com.example.scylier.istudyspot.models.ApiResponse.Success)
    }

    @Test
    fun testRepository_getOrderDetail_success() = runBlocking {
        val response = repository.getOrderDetail("order1", token = "test_token")

        assertTrue(response is com.example.scylier.istudyspot.models.ApiResponse.Success)
        val success = response as com.example.scylier.istudyspot.models.ApiResponse.Success
        assertEquals("order1", success.data.id)
    }

    @Test
    fun testRepository_cancelOrder_success() = runBlocking {
        val response = repository.cancelOrder("order1", token = "test_token")

        assertTrue(response is com.example.scylier.istudyspot.models.ApiResponse.Success)
        val success = response as com.example.scylier.istudyspot.models.ApiResponse.Success
        assertEquals("cancelled", success.data.status)
    }

    @Test
    fun testRepository_checkin_success() = runBlocking {
        val response = repository.checkin("order1", "123456", token = "test_token")

        assertTrue(response is com.example.scylier.istudyspot.models.ApiResponse.Success)
        val success = response as com.example.scylier.istudyspot.models.ApiResponse.Success
        assertEquals("in_use", success.data.status)
    }

    @Test
    fun testRepository_checkout_success() = runBlocking {
        val response = repository.checkout("order1", token = "test_token")

        assertTrue(response is com.example.scylier.istudyspot.models.ApiResponse.Success)
        val success = response as com.example.scylier.istudyspot.models.ApiResponse.Success
        assertEquals("completed", success.data.status)
    }

    @Test
    fun testRepository_getUserInfo_success() = runBlocking {
        val response = repository.getUserInfo(token = "test_token")

        assertTrue(response is com.example.scylier.istudyspot.models.ApiResponse.Success)
        val success = response as com.example.scylier.istudyspot.models.ApiResponse.Success
        assertNotNull(success.data.username)
    }

    @Test
    fun testRepository_updateUserInfo_nickname() = runBlocking {
        val response = repository.updateUserInfo(nickname = "新昵称", token = "test_token")

        assertTrue(response is com.example.scylier.istudyspot.models.ApiResponse.Success)
        val success = response as com.example.scylier.istudyspot.models.ApiResponse.Success
        assertEquals("新昵称", success.data.nickname)
    }

    @Test
    fun testRepository_updateUserInfo_phone() = runBlocking {
        val response = repository.updateUserInfo(phone = "13900139000", token = "test_token")

        assertTrue(response is com.example.scylier.istudyspot.models.ApiResponse.Success)
        val success = response as com.example.scylier.istudyspot.models.ApiResponse.Success
        assertEquals("13900139000", success.data.phone)
    }

    @Test
    fun testRepository_updateUserInfo_email() = runBlocking {
        val response = repository.updateUserInfo(email = "test@example.com", token = "test_token")

        assertTrue(response is com.example.scylier.istudyspot.models.ApiResponse.Success)
        val success = response as com.example.scylier.istudyspot.models.ApiResponse.Success
        assertEquals("test@example.com", success.data.email)
    }

    @Test
    fun testRepository_updateUserInfo_allFields() = runBlocking {
        val response = repository.updateUserInfo(
            nickname = "完整更新",
            phone = "13800138000",
            email = "full@example.com",
            token = "test_token"
        )

        assertTrue(response is com.example.scylier.istudyspot.models.ApiResponse.Success)
        val success = response as com.example.scylier.istudyspot.models.ApiResponse.Success
        assertEquals("完整更新", success.data.nickname)
        assertEquals("13800138000", success.data.phone)
        assertEquals("full@example.com", success.data.email)
    }

    @Test
    fun testRepository_changePassword_success() = runBlocking {
        val response = repository.changePassword("oldPass", "newPass", token = "test_token")

        assertTrue(response is com.example.scylier.istudyspot.models.ApiResponse.Success)
    }

    @Test
    fun testRepository_createPayment_wechat() = runBlocking {
        val response = repository.createPayment(
            orderId = "order1",
            amount = 20.0,
            paymentMethod = "wechat",
            token = "test_token"
        )

        assertTrue(response is com.example.scylier.istudyspot.models.ApiResponse.Success)
        val success = response as com.example.scylier.istudyspot.models.ApiResponse.Success
        assertEquals(20.0, success.data.amount, 0.01)
        assertEquals("wechat", success.data.paymentMethod)
    }

    @Test
    fun testRepository_createPayment_alipay() = runBlocking {
        val response = repository.createPayment(
            orderId = "order2",
            amount = 30.0,
            paymentMethod = "alipay",
            token = "test_token"
        )

        assertTrue(response is com.example.scylier.istudyspot.models.ApiResponse.Success)
        val success = response as com.example.scylier.istudyspot.models.ApiResponse.Success
        assertEquals(30.0, success.data.amount, 0.01)
        assertEquals("alipay", success.data.paymentMethod)
    }

    @Test
    fun testRepository_getPaymentStatus_success() = runBlocking {
        val response = repository.getPaymentStatus("payment1", token = "test_token")

        assertTrue(response is com.example.scylier.istudyspot.models.ApiResponse.Success)
    }

    @Test
    fun testRepository_getStudyRoomStatistics_success() = runBlocking {
        val response = repository.getStudyRoomStatistics(
            id = "1",
            startDate = "2026-10-01",
            endDate = "2026-10-07"
        )

        assertTrue(response is com.example.scylier.istudyspot.models.ApiResponse.Success)
        val success = response as com.example.scylier.istudyspot.models.ApiResponse.Success
        assertTrue(success.data.totalSeats > 0)
    }

    @Test
    fun testRepository_getStudyRoomStatistics_dailyData() = runBlocking {
        val response = repository.getStudyRoomStatistics(
            id = "1",
            startDate = "2026-10-01",
            endDate = "2026-10-07"
        )

        assertTrue(response is com.example.scylier.istudyspot.models.ApiResponse.Success)
        val success = response as com.example.scylier.istudyspot.models.ApiResponse.Success
        assertTrue(success.data.dailyData.isNotEmpty())
    }

    @Test
    fun testRepository_multipleCalls_consistency() = runBlocking {
        val response1 = repository.getStudyRooms()
        val response2 = repository.getStudyRooms()

        assertTrue(response1 is com.example.scylier.istudyspot.models.ApiResponse.Success)
        assertTrue(response2 is com.example.scylier.istudyspot.models.ApiResponse.Success)
    }
}
