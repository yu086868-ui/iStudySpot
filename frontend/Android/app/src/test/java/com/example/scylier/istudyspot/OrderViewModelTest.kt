package com.example.scylier.istudyspot

import com.example.scylier.istudyspot.models.ApiResponse
import com.example.scylier.istudyspot.models.order.*
import com.example.scylier.istudyspot.repository.MainRepository
import com.example.scylier.istudyspot.viewmodel.OrderViewModel
import com.example.scylier.istudyspot.viewmodel.OrderListUiState
import com.example.scylier.istudyspot.viewmodel.OrderDetailUiState
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OrderViewModelTest {

    private lateinit var viewModel: OrderViewModel
    private val testDispatcher = UnconfinedTestDispatcher()
    private val mockRepository = mockk<MainRepository>()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = OrderViewModel(mockRepository)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testInitialState() {
        assertTrue(viewModel.orderListState.value.isLoading)
        assertTrue(viewModel.orderDetailState.value.isLoading)
        assertTrue(viewModel.orderListState.value.orders.isEmpty())
        assertNull(viewModel.orderListState.value.error)
        assertNull(viewModel.orderDetailState.value.order)
        assertNull(viewModel.orderDetailState.value.error)
        assertNull(viewModel.orderDetailState.value.actionSuccess)
    }

    @Test
    fun testPayOrder_success() = runTest {
        coEvery { mockRepository.payOrder(1L) } returns
            ApiResponse.Success(200, "支付成功", mapOf("orderId" to 1L, "status" to "paid", "paymentStatus" to "success"))
        coEvery { mockRepository.getOrderDetail(1L) } returns
            ApiResponse.Success(200, "获取成功", OrderDetail(id = 1L, seatId = 1L, userId = 1L, studyRoomName = "自习室1", seatPosition = "1-1", startTime = "2026-10-01T10:00:00", endTime = "2026-10-01T12:00:00", totalPrice = 20.0, status = "paid", createdAt = "2026-10-01T09:00:00"))

        viewModel.payOrder(1L)

        assertEquals("支付成功", viewModel.orderDetailState.value.actionSuccess)
    }

    @Test
    fun testPayOrder_error() = runTest {
        coEvery { mockRepository.payOrder(1L) } returns
            ApiResponse.Error(400, "订单状态不正确")

        viewModel.payOrder(1L)

        assertNull(viewModel.orderDetailState.value.actionSuccess)
        assertEquals("订单状态不正确", viewModel.orderDetailState.value.error)
    }

    @Test
    fun testRenewOrder_success() = runTest {
        coEvery { mockRepository.renewOrder(1L, "2026-10-01T13:00:00") } returns
            ApiResponse.Success(200, "续时成功", mapOf("orderId" to 1L, "newEndTime" to "2026-10-01T13:00:00", "additionalAmount" to 10.0))
        coEvery { mockRepository.getOrderDetail(1L) } returns
            ApiResponse.Success(200, "获取成功", OrderDetail(id = 1L, seatId = 1L, userId = 1L, studyRoomName = "自习室1", seatPosition = "1-1", startTime = "2026-10-01T10:00:00", endTime = "2026-10-01T13:00:00", totalPrice = 30.0, status = "in_use", createdAt = "2026-10-01T09:00:00"))

        viewModel.renewOrder(1L, "2026-10-01T13:00:00")

        assertEquals("续时成功", viewModel.orderDetailState.value.actionSuccess)
        assertFalse(viewModel.orderDetailState.value.isLoading)
    }

    @Test
    fun testRenewOrder_error() = runTest {
        coEvery { mockRepository.renewOrder(1L, any()) } returns
            ApiResponse.Error(400, "续时失败")

        viewModel.renewOrder(1L, "2026-10-01T13:00:00")

        assertNull(viewModel.orderDetailState.value.actionSuccess)
        assertEquals("续时失败", viewModel.orderDetailState.value.error)
        assertFalse(viewModel.orderDetailState.value.isLoading)
    }

    @Test
    fun testLoadOrders_success() = runTest {
        val orders = listOf(
            OrderItem(id = 1L, seatId = 1L, studyRoomName = "自习室1", seatPosition = "1-1", startTime = "2026-10-01T10:00:00", endTime = "2026-10-01T12:00:00", totalPrice = 20.0, status = "pending", createdAt = "2026-10-01T09:00:00"),
            OrderItem(id = 2L, seatId = 2L, studyRoomName = "自习室2", seatPosition = "2-3", startTime = "2026-10-02T14:00:00", endTime = "2026-10-02T16:00:00", totalPrice = 20.0, status = "paid", createdAt = "2026-10-02T13:00:00")
        )
        coEvery { mockRepository.getUserOrders(any(), any(), any(), any()) } returns
            ApiResponse.Success(200, "获取成功", OrderListResponse(total = 2, list = orders))

        viewModel.loadOrders()

        assertFalse(viewModel.orderListState.value.isLoading)
        assertEquals(2, viewModel.orderListState.value.orders.size)
        assertNull(viewModel.orderListState.value.error)
    }

    @Test
    fun testLoadOrders_error() = runTest {
        coEvery { mockRepository.getUserOrders(any(), any(), any(), any()) } returns
            ApiResponse.Error(500, "网络错误")

        viewModel.loadOrders()

        assertFalse(viewModel.orderListState.value.isLoading)
        assertTrue(viewModel.orderListState.value.orders.isEmpty())
        assertEquals("网络错误", viewModel.orderListState.value.error)
    }

    @Test
    fun testLoadOrderDetail_success() = runTest {
        val detail = OrderDetail(id = 1L, seatId = 1L, userId = 1L, studyRoomName = "自习室1", seatPosition = "1-1", startTime = "2026-10-01T10:00:00", endTime = "2026-10-01T12:00:00", totalPrice = 20.0, status = "paid", createdAt = "2026-10-01T09:00:00")
        coEvery { mockRepository.getOrderDetail(1L) } returns
            ApiResponse.Success(200, "获取成功", detail)

        viewModel.loadOrderDetail(1L)

        assertFalse(viewModel.orderDetailState.value.isLoading)
        assertNotNull(viewModel.orderDetailState.value.order)
        assertEquals(1L, viewModel.orderDetailState.value.order?.id)
        assertEquals("paid", viewModel.orderDetailState.value.order?.status)
    }

    @Test
    fun testLoadOrderDetail_error() = runTest {
        coEvery { mockRepository.getOrderDetail(999L) } returns
            ApiResponse.Error(404, "订单不存在")

        viewModel.loadOrderDetail(999L)

        assertFalse(viewModel.orderDetailState.value.isLoading)
        assertNull(viewModel.orderDetailState.value.order)
        assertEquals("订单不存在", viewModel.orderDetailState.value.error)
    }

    @Test
    fun testCancelOrder_success() = runTest {
        coEvery { mockRepository.cancelOrder(1L) } returns
            ApiResponse.Success(200, "取消成功", CancelOrderResponse(id = 1L, status = "cancelled"))
        coEvery { mockRepository.getOrderDetail(1L) } returns
            ApiResponse.Success(200, "获取成功", OrderDetail(id = 1L, seatId = 1L, userId = 1L, studyRoomName = "自习室1", seatPosition = "1-1", startTime = "2026-10-01T10:00:00", endTime = "2026-10-01T12:00:00", totalPrice = 20.0, status = "cancelled", createdAt = "2026-10-01T09:00:00"))

        viewModel.cancelOrder(1L)

        assertEquals("取消成功", viewModel.orderDetailState.value.actionSuccess)
    }

    @Test
    fun testCancelOrder_error() = runTest {
        coEvery { mockRepository.cancelOrder(1L) } returns
            ApiResponse.Error(400, "无法取消")

        viewModel.cancelOrder(1L)

        assertEquals("无法取消", viewModel.orderDetailState.value.error)
    }

    @Test
    fun testCheckin_success() = runTest {
        coEvery { mockRepository.checkin(1L, 1L) } returns
            ApiResponse.Success(200, "签到成功", CheckinResponse(id = 1L, checkinTime = "2026-10-01T10:00:00", status = "in_use"))
        coEvery { mockRepository.getOrderDetail(1L) } returns
            ApiResponse.Success(200, "获取成功", OrderDetail(id = 1L, seatId = 1L, userId = 1L, studyRoomName = "自习室1", seatPosition = "1-1", startTime = "2026-10-01T10:00:00", endTime = "2026-10-01T12:00:00", totalPrice = 20.0, status = "in_use", createdAt = "2026-10-01T09:00:00"))

        viewModel.checkin(1L)

        assertEquals("签到成功", viewModel.orderDetailState.value.actionSuccess)
    }

    @Test
    fun testCheckout_success() = runTest {
        coEvery { mockRepository.checkout(1L) } returns
            ApiResponse.Success(200, "签退成功", CheckoutResponse(id = 1L, checkoutTime = "2026-10-01T12:00:00", actualDuration = 120, actualPrice = 20.0, status = "completed"))
        coEvery { mockRepository.getOrderDetail(1L) } returns
            ApiResponse.Success(200, "获取成功", OrderDetail(id = 1L, seatId = 1L, userId = 1L, studyRoomName = "自习室1", seatPosition = "1-1", startTime = "2026-10-01T10:00:00", endTime = "2026-10-01T12:00:00", totalPrice = 20.0, status = "completed", createdAt = "2026-10-01T09:00:00"))

        viewModel.checkout(1L)

        assertEquals("签退成功", viewModel.orderDetailState.value.actionSuccess)
    }

    @Test
    fun testClearActionSuccess() = runTest {
        coEvery { mockRepository.payOrder(1L) } returns
            ApiResponse.Success(200, "支付成功", mapOf("orderId" to 1L, "status" to "paid"))
        coEvery { mockRepository.getOrderDetail(1L) } returns
            ApiResponse.Success(200, "获取成功", OrderDetail(id = 1L, seatId = 1L, userId = 1L, studyRoomName = "自习室1", seatPosition = "1-1", startTime = "2026-10-01T10:00:00", endTime = "2026-10-01T12:00:00", totalPrice = 20.0, status = "paid", createdAt = "2026-10-01T09:00:00"))

        viewModel.payOrder(1L)
        assertNotNull(viewModel.orderDetailState.value.actionSuccess)

        viewModel.clearActionSuccess()
        assertNull(viewModel.orderDetailState.value.actionSuccess)
        assertNull(viewModel.orderDetailState.value.error)
    }

    @Test
    fun testOrderListUiState_dataClass() {
        val state = OrderListUiState(
            orders = emptyList(),
            isLoading = false,
            error = "测试错误"
        )
        assertTrue(state.orders.isEmpty())
        assertFalse(state.isLoading)
        assertEquals("测试错误", state.error)
    }

    @Test
    fun testOrderDetailUiState_dataClass() {
        val state = OrderDetailUiState(
            isLoading = false,
            error = null,
            actionSuccess = "操作成功"
        )
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertEquals("操作成功", state.actionSuccess)
    }
}
