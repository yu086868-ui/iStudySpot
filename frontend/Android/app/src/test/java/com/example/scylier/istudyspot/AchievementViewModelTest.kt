package com.example.scylier.istudyspot

import com.example.scylier.istudyspot.models.ApiResponse
import com.example.scylier.istudyspot.repository.MainRepository
import com.example.scylier.istudyspot.viewmodel.AchievementItem
import com.example.scylier.istudyspot.viewmodel.AchievementUiState
import com.example.scylier.istudyspot.viewmodel.AchievementViewModel
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
class AchievementViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val repository = mockk<MainRepository>()
    private lateinit var viewModel: AchievementViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = AchievementViewModel(repository)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadAchievementsSuccessShouldMapItemsAndCounts() = runTest {
        coEvery { repository.getAchievements() } returns ApiResponse.Success(
            200,
            "ok",
            listOf(
                mapOf("code" to "early", "name" to "Early", "description" to "Morning", "icon" to "sun", "category" to "study", "isUnlocked" to true, "unlockedAt" to "2026-06-01"),
                mapOf("code" to "night", "name" to "Night", "description" to "Evening", "icon" to "moon", "category" to "study", "isUnlocked" to false)
            )
        )

        viewModel.loadAchievements()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals(2, state.totalCount)
        assertEquals(1, state.unlockedCount)
        assertEquals("early", state.achievements.first().code)
        assertEquals("2026-06-01", state.achievements.first().unlockedAt)
    }

    @Test
    fun loadAchievementsErrorShouldExposeMessage() = runTest {
        coEvery { repository.getAchievements() } returns ApiResponse.Error(500, "failed")

        viewModel.loadAchievements()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals("failed", state.error)
        assertTrue(state.achievements.isEmpty())
    }

    @Test
    fun loadAchievementsExceptionShouldExposeReadableError() = runTest {
        coEvery { repository.getAchievements() } throws RuntimeException("boom")

        viewModel.loadAchievements()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertTrue(state.error.orEmpty().contains("boom"))
    }

    @Test
    fun uiStateAndItemShouldKeepProvidedValues() {
        val item = AchievementItem("code", "name", "desc", "icon", "cat", true, "time")
        val state = AchievementUiState(listOf(item), unlockedCount = 1, totalCount = 1, isLoading = false)

        assertEquals("code", state.achievements.single().code)
        assertEquals(1, state.unlockedCount)
        assertFalse(state.isLoading)
    }
}
