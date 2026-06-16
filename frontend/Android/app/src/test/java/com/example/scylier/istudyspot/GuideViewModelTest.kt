package com.example.scylier.istudyspot

import com.example.scylier.istudyspot.models.ApiResponse
import com.example.scylier.istudyspot.models.studyroom.StudyRoomGuideDetail
import com.example.scylier.istudyspot.models.studyroom.StudyRoomGuideSummary
import com.example.scylier.istudyspot.repository.MainRepository
import com.example.scylier.istudyspot.viewmodel.GuideViewModel
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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GuideViewModelTest {

    private lateinit var viewModel: GuideViewModel
    private val testDispatcher = UnconfinedTestDispatcher()
    private val mockRepository = mockk<MainRepository>()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        coEvery { mockRepository.getStudyRoomGuides() } returns ApiResponse.Success(
            200,
            "success",
            listOf(
                StudyRoomGuideSummary(
                    studyRoomId = 1L,
                    studyRoomName = "iStudySpot 学习空间（五道口店）",
                    address = "北京市海淀区五道口购物中心3F",
                    openTime = "08:00:00",
                    closeTime = "23:00:00",
                    description = "临近清华北大，考研党聚集地"
                )
            )
        )
        coEvery { mockRepository.getStudyRoomGuideDetail(1L) } returns ApiResponse.Success(
            200,
            "success",
            StudyRoomGuideDetail(
                studyRoomId = 1L,
                studyRoomName = "iStudySpot 学习空间（五道口店）",
                address = "北京市海淀区五道口购物中心3F",
                openTime = "08:00:00",
                closeTime = "23:00:00",
                description = "临近清华北大，考研党聚集地",
                contactInfo = "电话：010-62550101",
                learningAreas = "静音区：适合深度学习",
                convenienceFacilities = "WiFi、储物柜",
                transportationGuide = "地铁五道口站步行6分钟"
            )
        )
        viewModel = GuideViewModel(mockRepository)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testInitialState() {
        val state = viewModel.state.value
        assertTrue(state.isLoadingList)
        assertTrue(state.guides.isEmpty())
        assertNull(state.selectedGuide)
    }

    @Test
    fun testLoadGuideList() = runTest {
        viewModel.loadGuideList()
        val state = viewModel.state.value
        assertFalse(state.isLoadingList)
        assertEquals(1, state.guides.size)
        assertEquals("iStudySpot 学习空间（五道口店）", state.guides.first().studyRoomName)
        assertNull(state.errorMessage)
    }

    @Test
    fun testLoadGuideDetail() = runTest {
        viewModel.loadGuideDetail(1L)
        val detail = viewModel.state.value.selectedGuide
        assertNotNull(detail)
        assertEquals(1L, detail!!.studyRoomId)
        assertTrue(detail.contactInfo.contains("010-62550101"))
        assertFalse(viewModel.state.value.isLoadingDetail)
    }

    @Test
    fun testClearSelectedGuide() = runTest {
        viewModel.loadGuideDetail(1L)
        viewModel.clearSelectedGuide()
        assertNull(viewModel.state.value.selectedGuide)
        assertFalse(viewModel.state.value.isLoadingDetail)
    }
}
