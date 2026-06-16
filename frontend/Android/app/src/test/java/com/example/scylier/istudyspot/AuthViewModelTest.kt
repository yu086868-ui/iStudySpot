package com.example.scylier.istudyspot

import com.example.scylier.istudyspot.infra.network.ApiClient
import com.example.scylier.istudyspot.models.ApiResponse
import com.example.scylier.istudyspot.models.auth.LoginResponse
import com.example.scylier.istudyspot.models.auth.RegisterResponse
import com.example.scylier.istudyspot.models.auth.UserInfo
import com.example.scylier.istudyspot.repository.MainRepository
import com.example.scylier.istudyspot.utils.ConfigManager
import com.example.scylier.istudyspot.viewmodel.AuthUiState
import com.example.scylier.istudyspot.viewmodel.AuthViewModel
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
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], manifest = Config.NONE)
class AuthViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val repository = mockk<MainRepository>()
    private lateinit var viewModel: AuthViewModel
    private lateinit var configManager: ConfigManager

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        configManager = ConfigManager.getInstance(RuntimeEnvironment.getApplication())
        configManager.clearAll()
        ApiClient.currentToken = null
        viewModel = AuthViewModel(repository)
    }

    @After
    fun teardown() {
        configManager.clearAll()
        ApiClient.currentToken = null
        Dispatchers.resetMain()
    }

    @Test
    fun loginShouldRejectEmptyInputs() {
        viewModel.login("", "pwd", configManager)
        assertEquals("用户名和密码不能为空", viewModel.loginState.value.error)
    }

    @Test
    fun loginSuccessShouldPersistTokens() = runTest {
        coEvery { repository.login("alice", "pwd") } returns ApiResponse.Success(
            200,
            "ok",
            LoginResponse("token-1", user = UserInfo(7L, "alice", "Alice"))
        )

        viewModel.login("alice", "pwd", configManager)

        val state = viewModel.loginState.value
        assertTrue(state.isSuccess)
        assertEquals("token-1", configManager.getToken())
        assertEquals("7", configManager.getUserId())
        assertEquals("alice", configManager.getUsername())
        assertEquals("Alice", configManager.getNickname())
        assertEquals("token-1", ApiClient.currentToken)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun loginErrorShouldExposeMessage() = runTest {
        coEvery { repository.login(any(), any()) } returns ApiResponse.Error(401, "invalid")

        viewModel.login("alice", "pwd", configManager)

        assertEquals("invalid", viewModel.loginState.value.error)
        assertFalse(viewModel.loginState.value.isSuccess)
    }

    @Test
    fun registerShouldValidateInputsAndPersistOnSuccess() = runTest {
        viewModel.register("", "1", "1", "Nick", configManager)
        assertEquals("请填写所有必填字段", viewModel.registerState.value.error)

        viewModel.register("alice", "1", "2", "Nick", configManager)
        assertEquals("两次密码输入不一致", viewModel.registerState.value.error)

        coEvery { repository.register("alice", "pwd", "Nick") } returns ApiResponse.Success(
            201,
            "ok",
            RegisterResponse(token = "token-2", user = UserInfo(8L, "alice", "Nick"))
        )

        viewModel.register("alice", "pwd", "pwd", "Nick", configManager)

        assertTrue(viewModel.registerState.value.isSuccess)
        assertEquals("token-2", configManager.getToken())
        assertEquals("8", configManager.getUserId())
        assertEquals("alice", configManager.getUsername())
        assertEquals("Nick", configManager.getNickname())
    }

    @Test
    fun registerErrorShouldExposeMessage() = runTest {
        coEvery { repository.register(any(), any(), any()) } returns ApiResponse.Error(500, "server error")

        viewModel.register("alice", "pwd", "pwd", "Nick", configManager)

        assertEquals("server error", viewModel.registerState.value.error)
        assertFalse(viewModel.registerState.value.isSuccess)
    }

    @Test
    fun resetStatesShouldClearState() {
        viewModel.login("", "pwd", configManager)
        viewModel.register("", "1", "1", "Nick", configManager)
        viewModel.resetRegisterState()
        viewModel.resetLoginState()

        assertEquals(AuthUiState(), viewModel.registerState.value)
        assertEquals(AuthUiState(), viewModel.loginState.value)
    }
}
