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
class MainRepositoryTest {

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
    fun testRepository_getStudyRooms_success() = runBlocking {
        val response = repository.getStudyRooms()

        assertTrue(response is com.example.scylier.istudyspot.models.ApiResponse.Success)
        val success = response as com.example.scylier.istudyspot.models.ApiResponse.Success
        assertNotNull(success.data.list)
    }

    @Test
    fun testRepository_getStudyRoomDetail_success() = runBlocking {
        val response = repository.getStudyRoomDetail("1")

        assertTrue(response is com.example.scylier.istudyspot.models.ApiResponse.Success)
        val success = response as com.example.scylier.istudyspot.models.ApiResponse.Success
        assertEquals("1", success.data.id)
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
            studyRoomId = "1",
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
    fun testRepository_getUserOrders_success() = runBlocking {
        val response = repository.getUserOrders(token = "test_token")

        assertTrue(response is com.example.scylier.istudyspot.models.ApiResponse.Success)
        val success = response as com.example.scylier.istudyspot.models.ApiResponse.Success
        assertNotNull(success.data.list)
    }

    @Test
    fun testRepository_getUserOrders_withStatusFilter() = runBlocking {
        val response = repository.getUserOrders(status = "pending", token = "test_token")

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
    fun testRepository_updateUserInfo_success() = runBlocking {
        val response = repository.updateUserInfo(
            nickname = "新昵称",
            token = "test_token"
        )

        assertTrue(response is com.example.scylier.istudyspot.models.ApiResponse.Success)
        val success = response as com.example.scylier.istudyspot.models.ApiResponse.Success
        assertEquals("新昵称", success.data.nickname)
    }

    @Test
    fun testRepository_changePassword_success() = runBlocking {
        val response = repository.changePassword("oldPass", "newPass", token = "test_token")

        assertTrue(response is com.example.scylier.istudyspot.models.ApiResponse.Success)
    }

    @Test
    fun testRepository_createPayment_success() = runBlocking {
        val response = repository.createPayment(
            orderId = "order1",
            amount = 20.0,
            paymentMethod = "wechat",
            token = "test_token"
        )

        assertTrue(response is com.example.scylier.istudyspot.models.ApiResponse.Success)
        val success = response as com.example.scylier.istudyspot.models.ApiResponse.Success
        assertEquals(20.0, success.data.amount, 0.01)
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
}
