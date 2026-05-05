package com.example.scylier.istudyspot

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.scylier.istudyspot.models.ai.AiMessage
import com.example.scylier.istudyspot.models.ai.MessageType
import com.example.scylier.istudyspot.ui.screen.AiChatScreen
import org.junit.Rule
import org.junit.Test

class AiChatScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testAiChatScreen_emptyState_displaysWelcomeMessage() {
        composeTestRule.setContent {
            AiChatScreen(
                messages = emptyList(),
                isLoading = false,
                onSendMessage = {},
                onBackClick = {}
            )
        }

        composeTestRule.onNodeWithText("AI咨询助手").assertExists()
        composeTestRule.onNodeWithText("我可以帮您解答关于自习室的各种问题").assertExists()
    }

    @Test
    fun testAiChatScreen_emptyState_displaysSuggestionChips() {
        composeTestRule.setContent {
            AiChatScreen(
                messages = emptyList(),
                isLoading = false,
                onSendMessage = {},
                onBackClick = {}
            )
        }

        composeTestRule.onNodeWithText("如何预约座位？").assertExists()
        composeTestRule.onNodeWithText("自习室开放时间？").assertExists()
        composeTestRule.onNodeWithText("取消预约规则").assertExists()
        composeTestRule.onNodeWithText("签到流程是什么？").assertExists()
    }

    @Test
    fun testAiChatScreen_displaysUserMessage() {
        val messages = listOf(
            AiMessage(
                id = "1",
                content = "如何预约座位？",
                type = MessageType.USER
            )
        )

        composeTestRule.setContent {
            AiChatScreen(
                messages = messages,
                isLoading = false,
                onSendMessage = {},
                onBackClick = {}
            )
        }

        composeTestRule.onNodeWithText("如何预约座位？").assertExists()
    }

    @Test
    fun testAiChatScreen_displaysAiMessage() {
        val messages = listOf(
            AiMessage(
                id = "1",
                content = "预约座位的流程如下：",
                type = MessageType.AI
            )
        )

        composeTestRule.setContent {
            AiChatScreen(
                messages = messages,
                isLoading = false,
                onSendMessage = {},
                onBackClick = {}
            )
        }

        composeTestRule.onNodeWithText("预约座位的流程如下：").assertExists()
    }

    @Test
    fun testAiChatScreen_displaysMultipleMessages() {
        val messages = listOf(
            AiMessage(
                id = "1",
                content = "如何预约座位？",
                type = MessageType.USER
            ),
            AiMessage(
                id = "2",
                content = "预约座位的流程如下：\n1. 打开APP，点击首页的\"预约座位\"",
                type = MessageType.AI
            )
        )

        composeTestRule.setContent {
            AiChatScreen(
                messages = messages,
                isLoading = false,
                onSendMessage = {},
                onBackClick = {}
            )
        }

        composeTestRule.onNodeWithText("如何预约座位？").assertExists()
        composeTestRule.onNodeWithText("预约座位的流程如下：").assertExists()
    }

    @Test
    fun testAiChatScreen_inputField_exists() {
        composeTestRule.setContent {
            AiChatScreen(
                messages = emptyList(),
                isLoading = false,
                onSendMessage = {},
                onBackClick = {}
            )
        }

        composeTestRule.onNodeWithText("输入您的问题...").assertExists()
    }

    @Test
    fun testAiChatScreen_sendButton_exists() {
        composeTestRule.setContent {
            AiChatScreen(
                messages = emptyList(),
                isLoading = false,
                onSendMessage = {},
                onBackClick = {}
            )
        }

        composeTestRule.onNodeWithContentDescription("发送").assertExists()
    }

    @Test
    fun testAiChatScreen_backButton_exists() {
        composeTestRule.setContent {
            AiChatScreen(
                messages = emptyList(),
                isLoading = false,
                onSendMessage = {},
                onBackClick = {}
            )
        }

        composeTestRule.onNodeWithContentDescription("返回").assertExists()
    }

    @Test
    fun testAiChatScreen_loadingState_sendButtonDisabled() {
        composeTestRule.setContent {
            AiChatScreen(
                messages = emptyList(),
                isLoading = true,
                onSendMessage = {},
                onBackClick = {}
            )
        }

        composeTestRule.onNodeWithContentDescription("发送").assertIsNotEnabled()
    }
}
