package com.example.scylier.istudyspot

import com.example.scylier.istudyspot.viewmodel.GuideViewModel
import com.example.scylier.istudyspot.viewmodel.GuideUiState
import com.example.scylier.istudyspot.viewmodel.Facility
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
class GuideViewModelTest {

    private lateinit var viewModel: GuideViewModel
    private val testDispatcher = UnconfinedTestDispatcher()
    private val mockRepository = mockk<MainRepository>()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        coEvery { mockRepository.getStudyRooms(any(), any(), any(), any()) } returns ApiResponse.Error(500, "Test error")
        viewModel = GuideViewModel(mockRepository)
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
    fun testViewModel_initialState_emptyFacilities() {
        assertTrue(viewModel.state.value.facilities.isEmpty())
    }

    @Test
    fun testViewModel_loadGuideInfo_hasData() = runTest {
        viewModel.loadGuideInfo()
        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertTrue(state.facilities.isNotEmpty())
        assertTrue(state.location.isNotEmpty())
        assertTrue(state.openingHours.isNotEmpty())
        assertTrue(state.contact.isNotEmpty())
    }

    @Test
    fun testViewModel_facilities_size() = runTest {
        viewModel.loadGuideInfo()
        assertEquals(8, viewModel.state.value.facilities.size)
    }

    @Test
    fun testViewModel_facilities_firstItem() = runTest {
        viewModel.loadGuideInfo()
        val first = viewModel.state.value.facilities[0]
        assertEquals("静音区", first.name)
        assertTrue(first.description.contains("安静"))
    }

    @Test
    fun testViewModel_facilities_containsQuietZone() = runTest {
        viewModel.loadGuideInfo()
        val quietZone = viewModel.state.value.facilities.find { it.name == "静音区" }
        assertNotNull(quietZone)
        assertTrue(quietZone!!.description.contains("禁止交谈"))
    }

    @Test
    fun testViewModel_facilities_containsDiscussionZone() = runTest {
        viewModel.loadGuideInfo()
        val discussionZone = viewModel.state.value.facilities.find { it.name == "讨论区" }
        assertNotNull(discussionZone)
        assertTrue(discussionZone!!.description.contains("讨论"))
    }

    @Test
    fun testViewModel_facilities_containsRestArea() = runTest {
        viewModel.loadGuideInfo()
        val restArea = viewModel.state.value.facilities.find { it.name == "休息区" }
        assertNotNull(restArea)
        assertTrue(restArea!!.description.contains("休息"))
    }

    @Test
    fun testViewModel_facilities_containsPrintArea() = runTest {
        viewModel.loadGuideInfo()
        val printArea = viewModel.state.value.facilities.find { it.name == "打印区" }
        assertNotNull(printArea)
        assertTrue(printArea!!.description.contains("打印"))
    }

    @Test
    fun testViewModel_facilities_containsWiFi() = runTest {
        viewModel.loadGuideInfo()
        val wifi = viewModel.state.value.facilities.find { it.name == "WiFi覆盖" }
        assertNotNull(wifi)
        assertTrue(wifi!!.description.contains("WiFi"))
    }

    @Test
    fun testViewModel_facilities_containsAirConditioner() = runTest {
        viewModel.loadGuideInfo()
        val ac = viewModel.state.value.facilities.find { it.name == "空调系统" }
        assertNotNull(ac)
        assertTrue(ac!!.description.contains("空调"))
    }

    @Test
    fun testViewModel_location_notEmpty() = runTest {
        viewModel.loadGuideInfo()
        assertTrue(viewModel.state.value.location.isNotEmpty())
    }

    @Test
    fun testViewModel_openingHours_notEmpty() = runTest {
        viewModel.loadGuideInfo()
        assertTrue(viewModel.state.value.openingHours.isNotEmpty())
    }

    @Test
    fun testViewModel_contact_containsPhone() = runTest {
        viewModel.loadGuideInfo()
        assertTrue(viewModel.state.value.contact.contains("电话"))
    }

    @Test
    fun testViewModel_contact_containsEmail() = runTest {
        viewModel.loadGuideInfo()
        assertTrue(viewModel.state.value.contact.contains("邮箱"))
    }

    @Test
    fun testFacility_dataClass() {
        val facility = Facility("测试设施", "测试描述")
        assertEquals("测试设施", facility.name)
        assertEquals("测试描述", facility.description)
    }
}
