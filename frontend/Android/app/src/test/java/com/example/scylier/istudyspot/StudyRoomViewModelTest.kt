package com.example.scylier.istudyspot

import com.example.scylier.istudyspot.models.ApiResponse
import com.example.scylier.istudyspot.models.studyroom.SeatInfo
import com.example.scylier.istudyspot.models.studyroom.SeatLayoutData
import com.example.scylier.istudyspot.models.studyroom.SeatLayoutItemInfo
import com.example.scylier.istudyspot.models.studyroom.StudyRoomDetail
import com.example.scylier.istudyspot.models.studyroom.StudyRoomItem
import com.example.scylier.istudyspot.models.studyroom.StudyRoomListResponse
import com.example.scylier.istudyspot.repository.MainRepository
import com.example.scylier.istudyspot.viewmodel.StudyRoomViewModel
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
class StudyRoomViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val repository = mockk<MainRepository>()
    private lateinit var viewModel: StudyRoomViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = StudyRoomViewModel(repository)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadStudyRoomsSuccessShouldUpdateList() = runTest {
        coEvery { repository.getStudyRooms(keyword = "quiet") } returns ApiResponse.Success(
            200,
            "ok",
            StudyRoomListResponse(
                total = 1,
                list = listOf(StudyRoomItem(1L, "Quiet Room", "Library", "08:00", "22:00"))
            )
        )

        viewModel.loadStudyRooms("quiet")

        val state = viewModel.studyRoomState.value
        assertFalse(state.isLoading)
        assertEquals(1, state.studyRooms.size)
        assertEquals("Quiet Room", state.studyRooms.first().name)
        assertNull(state.error)
    }

    @Test
    fun loadStudyRoomsErrorShouldExposeError() = runTest {
        coEvery { repository.getStudyRooms(keyword = null) } returns ApiResponse.Error(500, "server error")

        viewModel.loadStudyRooms()

        val state = viewModel.studyRoomState.value
        assertFalse(state.isLoading)
        assertTrue(state.studyRooms.isEmpty())
        assertEquals("server error", state.error)
    }

    @Test
    fun loadSeatsShouldPreferLayoutData() = runTest {
        val seats = listOf(seat(11L, 2, 3), seat(12L, 2, 4))
        coEvery { repository.getStudyRoomSeatLayout(1L) } returns ApiResponse.Success(
            200,
            "ok",
            SeatLayoutData(
                studyRoomId = 1L,
                studyRoomName = "Room",
                rows = 6,
                cols = 8,
                layoutMode = "hybrid",
                seats = seats,
                items = listOf(SeatLayoutItemInfo(1L, 1L, seatId = 11L, itemType = "seat", rowNum = 2, colNum = 3))
            )
        )

        viewModel.loadSeats(1L)

        val state = viewModel.seatMapState.value
        assertFalse(state.isLoading)
        assertEquals("hybrid", state.layoutMode)
        assertEquals(6, state.rows)
        assertEquals(8, state.cols)
        assertEquals(seats, state.seats)
        assertEquals(1L, state.layout?.studyRoomId)
    }

    @Test
    fun loadSeatsShouldFallbackToGridWhenLayoutIsNull() = runTest {
        coEvery { repository.getStudyRoomSeatLayout(1L) } returns ApiResponse.Success(200, "ok", null)
        coEvery { repository.getStudyRoomSeats(1L) } returns ApiResponse.Success(
            200,
            "ok",
            listOf(seat(1L, 1, 2), seat(2L, 4, 5))
        )

        viewModel.loadSeats(1L)

        val state = viewModel.seatMapState.value
        assertFalse(state.isLoading)
        assertEquals("grid", state.layoutMode)
        assertEquals(4, state.rows)
        assertEquals(5, state.cols)
        assertNull(state.layout)
        assertNull(state.error)
    }

    @Test
    fun loadSeatsShouldKeepLayoutErrorWhenFallbackAlsoFails() = runTest {
        coEvery { repository.getStudyRoomSeatLayout(1L) } returns ApiResponse.Error(500, "layout failed")
        coEvery { repository.getStudyRoomSeats(1L) } returns ApiResponse.Error(500, "seats failed")

        viewModel.loadSeats(1L)

        val state = viewModel.seatMapState.value
        assertFalse(state.isLoading)
        assertEquals("layout failed", state.error)
    }

    @Test
    fun loadStudyRoomDetailShouldExposeDescriptionOnSuccess() = runTest {
        coEvery { repository.getStudyRoomDetail(1L) } returns ApiResponse.Success(
            200,
            "ok",
            StudyRoomDetail(1L, "Room", "Address", description = "Bright and quiet")
        )

        viewModel.loadStudyRoomDetail(1L)

        val state = viewModel.detailState.value
        assertFalse(state.isLoading)
        assertEquals("Bright and quiet", state.description)
        assertEquals(30, state.totalSeats)
    }

    @Test
    fun loadStudyRoomDetailShouldStopLoadingOnErrorOrException() = runTest {
        coEvery { repository.getStudyRoomDetail(1L) } returns ApiResponse.Error(404, "not found")
        viewModel.loadStudyRoomDetail(1L)
        assertFalse(viewModel.detailState.value.isLoading)

        coEvery { repository.getStudyRoomDetail(2L) } throws RuntimeException("boom")
        viewModel.loadStudyRoomDetail(2L)
        assertFalse(viewModel.detailState.value.isLoading)
    }

    private fun seat(id: Long, row: Int, col: Int) = SeatInfo(
        id = id,
        rowNum = row,
        colNum = col,
        status = "available",
        seatType = 1,
        pricePerHour = 10.0,
        seatNumber = "A$id"
    )
}
