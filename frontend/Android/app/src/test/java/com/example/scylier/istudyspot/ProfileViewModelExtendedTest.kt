package com.example.scylier.istudyspot

import com.example.scylier.istudyspot.infra.network.ApiClient
import com.example.scylier.istudyspot.models.ApiResponse
import com.example.scylier.istudyspot.models.auth.UserInfo
import com.example.scylier.istudyspot.models.order.OrderItem
import com.example.scylier.istudyspot.models.order.OrderListResponse
import com.example.scylier.istudyspot.repository.MainRepository
import com.example.scylier.istudyspot.utils.ConfigManager
import com.example.scylier.istudyspot.viewmodel.ProfileViewModel
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
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
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], manifest = Config.NONE)
class ProfileViewModelExtendedTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val repository = mockk<MainRepository>()
    private lateinit var viewModel: ProfileViewModel
    private lateinit var configManager: ConfigManager

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = ProfileViewModel(repository)
        configManager = ConfigManager.getInstance(RuntimeEnvironment.getApplication())
        configManager.clearAll()
        ApiClient.currentToken = null
    }

    @After
    fun teardown() {
        configManager.clearAll()
        ApiClient.currentToken = null
        Dispatchers.resetMain()
    }

    @Test
    fun loadProfileWithoutTokenShouldStopLoading() {
        viewModel.loadProfile(configManager)
        assertFalse(viewModel.state.value.isLoading)
        assertFalse(viewModel.state.value.isLoggedIn)
    }

    @Test
    fun loadProfileWithEmptyBackendShouldUseFallbackStats() = runTest {
        configManager.saveToken("saved-token")
        configManager.saveUsername("saved-user")
        configManager.saveNickname("saved-nick")
        coEvery { repository.getUserInfo() } returns ApiResponse.Error(500, "failed")
        coEvery { repository.getCheckinRecords() } returns ApiResponse.Success(200, "ok", emptyMap())
        coEvery { repository.getUserOrders() } returns ApiResponse.Success(200, "ok", null)

        viewModel.loadProfile(configManager)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state.isLoggedIn)
        assertEquals("saved-user", state.username)
        assertEquals("saved-nick", state.nickname)
        assertEquals(328, state.totalStudyHours)
        assertEquals(45, state.totalBookings)
        assertFalse(state.isLoading)
    }

    @Test
    fun loadProfileWithDataShouldUseRemoteValues() = runTest {
        configManager.saveToken("saved-token")
        coEvery { repository.getUserInfo() } returns ApiResponse.Success(
            200,
            "ok",
            UserInfo(1L, "remote", "Remote", phone = "13800000000", email = "remote@example.com")
        )
        coEvery { repository.getCheckinRecords() } returns ApiResponse.Success(
            200,
            "ok",
            mapOf("totalHours" to 120, "streak" to 8)
        )
        coEvery { repository.getUserOrders() } returns ApiResponse.Success(
            200,
            "ok",
            OrderListResponse(
                total = 1,
                list = listOf(OrderItem(id = 1L, seatId = 1L, studyRoomName = "Room"))
            )
        )

        viewModel.loadProfile(configManager)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals("remote", state.username)
        assertEquals("Remote", state.nickname)
        assertEquals("13800000000", state.phone)
        assertEquals("remote@example.com", state.email)
        assertEquals(120, state.totalStudyHours)
        assertEquals(8, state.streakDays)
        assertEquals(1, state.totalBookings)
        assertEquals(4, state.studyLevel)
        assertTrue(state.levelProgress > 0f)
    }

    @Test
    fun logoutShouldClearStateAndToken() = runTest {
        configManager.saveToken("token")
        coEvery { repository.logout() } returns ApiResponse.Success(200, "ok", Unit)

        viewModel.logout(configManager)
        advanceUntilIdle()

        assertEquals("", viewModel.state.value.nickname)
        assertFalse(viewModel.state.value.isLoggedIn)
        assertNull(configManager.getToken())
        assertNull(ApiClient.currentToken)
    }

    @Test
    fun helperFunctionsShouldCoverBoundaries() {
        assertEquals(1, viewModel.calculateLevel(0))
        assertEquals(2, viewModel.calculateLevel(10))
        assertEquals(3, viewModel.calculateLevel(50))
        assertEquals("传奇", viewModel.getLevelTitle(99))
        assertEquals(1f, viewModel.calculateLevelProgress(2500, 8), 0.0f)
    }
}
