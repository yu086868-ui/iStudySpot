package com.example.scylier.istudyspot

import com.example.scylier.istudyspot.viewmodel.ProfileViewModel
import com.example.scylier.istudyspot.viewmodel.ProfileUiState
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
    fun testViewModel_initialState_isLoading() {
        assertTrue(viewModel.state.value.isLoading)
    }

    @Test
    fun testViewModel_initialState_notLoggedIn() {
        assertFalse(viewModel.state.value.isLoggedIn)
    }

    @Test
    fun testViewModel_initialState_defaults() {
        val state = viewModel.state.value
        assertEquals("未登录", state.username)
        assertEquals("", state.nickname)
        assertEquals("未设置", state.phone)
        assertEquals("未设置", state.email)
        assertNull(state.avatar)
        assertEquals(0, state.totalStudyHours)
        assertEquals(0, state.streakDays)
        assertEquals(0, state.totalBookings)
        assertEquals(1, state.studyLevel)
        assertEquals("新手", state.studyLevelTitle)
        assertEquals(0f, state.levelProgress, 0.01f)
    }

    @Test
    fun testUiState_dataClass() {
        val state = ProfileUiState(
            username = "testuser",
            nickname = "测试用户",
            phone = "13800138000",
            email = "test@example.com",
            avatar = "https://example.com/avatar.png",
            isLoggedIn = true,
            isLoading = false,
            totalStudyHours = 100,
            streakDays = 5,
            totalBookings = 20,
            studyLevel = 3,
            studyLevelTitle = "进阶",
            levelProgress = 0.5f
        )
        assertEquals("testuser", state.username)
        assertEquals("测试用户", state.nickname)
        assertEquals("13800138000", state.phone)
        assertEquals("test@example.com", state.email)
        assertEquals("https://example.com/avatar.png", state.avatar)
        assertTrue(state.isLoggedIn)
        assertFalse(state.isLoading)
        assertEquals(100, state.totalStudyHours)
        assertEquals(5, state.streakDays)
        assertEquals(20, state.totalBookings)
        assertEquals(3, state.studyLevel)
        assertEquals("进阶", state.studyLevelTitle)
        assertEquals(0.5f, state.levelProgress, 0.01f)
    }

    @Test
    fun testCalculateLevel() {
        val vm = ProfileViewModel()
        assertEquals(1, vm.calculateLevel(0))
        assertEquals(1, vm.calculateLevel(5))
        assertEquals(2, vm.calculateLevel(15))
        assertEquals(3, vm.calculateLevel(75))
        assertEquals(4, vm.calculateLevel(150))
        assertEquals(5, vm.calculateLevel(300))
        assertEquals(6, vm.calculateLevel(750))
        assertEquals(7, vm.calculateLevel(1500))
        assertEquals(8, vm.calculateLevel(2500))
    }

    @Test
    fun testGetLevelTitle() {
        val vm = ProfileViewModel()
        assertEquals("新手", vm.getLevelTitle(1))
        assertEquals("入门", vm.getLevelTitle(2))
        assertEquals("进阶", vm.getLevelTitle(3))
        assertEquals("熟练", vm.getLevelTitle(4))
        assertEquals("学习达人", vm.getLevelTitle(5))
        assertEquals("学霸", vm.getLevelTitle(6))
        assertEquals("学神", vm.getLevelTitle(7))
        assertEquals("传奇", vm.getLevelTitle(8))
    }

    @Test
    fun testCalculateLevelProgress() {
        val vm = ProfileViewModel()
        val progress = vm.calculateLevelProgress(25, 2)
        assertTrue(progress > 0f && progress <= 1f)
    }
}
