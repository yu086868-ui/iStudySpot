package com.example.scylier.istudyspot

import com.example.scylier.istudyspot.models.ApiResponse
import com.example.scylier.istudyspot.repository.MainRepository
import com.example.scylier.istudyspot.viewmodel.StudyRecordUiState
import com.example.scylier.istudyspot.viewmodel.StudyRecordViewModel
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StudyRecordViewModelTest {

    private lateinit var viewModel: StudyRecordViewModel
    private val testDispatcher = UnconfinedTestDispatcher()
    private val mockRepository = mockk<MainRepository>()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        coEvery { mockRepository.getCheckinRecords(any(), any(), any(), any()) } returns
            ApiResponse.Error(500, "Test error")
        coEvery { mockRepository.getUserOrders(any(), any(), any(), any(), any()) } returns
            ApiResponse.Error(500, "Test error")
        viewModel = StudyRecordViewModel(mockRepository)
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
        assertEquals(0, state.weekStudyHours)
        assertEquals(0, state.monthStudyHours)
        assertEquals(0, state.totalStudyHours)
        assertEquals(0, state.totalBookings)
        assertEquals(0, state.streakDays)
        assertEquals(0.0, state.avgStudyDuration, 0.01)
        assertEquals("", state.favoriteSeat)
        assertEquals("", state.peakTime)
    }

    @Test
    fun testViewModel_loadStudyRecords_fallsBackToMock() = runTest {
        viewModel.loadStudyRecords()
        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertTrue(state.weekStudyHours > 0)
        assertTrue(state.monthStudyHours > 0)
        assertTrue(state.totalStudyHours > 0)
        assertTrue(state.totalBookings > 0)
        assertTrue(state.streakDays > 0)
        assertTrue(state.avgStudyDuration > 0)
        assertTrue(state.favoriteSeat.isNotEmpty())
        assertTrue(state.peakTime.isNotEmpty())
    }

    @Test
    fun testViewModel_mockState_monthGreaterThanWeek() = runTest {
        viewModel.loadStudyRecords()
        val state = viewModel.state.value
        assertTrue(state.monthStudyHours > state.weekStudyHours)
    }

    @Test
    fun testViewModel_mockState_totalGreaterThanMonth() = runTest {
        viewModel.loadStudyRecords()
        val state = viewModel.state.value
        assertTrue(state.totalStudyHours > state.monthStudyHours)
    }

    @Test
    fun testViewModel_mockState_favoriteSeatUsesSeatNumber() = runTest {
        viewModel.loadStudyRecords()
        val state = viewModel.state.value
        assertEquals("A12", state.favoriteSeat)
    }

    @Test
    fun testViewModel_mockState_peakTimeContainsColon() = runTest {
        viewModel.loadStudyRecords()
        val state = viewModel.state.value
        assertTrue(state.peakTime.contains(":"))
    }

    @Test
    fun testUiState_dataClass() {
        val state = StudyRecordUiState(
            weekStudyHours = 10,
            monthStudyHours = 40,
            totalStudyHours = 100,
            totalBookings = 20,
            streakDays = 5,
            avgStudyDuration = 2.5,
            favoriteSeat = "A12",
            peakTime = "14:00-17:00",
            isLoading = false
        )
        assertEquals(10, state.weekStudyHours)
        assertEquals(40, state.monthStudyHours)
        assertEquals(100, state.totalStudyHours)
        assertEquals(20, state.totalBookings)
        assertEquals(5, state.streakDays)
        assertEquals(2.5, state.avgStudyDuration, 0.01)
        assertEquals("A12", state.favoriteSeat)
        assertEquals("14:00-17:00", state.peakTime)
        assertFalse(state.isLoading)
    }
}
