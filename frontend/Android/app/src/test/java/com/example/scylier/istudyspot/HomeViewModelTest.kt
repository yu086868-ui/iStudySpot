package com.example.scylier.istudyspot

import com.example.scylier.istudyspot.viewmodel.HomeViewModel
import com.example.scylier.istudyspot.viewmodel.HomeUiState
import com.example.scylier.istudyspot.models.ApiResponse
import com.example.scylier.istudyspot.repository.MainRepository
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
class HomeViewModelTest {

    private lateinit var viewModel: HomeViewModel
    private val testDispatcher = UnconfinedTestDispatcher()
    private val mockRepository = mockk<MainRepository>()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        coEvery { mockRepository.getUserOrders(any(), any(), any(), any()) } returns ApiResponse.Error(500, "Test error")
        coEvery { mockRepository.getCheckinRecords(any(), any(), any(), any()) } returns ApiResponse.Error(500, "Test error")
        viewModel = HomeViewModel(mockRepository)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testViewModel_initialState_isLoading() {
        assertTrue(viewModel.state.value.isLoading)
    }

    @Test
    fun testViewModel_initialState_defaults() {
        val state = viewModel.state.value
        assertEquals(0, state.todayBookings)
        assertEquals("0h", state.studyHours)
        assertEquals("0天", state.streakDays)
        assertEquals("", state.greeting)
        assertEquals("", state.motivationalQuote)
    }

    @Test
    fun testViewModel_loadHomeData_fallsBackToMock() = runTest {
        viewModel.loadHomeData()
        val state = viewModel.state.value
        assertFalse(state.isLoading)
    }

    @Test
    fun testViewModel_loadHomeData_setsGreeting() = runTest {
        viewModel.loadHomeData()
        val state = viewModel.state.value
        assertTrue(state.greeting.isNotEmpty())
        assertTrue(
            state.greeting == "早上好" ||
            state.greeting == "中午好" ||
            state.greeting == "下午好" ||
            state.greeting == "晚上好"
        )
    }

    @Test
    fun testViewModel_loadHomeData_setsMotivationalQuote() = runTest {
        viewModel.loadHomeData()
        val state = viewModel.state.value
        assertTrue(state.motivationalQuote.isNotEmpty())
    }

    @Test
    fun testViewModel_loadHomeData_notLoadingAfterComplete() = runTest {
        viewModel.loadHomeData()
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun testUiState_dataClass() {
        val state = HomeUiState(
            todayBookings = 3,
            studyHours = "2.5h",
            streakDays = "7天",
            greeting = "早上好",
            motivationalQuote = "学如逆水行舟，不进则退",
            isLoading = false
        )
        assertEquals(3, state.todayBookings)
        assertEquals("2.5h", state.studyHours)
        assertEquals("7天", state.streakDays)
        assertEquals("早上好", state.greeting)
        assertEquals("学如逆水行舟，不进则退", state.motivationalQuote)
        assertFalse(state.isLoading)
    }

    @Test
    fun testUiState_defaultValues() {
        val state = HomeUiState()
        assertEquals(0, state.todayBookings)
        assertEquals("0h", state.studyHours)
        assertEquals("0天", state.streakDays)
        assertEquals("", state.greeting)
        assertEquals("", state.motivationalQuote)
        assertTrue(state.isLoading)
    }

    @Test
    fun testViewModel_greetingMorning() = runTest {
        viewModel.loadHomeData()
        val greeting = viewModel.state.value.greeting
        assertNotNull(greeting)
        assertTrue(greeting.isNotEmpty())
    }

    @Test
    fun testViewModel_quotesAreNotEmpty() = runTest {
        viewModel.loadHomeData()
        val quote = viewModel.state.value.motivationalQuote
        assertTrue(quote.isNotEmpty())
    }
}
