package com.example.scylier.istudyspot

import com.example.scylier.istudyspot.viewmodel.GuideViewModel
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class GuideViewModelTest {

    private lateinit var viewModel: GuideViewModel

    @Before
    fun setup() {
        viewModel = GuideViewModel()
    }

    @Test
    fun testViewModel_facilities_notEmpty() {
        assertTrue(viewModel.facilities.isNotEmpty())
    }

    @Test
    fun testViewModel_facilities_size() {
        assertEquals(8, viewModel.facilities.size)
    }

    @Test
    fun testViewModel_facilities_firstItem() {
        val first = viewModel.facilities[0]
        assertEquals("静音区", first.name)
        assertTrue(first.description.contains("安静"))
    }

    @Test
    fun testViewModel_facilities_containsQuietZone() {
        val quietZone = viewModel.facilities.find { it.name == "静音区" }
        assertNotNull(quietZone)
        assertTrue(quietZone!!.description.contains("禁止交谈"))
    }

    @Test
    fun testViewModel_facilities_containsDiscussionZone() {
        val discussionZone = viewModel.facilities.find { it.name == "讨论区" }
        assertNotNull(discussionZone)
        assertTrue(discussionZone!!.description.contains("讨论"))
    }

    @Test
    fun testViewModel_facilities_containsRestArea() {
        val restArea = viewModel.facilities.find { it.name == "休息区" }
        assertNotNull(restArea)
        assertTrue(restArea!!.description.contains("休息"))
    }

    @Test
    fun testViewModel_facilities_containsPrintArea() {
        val printArea = viewModel.facilities.find { it.name == "打印区" }
        assertNotNull(printArea)
        assertTrue(printArea!!.description.contains("打印"))
    }

    @Test
    fun testViewModel_facilities_containsLocker() {
        val locker = viewModel.facilities.find { it.name == "储物柜" }
        assertNotNull(locker)
        assertTrue(locker!!.description.contains("储物"))
    }

    @Test
    fun testViewModel_facilities_containsWaterDispenser() {
        val water = viewModel.facilities.find { it.name == "饮水机" }
        assertNotNull(water)
        assertTrue(water!!.description.contains("饮用水"))
    }

    @Test
    fun testViewModel_facilities_containsWiFi() {
        val wifi = viewModel.facilities.find { it.name == "WiFi覆盖" }
        assertNotNull(wifi)
        assertTrue(wifi!!.description.contains("WiFi"))
    }

    @Test
    fun testViewModel_facilities_containsAirConditioner() {
        val ac = viewModel.facilities.find { it.name == "空调系统" }
        assertNotNull(ac)
        assertTrue(ac!!.description.contains("空调"))
    }

    @Test
    fun testViewModel_location_notEmpty() {
        assertTrue(viewModel.location.isNotEmpty())
    }

    @Test
    fun testViewModel_location_containsAddress() {
        assertTrue(viewModel.location.contains("XX市"))
    }

    @Test
    fun testViewModel_openingHours_notEmpty() {
        assertTrue(viewModel.openingHours.isNotEmpty())
    }

    @Test
    fun testViewModel_openingHours_containsTime() {
        assertTrue(viewModel.openingHours.contains("08:00"))
        assertTrue(viewModel.openingHours.contains("23:00"))
    }

    @Test
    fun testViewModel_openingHours_containsWeekdays() {
        assertTrue(viewModel.openingHours.contains("周一至周日"))
    }

    @Test
    fun testViewModel_contact_notEmpty() {
        assertTrue(viewModel.contact.isNotEmpty())
    }

    @Test
    fun testViewModel_contact_containsPhone() {
        assertTrue(viewModel.contact.contains("电话"))
    }

    @Test
    fun testViewModel_contact_containsEmail() {
        assertTrue(viewModel.contact.contains("邮箱"))
    }

    @Test
    fun testFacility_dataClass() {
        val facility = GuideViewModel.Facility("测试设施", "测试描述")
        assertEquals("测试设施", facility.name)
        assertEquals("测试描述", facility.description)
    }
}
