package com.example.scylier.istudyspot

import com.example.scylier.istudyspot.models.ApiResponse
import com.example.scylier.istudyspot.models.order.OrderResponse
import com.example.scylier.istudyspot.repository.MainRepository
import com.example.scylier.istudyspot.viewmodel.BookingUiState
import com.example.scylier.istudyspot.viewmodel.BookingViewModel
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BookingViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val repository = mockk<MainRepository>()
    private lateinit var viewModel: BookingViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = BookingViewModel(repository)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialStateShouldBeIdle() {
        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertFalse(state.isSuccess)
        assertNull(state.orderId)
        assertNull(state.error)
    }

    @Test
    fun createOrderSuccessShouldExposeOrderId() = runTest {
        coEvery {
            repository.createOrder(1L, 2L, "2026-06-17 10:00:00", "2026-06-17 12:00:00", "normal")
        } returns ApiResponse.Success(201, "created", OrderResponse(id = 99L, seatId = 2L, status = "pending"))

        viewModel.createOrder(1L, 2L, "2026-06-17 10:00:00", "2026-06-17 12:00:00", "normal")

        val state = viewModel.state.value
        assertTrue(state.isSuccess)
        assertEquals(99L, state.orderId)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun createOrderErrorShouldExposeMessage() = runTest {
        coEvery { repository.createOrder(any(), any(), any(), any(), any()) } returns
            ApiResponse.Error(400, "Bad request")

        viewModel.createOrder(1L, 2L, "start", "end", "normal")

        val state = viewModel.state.value
        assertFalse(state.isSuccess)
        assertEquals("Bad request", state.error)
        assertFalse(state.isLoading)
    }

    @Test
    fun resetStateShouldClearPreviousResult() = runTest {
        coEvery { repository.createOrder(any(), any(), any(), any(), any()) } returns
            ApiResponse.Success(201, "created", OrderResponse(id = 3L))

        viewModel.createOrder(1L, 2L, "start", "end", "normal")
        viewModel.resetState()

        assertEquals(BookingUiState(), viewModel.state.value)
    }
}
