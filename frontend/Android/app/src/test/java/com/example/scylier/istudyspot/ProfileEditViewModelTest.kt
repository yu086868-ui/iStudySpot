package com.example.scylier.istudyspot

import com.example.scylier.istudyspot.models.ApiResponse
import com.example.scylier.istudyspot.models.auth.UserInfo
import com.example.scylier.istudyspot.repository.MainRepository
import com.example.scylier.istudyspot.viewmodel.ProfileEditViewModel
import com.example.scylier.istudyspot.viewmodel.ProfileEditUiState
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
class ProfileEditViewModelTest {

    private lateinit var viewModel: ProfileEditViewModel
    private val testDispatcher = UnconfinedTestDispatcher()
    private val mockRepository = mockk<MainRepository>()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = ProfileEditViewModel(mockRepository)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testInitialState() {
        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals("", state.nickname)
        assertEquals("", state.phone)
        assertEquals("", state.email)
        assertNull(state.error)
        assertNull(state.success)
    }

    @Test
    fun testLoadCurrentInfo() {
        viewModel.loadCurrentInfo("测试昵称", "13800138000", "test@example.com")
        val state = viewModel.state.value
        assertEquals("测试昵称", state.nickname)
        assertEquals("13800138000", state.phone)
        assertEquals("test@example.com", state.email)
    }

    @Test
    fun testLoadCurrentInfo_overwritesPrevious() {
        viewModel.loadCurrentInfo("昵称1", "111", "a@b.com")
        viewModel.loadCurrentInfo("昵称2", "222", "c@d.com")
        val state = viewModel.state.value
        assertEquals("昵称2", state.nickname)
        assertEquals("222", state.phone)
        assertEquals("c@d.com", state.email)
    }

    @Test
    fun testUpdateUserInfo_success() = runTest {
        coEvery { mockRepository.updateUserInfo("新昵称", null, "13900139000", "new@example.com") } returns
            ApiResponse.Success(200, "更新成功", UserInfo(1L, "test", "新昵称", phone = "13900139000", email = "new@example.com"))

        viewModel.updateUserInfo("新昵称", "13900139000", "new@example.com")

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals("资料更新成功", state.success)
        assertNull(state.error)
    }

    @Test
    fun testUpdateUserInfo_error() = runTest {
        coEvery { mockRepository.updateUserInfo(any(), any(), any(), any()) } returns
            ApiResponse.Error(500, "服务器错误")

        viewModel.updateUserInfo("昵称", "13900139000", "new@example.com")

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNull(state.success)
        assertEquals("服务器错误", state.error)
    }

    @Test
    fun testUpdateUserInfo_setsLoadingDuringRequest() = runTest {
        coEvery { mockRepository.updateUserInfo(any(), any(), any(), any()) } returns
            ApiResponse.Success(200, "更新成功", UserInfo(1L, "test", "新昵称"))

        viewModel.updateUserInfo("新昵称", "13900139000", "new@example.com")

        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun testChangePassword_success() = runTest {
        coEvery { mockRepository.changePassword("oldPass", "newPass") } returns
            ApiResponse.Success(200, "密码修改成功", Unit)

        viewModel.changePassword("oldPass", "newPass")

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals("密码修改成功", state.success)
        assertNull(state.error)
    }

    @Test
    fun testChangePassword_error() = runTest {
        coEvery { mockRepository.changePassword(any(), any()) } returns
            ApiResponse.Error(400, "旧密码不正确")

        viewModel.changePassword("wrongOld", "newPass")

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNull(state.success)
        assertEquals("旧密码不正确", state.error)
    }

    @Test
    fun testChangePassword_setsLoadingDuringRequest() = runTest {
        coEvery { mockRepository.changePassword(any(), any()) } returns
            ApiResponse.Success(200, "密码修改成功", Unit)

        viewModel.changePassword("oldPass", "newPass")

        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun testClearMessages() {
        viewModel.loadCurrentInfo("昵称", "13900139000", "test@example.com")
        coEvery { mockRepository.updateUserInfo(any(), any(), any(), any()) } returns
            ApiResponse.Success(200, "更新成功", UserInfo(1L, "test", "昵称"))

        viewModel.clearMessages()
        val state = viewModel.state.value
        assertNull(state.error)
        assertNull(state.success)
    }

    @Test
    fun testClearMessages_afterError() = runTest {
        coEvery { mockRepository.changePassword(any(), any()) } returns
            ApiResponse.Error(400, "旧密码不正确")

        viewModel.changePassword("wrong", "new")
        assertNotNull(viewModel.state.value.error)

        viewModel.clearMessages()
        assertNull(viewModel.state.value.error)
        assertNull(viewModel.state.value.success)
    }

    @Test
    fun testUiState_dataClass() {
        val state = ProfileEditUiState(
            isLoading = true,
            nickname = "昵称",
            phone = "13800138000",
            email = "test@example.com",
            error = "错误",
            success = "成功"
        )
        assertTrue(state.isLoading)
        assertEquals("昵称", state.nickname)
        assertEquals("13800138000", state.phone)
        assertEquals("test@example.com", state.email)
        assertEquals("错误", state.error)
        assertEquals("成功", state.success)
    }

    @Test
    fun testUiState_defaultValues() {
        val state = ProfileEditUiState()
        assertFalse(state.isLoading)
        assertEquals("", state.nickname)
        assertEquals("", state.phone)
        assertEquals("", state.email)
        assertNull(state.error)
        assertNull(state.success)
    }

    @Test
    fun testUpdateUserInfo_clearsPreviousMessages() = runTest {
        coEvery { mockRepository.updateUserInfo(any(), any(), any(), any()) } returns
            ApiResponse.Error(500, "第一次错误")
        viewModel.updateUserInfo("昵称", "13900139000", "test@example.com")
        assertEquals("第一次错误", viewModel.state.value.error)

        coEvery { mockRepository.updateUserInfo(any(), any(), any(), any()) } returns
            ApiResponse.Success(200, "更新成功", UserInfo(1L, "test", "昵称"))
        viewModel.updateUserInfo("昵称", "13900139000", "test@example.com")

        assertNull(viewModel.state.value.error)
        assertEquals("资料更新成功", viewModel.state.value.success)
    }

    @Test
    fun testChangePassword_clearsPreviousMessages() = runTest {
        coEvery { mockRepository.changePassword(any(), any()) } returns
            ApiResponse.Success(200, "密码修改成功", Unit)
        viewModel.changePassword("old", "new")
        assertEquals("密码修改成功", viewModel.state.value.success)

        coEvery { mockRepository.changePassword(any(), any()) } returns
            ApiResponse.Error(400, "密码错误")
        viewModel.changePassword("old", "new")

        assertNull(viewModel.state.value.success)
        assertEquals("密码错误", viewModel.state.value.error)
    }
}
