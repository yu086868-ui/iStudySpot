package com.example.scylier.istudyspot

import com.example.scylier.istudyspot.viewmodel.StudyRecordViewModel
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class StudyRecordViewModelTest {

    private lateinit var viewModel: StudyRecordViewModel

    @Before
    fun setup() {
        viewModel = StudyRecordViewModel()
    }

    @Test
    fun testViewModel_weekStudyHours() {
        assertEquals(24, viewModel.weekStudyHours)
    }

    @Test
    fun testViewModel_monthStudyHours() {
        assertEquals(96, viewModel.monthStudyHours)
    }

    @Test
    fun testViewModel_totalStudyHours() {
        assertEquals(328, viewModel.totalStudyHours)
    }

    @Test
    fun testViewModel_totalBookings() {
        assertEquals(45, viewModel.totalBookings)
    }

    @Test
    fun testViewModel_streakDays() {
        assertEquals(7, viewModel.streakDays)
    }

    @Test
    fun testViewModel_avgStudyDuration() {
        assertEquals(3.5, viewModel.avgStudyDuration, 0.01)
    }

    @Test
    fun testViewModel_favoriteSeat() {
        assertEquals("A区-12号", viewModel.favoriteSeat)
    }

    @Test
    fun testViewModel_peakTime() {
        assertEquals("14:00-17:00", viewModel.peakTime)
    }

    @Test
    fun testViewModel_weekStudyHours_positive() {
        assertTrue(viewModel.weekStudyHours > 0)
    }

    @Test
    fun testViewModel_monthStudyHours_positive() {
        assertTrue(viewModel.monthStudyHours > 0)
    }

    @Test
    fun testViewModel_totalStudyHours_positive() {
        assertTrue(viewModel.totalStudyHours > 0)
    }

    @Test
    fun testViewModel_totalBookings_positive() {
        assertTrue(viewModel.totalBookings > 0)
    }

    @Test
    fun testViewModel_streakDays_positive() {
        assertTrue(viewModel.streakDays > 0)
    }

    @Test
    fun testViewModel_avgStudyDuration_positive() {
        assertTrue(viewModel.avgStudyDuration > 0)
    }

    @Test
    fun testViewModel_favoriteSeat_notEmpty() {
        assertTrue(viewModel.favoriteSeat.isNotEmpty())
    }

    @Test
    fun testViewModel_peakTime_notEmpty() {
        assertTrue(viewModel.peakTime.isNotEmpty())
    }

    @Test
    fun testViewModel_monthStudyHours_greaterThanWeek() {
        assertTrue(viewModel.monthStudyHours > viewModel.weekStudyHours)
    }

    @Test
    fun testViewModel_totalStudyHours_greaterThanMonth() {
        assertTrue(viewModel.totalStudyHours > viewModel.monthStudyHours)
    }

    @Test
    fun testViewModel_favoriteSeat_containsArea() {
        assertTrue(viewModel.favoriteSeat.contains("区"))
    }

    @Test
    fun testViewModel_peakTime_containsTime() {
        assertTrue(viewModel.peakTime.contains(":"))
    }
}
