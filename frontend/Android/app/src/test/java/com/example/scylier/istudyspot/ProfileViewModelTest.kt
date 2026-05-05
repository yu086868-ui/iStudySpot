package com.example.scylier.istudyspot

import com.example.scylier.istudyspot.viewmodel.ProfileViewModel
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ProfileViewModelTest {

    private lateinit var viewModel: ProfileViewModel

    @Before
    fun setup() {
        viewModel = ProfileViewModel()
    }

    @Test
    fun testViewModel_userInfo_name() {
        assertEquals("张三", viewModel.userInfo.name)
    }

    @Test
    fun testViewModel_userInfo_studentId() {
        assertEquals("2024001234", viewModel.userInfo.studentId)
    }

    @Test
    fun testViewModel_userInfo_points() {
        assertEquals(1280, viewModel.userInfo.points)
    }

    @Test
    fun testViewModel_userInfo_level() {
        assertEquals("LV5", viewModel.userInfo.level)
    }

    @Test
    fun testViewModel_userInfo_reservationCount() {
        assertEquals(48, viewModel.userInfo.reservationCount)
    }

    @Test
    fun testViewModel_userInfo_studyHours() {
        assertEquals("156h", viewModel.userInfo.studyHours)
    }

    @Test
    fun testViewModel_userInfo_violationCount() {
        assertEquals(0, viewModel.userInfo.violationCount)
    }

    @Test
    fun testViewModel_menuItems_size() {
        assertEquals(3, viewModel.menuItems.size)
    }

    @Test
    fun testViewModel_menuItems_firstItem() {
        val firstItem = viewModel.menuItems[0]
        assertEquals("消息中心", firstItem.label)
        assertEquals("3", firstItem.rightText)
    }

    @Test
    fun testViewModel_menuItems_secondItem() {
        val secondItem = viewModel.menuItems[1]
        assertEquals("我的钱包", secondItem.label)
        assertEquals("¥128.00", secondItem.rightText)
    }

    @Test
    fun testViewModel_menuItems_thirdItem() {
        val thirdItem = viewModel.menuItems[2]
        assertEquals("个人设置", thirdItem.label)
        assertNull(thirdItem.rightText)
    }
}
