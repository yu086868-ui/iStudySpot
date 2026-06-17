package com.example.scylier.istudyspot

import com.example.scylier.istudyspot.models.ApiResponse
import com.example.scylier.istudyspot.repository.MainRepository
import com.example.scylier.istudyspot.viewmodel.ViolationItem
import com.example.scylier.istudyspot.viewmodel.ViolationUiState
import com.example.scylier.istudyspot.viewmodel.ViolationViewModel
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
class ViolationViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val repository = mockk<MainRepository>()
    private lateinit var viewModel: ViolationViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = ViolationViewModel(repository)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadViolationsSuccessShouldMapListAndTotal() = runTest {
        coEvery { repository.getViolations() } returns ApiResponse.Success(
            200,
            "ok",
            mapOf(
                "total" to 1,
                "list" to listOf(
                    mapOf(
                        "id" to 7,
                        "type" to "no_show",
                        "description" to "Missed check-in",
                        "relatedOrderId" to 12,
                        "status" to "active",
                        "createdAt" to "2026-06-01",
                        "appealReason" to "traffic",
                        "appealResult" to "pending"
                    )
                )
            )
        )

        viewModel.loadViolations()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals(1, state.totalCount)
        assertEquals(7L, state.violations.single().id)
        assertEquals(12L, state.violations.single().relatedOrderId)
    }

    @Test
    fun loadViolationsNullDataShouldShowEmptyDataError() = runTest {
        coEvery { repository.getViolations() } returns ApiResponse.Success(200, "ok", null)

        viewModel.loadViolations()

        assertFalse(viewModel.state.value.isLoading)
        assertTrue(viewModel.state.value.error.orEmpty().isNotBlank())
    }

    @Test
    fun loadViolationsErrorAndExceptionShouldExposeMessage() = runTest {
        coEvery { repository.getViolations() } returns ApiResponse.Error(500, "failed")
        viewModel.loadViolations()
        assertEquals("failed", viewModel.state.value.error)

        coEvery { repository.getViolations() } throws RuntimeException("boom")
        viewModel.loadViolations()
        assertTrue(viewModel.state.value.error.orEmpty().contains("boom"))
    }

    @Test
    fun submitAppealSuccessShouldReloadViolations() = runTest {
        coEvery { repository.appealViolation(7L, "reason") } returns ApiResponse.Success(200, "ok", Unit)
        coEvery { repository.getViolations() } returns ApiResponse.Success(
            200,
            "ok",
            mapOf("total" to 0, "list" to emptyList<Map<String, Any?>>())
        )

        viewModel.submitAppeal(7L, "reason")

        assertFalse(viewModel.state.value.isLoading)
        assertEquals(0, viewModel.state.value.totalCount)
    }

    @Test
    fun submitAppealErrorAndClearMessagesShouldUpdateState() = runTest {
        coEvery { repository.appealViolation(7L, "reason") } returns ApiResponse.Error(400, "rejected")

        viewModel.submitAppeal(7L, "reason")
        assertEquals("rejected", viewModel.state.value.error)

        viewModel.clearMessages()
        assertNull(viewModel.state.value.error)
        assertNull(viewModel.state.value.appealSuccess)
    }

    @Test
    fun violationItemShouldExposeLabelsForKnownAndUnknownValues() {
        val active = ViolationItem(1L, "no_show", "desc", null, "active", null, null, null)
        val unknown = ViolationItem(2L, "other", "desc", null, "custom", null, null, null)

        assertTrue(active.statusLabel.isNotBlank())
        assertTrue(active.typeLabel.isNotBlank())
        assertEquals("custom", unknown.statusLabel)
        assertTrue(unknown.typeLabel.isNotBlank())

        val state = ViolationUiState(listOf(active), totalCount = 1, isLoading = false, error = "err", appealSuccess = "ok")
        assertEquals(1, state.violations.size)
        assertEquals("ok", state.appealSuccess)
    }
}
