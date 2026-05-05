package com.example.scylier.istudyspot

import com.example.scylier.istudyspot.models.ai.MessageType
import com.example.scylier.istudyspot.viewmodel.AiChatViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AiChatViewModelComprehensiveTest {

    private lateinit var viewModel: AiChatViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = AiChatViewModel()
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testViewModel_initialState_emptyMessages() {
        assertEquals(0, viewModel.messages.size)
    }

    @Test
    fun testViewModel_initialState_notLoading() = runTest {
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun testViewModel_sendMessage_addsUserMessage() = runTest {
        viewModel.sendMessage("如何预约座位？")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(2, viewModel.messages.size)
        assertEquals(MessageType.USER, viewModel.messages[0].type)
        assertEquals("如何预约座位？", viewModel.messages[0].content)
    }

    @Test
    fun testViewModel_sendMessage_addsAiResponse() = runTest {
        viewModel.sendMessage("如何预约座位？")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(2, viewModel.messages.size)
        assertEquals(MessageType.AI, viewModel.messages[1].type)
        assertTrue(viewModel.messages[1].content.isNotEmpty())
    }

    @Test
    fun testViewModel_sendMessage_bookingQuestion_returnsBookingAnswer() = runTest {
        viewModel.sendMessage("我想预约座位")
        testDispatcher.scheduler.advanceUntilIdle()

        val aiMessage = viewModel.messages.last()
        assertTrue(aiMessage.content.contains("预约"))
    }

    @Test
    fun testViewModel_sendMessage_checkinQuestion_returnsCheckinAnswer() = runTest {
        viewModel.sendMessage("如何签到？")
        testDispatcher.scheduler.advanceUntilIdle()

        val aiMessage = viewModel.messages.last()
        assertTrue(aiMessage.content.contains("签到"))
    }

    @Test
    fun testViewModel_sendMessage_timeQuestion_returnsTimeAnswer() = runTest {
        viewModel.sendMessage("开放时间是什么？")
        testDispatcher.scheduler.advanceUntilIdle()

        val aiMessage = viewModel.messages.last()
        assertTrue(aiMessage.content.contains("开放时间") || aiMessage.content.contains("07:00"))
    }

    @Test
    fun testViewModel_sendMessage_cancelQuestion_returnsCancelAnswer() = runTest {
        viewModel.sendMessage("如何取消预约？")
        testDispatcher.scheduler.advanceUntilIdle()

        val aiMessage = viewModel.messages.last()
        assertTrue(aiMessage.content.contains("取消"))
    }

    @Test
    fun testViewModel_sendMessage_priceQuestion_returnsPriceAnswer() = runTest {
        viewModel.sendMessage("座位多少钱？")
        testDispatcher.scheduler.advanceUntilIdle()

        val aiMessage = viewModel.messages.last()
        assertTrue(aiMessage.content.contains("价格") || aiMessage.content.contains("元"))
    }

    @Test
    fun testViewModel_sendMessage_rulesQuestion_returnsRulesAnswer() = runTest {
        viewModel.sendMessage("有什么规则？")
        testDispatcher.scheduler.advanceUntilIdle()

        val aiMessage = viewModel.messages.last()
        assertTrue(aiMessage.content.contains("规则"))
    }

    @Test
    fun testViewModel_sendMessage_unknownQuestion_returnsDefaultAnswer() = runTest {
        viewModel.sendMessage("今天天气怎么样？")
        testDispatcher.scheduler.advanceUntilIdle()

        val aiMessage = viewModel.messages.last()
        assertTrue(aiMessage.content.isNotEmpty())
    }

    @Test
    fun testViewModel_sendMultipleMessages_maintainsOrder() = runTest {
        viewModel.sendMessage("问题1")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.sendMessage("问题2")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(4, viewModel.messages.size)
        assertEquals("问题1", viewModel.messages[0].content)
        assertEquals(MessageType.USER, viewModel.messages[0].type)
        assertEquals("问题2", viewModel.messages[2].content)
        assertEquals(MessageType.USER, viewModel.messages[2].type)
    }

    @Test
    fun testViewModel_clearMessages_emptiesList() = runTest {
        viewModel.sendMessage("测试消息")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.clearMessages()

        assertEquals(0, viewModel.messages.size)
    }

    @Test
    fun testViewModel_sendMessage_setsLoadingState() = runTest {
        viewModel.sendMessage("测试消息")
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun testViewModel_messageHasUniqueId() = runTest {
        viewModel.sendMessage("测试消息")
        testDispatcher.scheduler.advanceUntilIdle()

        val userMessage = viewModel.messages[0]
        val aiMessage = viewModel.messages[1]

        assertNotEquals(userMessage.id, aiMessage.id)
        assertTrue(userMessage.id.isNotEmpty())
        assertTrue(aiMessage.id.isNotEmpty())
    }

    @Test
    fun testViewModel_messageHasTimestamp() = runTest {
        val beforeTime = System.currentTimeMillis()
        viewModel.sendMessage("测试消息")
        testDispatcher.scheduler.advanceUntilIdle()
        val afterTime = System.currentTimeMillis()

        val userMessage = viewModel.messages[0]
        assertTrue(userMessage.timestamp >= beforeTime)
        assertTrue(userMessage.timestamp <= afterTime + 1000)
    }

    @Test
    fun testViewModel_multipleMessages_differentTimestamps() = runTest {
        viewModel.sendMessage("消息1")
        testDispatcher.scheduler.advanceUntilIdle()
        Thread.sleep(10)
        viewModel.sendMessage("消息2")
        testDispatcher.scheduler.advanceUntilIdle()

        val timestamp1 = viewModel.messages[0].timestamp
        val timestamp2 = viewModel.messages[2].timestamp

        assertTrue(timestamp2 >= timestamp1)
    }

    @Test
    fun testViewModel_sendEmptyMessage_doesNotCrash() = runTest {
        viewModel.sendMessage("")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(2, viewModel.messages.size)
    }

    @Test
    fun testViewModel_sendLongMessage_handlesCorrectly() = runTest {
        val longMessage = "这是一个很长的消息".repeat(100)
        viewModel.sendMessage(longMessage)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(2, viewModel.messages.size)
        assertEquals(longMessage, viewModel.messages[0].content)
    }

    @Test
    fun testViewModel_sendMessageWithSpecialCharacters_handlesCorrectly() = runTest {
        val specialMessage = "测试特殊字符: @#$%^&*()_+-=[]{}|;':\",./<>?"
        viewModel.sendMessage(specialMessage)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(2, viewModel.messages.size)
        assertEquals(specialMessage, viewModel.messages[0].content)
    }

    @Test
    fun testViewModel_sendMessageWithNewlines_handlesCorrectly() = runTest {
        val multilineMessage = "第一行\n第二行\n第三行"
        viewModel.sendMessage(multilineMessage)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(2, viewModel.messages.size)
        assertEquals(multilineMessage, viewModel.messages[0].content)
    }

    @Test
    fun testViewModel_aiResponseContainsRelevantKeywords() = runTest {
        viewModel.sendMessage("预约座位")
        testDispatcher.scheduler.advanceUntilIdle()

        val aiMessage = viewModel.messages.last()
        val relevantKeywords = listOf("预约", "座位", "流程", "步骤")
        val containsRelevantKeyword = relevantKeywords.any { aiMessage.content.contains(it) }
        assertTrue(containsRelevantKeyword)
    }

    @Test
    fun testViewModel_consecutiveMessages() = runTest {
        repeat(5) { index ->
            viewModel.sendMessage("消息 $index")
            testDispatcher.scheduler.advanceUntilIdle()
        }

        assertEquals(10, viewModel.messages.size)
    }
}
