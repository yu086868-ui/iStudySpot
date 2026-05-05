package com.example.scylier.istudyspot

import com.example.scylier.istudyspot.viewmodel.HomeViewModel
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class HomeViewModelTest {

    private lateinit var viewModel: HomeViewModel

    @Before
    fun setup() {
        viewModel = HomeViewModel()
    }

    @Test
    fun testViewModel_mainFeatures_notEmpty() {
        assertTrue(viewModel.mainFeatures.isNotEmpty())
    }

    @Test
    fun testViewModel_mainFeatures_size() {
        assertEquals(8, viewModel.mainFeatures.size)
    }

    @Test
    fun testViewModel_firstFeature_id() {
        val first = viewModel.mainFeatures[0]
        assertEquals("booking", first.id)
    }

    @Test
    fun testViewModel_firstFeature_title() {
        val first = viewModel.mainFeatures[0]
        assertEquals("预约座位", first.title)
    }

    @Test
    fun testViewModel_secondFeature_id() {
        val second = viewModel.mainFeatures[1]
        assertEquals("checkin", second.id)
    }

    @Test
    fun testViewModel_secondFeature_title() {
        val second = viewModel.mainFeatures[1]
        assertEquals("签到", second.title)
    }

    @Test
    fun testViewModel_thirdFeature_id() {
        val third = viewModel.mainFeatures[2]
        assertEquals("guide", third.id)
    }

    @Test
    fun testViewModel_thirdFeature_title() {
        val third = viewModel.mainFeatures[2]
        assertEquals("场馆导览", third.title)
    }

    @Test
    fun testViewModel_fourthFeature_id() {
        val fourth = viewModel.mainFeatures[3]
        assertEquals("my_booking", fourth.id)
    }

    @Test
    fun testViewModel_fourthFeature_title() {
        val fourth = viewModel.mainFeatures[3]
        assertEquals("我的预约", fourth.title)
    }

    @Test
    fun testViewModel_fifthFeature_id() {
        val fifth = viewModel.mainFeatures[4]
        assertEquals("study_record", fifth.id)
    }

    @Test
    fun testViewModel_fifthFeature_title() {
        val fifth = viewModel.mainFeatures[4]
        assertEquals("学习记录", fifth.title)
    }

    @Test
    fun testViewModel_sixthFeature_id() {
        val sixth = viewModel.mainFeatures[5]
        assertEquals("team_booking", sixth.id)
    }

    @Test
    fun testViewModel_sixthFeature_title() {
        val sixth = viewModel.mainFeatures[5]
        assertEquals("团队预约", sixth.title)
    }

    @Test
    fun testViewModel_seventhFeature_id() {
        val seventh = viewModel.mainFeatures[6]
        assertEquals("notification", seventh.id)
    }

    @Test
    fun testViewModel_seventhFeature_title() {
        val seventh = viewModel.mainFeatures[6]
        assertEquals("通知提醒", seventh.title)
    }

    @Test
    fun testViewModel_eighthFeature_id() {
        val eighth = viewModel.mainFeatures[7]
        assertEquals("settings", eighth.id)
    }

    @Test
    fun testViewModel_eighthFeature_title() {
        val eighth = viewModel.mainFeatures[7]
        assertEquals("偏好设置", eighth.title)
    }

    @Test
    fun testViewModel_allFeaturesHaveIds() {
        viewModel.mainFeatures.forEach { feature ->
            assertTrue(feature.id.isNotEmpty())
        }
    }

    @Test
    fun testViewModel_allFeaturesHaveTitles() {
        viewModel.mainFeatures.forEach { feature ->
            assertTrue(feature.title.isNotEmpty())
        }
    }

    @Test
    fun testViewModel_featuresContainBooking() {
        val booking = viewModel.mainFeatures.find { it.id == "booking" }
        assertNotNull(booking)
    }

    @Test
    fun testViewModel_featuresContainCheckin() {
        val checkin = viewModel.mainFeatures.find { it.id == "checkin" }
        assertNotNull(checkin)
    }

    @Test
    fun testViewModel_featuresContainNotification() {
        val notification = viewModel.mainFeatures.find { it.id == "notification" }
        assertNotNull(notification)
    }

    @Test
    fun testViewModel_featuresContainSettings() {
        val settings = viewModel.mainFeatures.find { it.id == "settings" }
        assertNotNull(settings)
    }
}
