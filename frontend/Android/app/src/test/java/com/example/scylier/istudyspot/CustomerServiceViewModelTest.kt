package com.example.scylier.istudyspot

import com.example.scylier.istudyspot.models.ApiResponse
import com.example.scylier.istudyspot.repository.MainRepository
import com.example.scylier.istudyspot.viewmodel.CustomerServiceViewModel
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
class CustomerServiceViewModelTest {

    private lateinit var viewModel: CustomerServiceViewModel
    private val testDispatcher = UnconfinedTestDispatcher()
    private val mockRepository = mockk<MainRepository>()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = CustomerServiceViewModel(mockRepository)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testInitialState() {
        assertTrue(viewModel.messages.value.isEmpty())
        assertFalse(viewModel.isLoading.value)
        assertEquals("", viewModel.welcomeMessage.value)
        assertTrue(viewModel.recommendedQuestions.value.isEmpty())
    }

    @Test
    fun testLoadWelcome_success() = runTest {
        coEvery { mockRepository.getCustomerServiceWelcome() } returns
            ApiResponse.Success(200, "获取成功", mapOf(
                "welcomeMessage" to "您好！我是智能客服",
                "recommendedQuestions" to listOf("如何预约？", "开放时间？")
            ))

        viewModel.loadWelcome()

        assertEquals("您好！我是智能客服", viewModel.welcomeMessage.value)
        assertEquals(2, viewModel.recommendedQuestions.value.size)
        assertEquals("如何预约？", viewModel.recommendedQuestions.value[0])
        assertEquals("开放时间？", viewModel.recommendedQuestions.value[1])
    }

    @Test
    fun testLoadWelcome_error() = runTest {
        coEvery { mockRepository.getCustomerServiceWelcome() } returns
            ApiResponse.Error(500, "网络错误")

        viewModel.loadWelcome()

        assertEquals("", viewModel.welcomeMessage.value)
        assertTrue(viewModel.recommendedQuestions.value.isEmpty())
    }

    @Test
    fun testLoadWelcome_missingWelcomeMessage() = runTest {
        coEvery { mockRepository.getCustomerServiceWelcome() } returns
            ApiResponse.Success(200, "获取成功", mapOf(
                "recommendedQuestions" to listOf("问题1")
            ))

        viewModel.loadWelcome()

        assertEquals("", viewModel.welcomeMessage.value)
        assertEquals(1, viewModel.recommendedQuestions.value.size)
    }

    @Test
    fun testLoadWelcome_missingRecommendedQuestions() = runTest {
        coEvery { mockRepository.getCustomerServiceWelcome() } returns
            ApiResponse.Success(200, "获取成功", mapOf(
                "welcomeMessage" to "欢迎"
            ))

        viewModel.loadWelcome()

        assertEquals("欢迎", viewModel.welcomeMessage.value)
        assertTrue(viewModel.recommendedQuestions.value.isEmpty())
    }

    @Test
    fun testSendMessage_success() = runTest {
        coEvery { mockRepository.customerServiceChat(any(), "你好") } returns
            ApiResponse.Success(200, "success", mapOf("response" to "您好，请问有什么可以帮您？"))

        viewModel.sendMessage("你好")

        assertEquals(2, viewModel.messages.value.size)
        val userMsg = viewModel.messages.value[0]
        assertEquals("你好", userMsg.content)
        assertTrue(userMsg.isFromUser)
        val botMsg = viewModel.messages.value[1]
        assertEquals("您好，请问有什么可以帮您？", botMsg.content)
        assertFalse(botMsg.isFromUser)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun testSendMessage_error() = runTest {
        coEvery { mockRepository.customerServiceChat(any(), any()) } returns
            ApiResponse.Error(500, "网络错误")

        viewModel.sendMessage("你好")

        assertEquals(2, viewModel.messages.value.size)
        val userMsg = viewModel.messages.value[0]
        assertTrue(userMsg.isFromUser)
        val errorMsg = viewModel.messages.value[1]
        assertEquals("网络异常，请稍后重试", errorMsg.content)
        assertFalse(errorMsg.isFromUser)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun testSendMessage_blankText_ignored() = runTest {
        viewModel.sendMessage("")
        viewModel.sendMessage("   ")
        viewModel.sendMessage("\t\n")

        assertTrue(viewModel.messages.value.isEmpty())
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun testSendMessage_multipleMessages() = runTest {
        coEvery { mockRepository.customerServiceChat(any(), "第一条") } returns
            ApiResponse.Success(200, "success", mapOf("response" to "回复1"))
        coEvery { mockRepository.customerServiceChat(any(), "第二条") } returns
            ApiResponse.Success(200, "success", mapOf("response" to "回复2"))

        viewModel.sendMessage("第一条")
        viewModel.sendMessage("第二条")

        assertEquals(4, viewModel.messages.value.size)
        assertEquals("第一条", viewModel.messages.value[0].content)
        assertTrue(viewModel.messages.value[0].isFromUser)
        assertEquals("回复1", viewModel.messages.value[1].content)
        assertFalse(viewModel.messages.value[1].isFromUser)
        assertEquals("第二条", viewModel.messages.value[2].content)
        assertTrue(viewModel.messages.value[2].isFromUser)
        assertEquals("回复2", viewModel.messages.value[3].content)
        assertFalse(viewModel.messages.value[3].isFromUser)
    }

    @Test
    fun testSendMessage_missingResponseKey() = runTest {
        coEvery { mockRepository.customerServiceChat(any(), any()) } returns
            ApiResponse.Success(200, "success", mapOf("otherKey" to "value"))

        viewModel.sendMessage("你好")

        assertEquals(2, viewModel.messages.value.size)
        val botMsg = viewModel.messages.value[1]
        assertEquals("抱歉，我暂时无法回答这个问题", botMsg.content)
    }

    @Test
    fun testSendMessage_isLoadingDuringRequest() = runTest {
        coEvery { mockRepository.customerServiceChat(any(), any()) } returns
            ApiResponse.Success(200, "success", mapOf("response" to "回复"))

        viewModel.sendMessage("你好")

        assertFalse(viewModel.isLoading.value)
    }
}
