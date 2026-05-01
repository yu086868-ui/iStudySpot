package com.example.scylier.istudyspot

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.scylier.istudyspot.ui.screen.HomeScreen
import org.junit.Rule
import org.junit.Test

class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testHomeScreen_displaysTitle() {
        composeTestRule.setContent {
            HomeScreen(onAction = {})
        }

        composeTestRule.onNodeWithText("iStudySpot").assertExists()
        composeTestRule.onNodeWithText("智慧自习室管理平台").assertExists()
    }

    @Test
    fun testHomeScreen_displaysBookingMenuItem() {
        composeTestRule.setContent {
            HomeScreen(onAction = {})
        }

        composeTestRule.onNodeWithText("预约座位").assertExists()
    }

    @Test
    fun testHomeScreen_displaysCheckinMenuItem() {
        composeTestRule.setContent {
            HomeScreen(onAction = {})
        }

        composeTestRule.onNodeWithText("签到").assertExists()
    }

    @Test
    fun testHomeScreen_displaysGuideMenuItem() {
        composeTestRule.setContent {
            HomeScreen(onAction = {})
        }

        composeTestRule.onNodeWithText("场馆导览").assertExists()
    }

    @Test
    fun testHomeScreen_displaysMyBookingMenuItem() {
        composeTestRule.setContent {
            HomeScreen(onAction = {})
        }

        composeTestRule.onNodeWithText("我的预约").assertExists()
    }

    @Test
    fun testHomeScreen_displaysStudyRecordMenuItem() {
        composeTestRule.setContent {
            HomeScreen(onAction = {})
        }

        composeTestRule.onNodeWithText("学习记录").assertExists()
    }

    @Test
    fun testHomeScreen_displaysAiChatMenuItem() {
        composeTestRule.setContent {
            HomeScreen(onAction = {})
        }

        composeTestRule.onNodeWithText("AI咨询").assertExists()
    }

    @Test
    fun testHomeScreen_displaysNotificationMenuItem() {
        composeTestRule.setContent {
            HomeScreen(onAction = {})
        }

        composeTestRule.onNodeWithText("通知提醒").assertExists()
    }

    @Test
    fun testHomeScreen_displaysSettingsMenuItem() {
        composeTestRule.setContent {
            HomeScreen(onAction = {})
        }

        composeTestRule.onNodeWithText("偏好设置").assertExists()
    }

    @Test
    fun testHomeScreen_clickBooking_triggersAction() {
        var clickedAction: String? = null

        composeTestRule.setContent {
            HomeScreen(onAction = { action -> clickedAction = action })
        }

        composeTestRule.onNodeWithText("预约座位").performClick()

        assert(clickedAction == "booking")
    }

    @Test
    fun testHomeScreen_clickCheckin_triggersAction() {
        var clickedAction: String? = null

        composeTestRule.setContent {
            HomeScreen(onAction = { action -> clickedAction = action })
        }

        composeTestRule.onNodeWithText("签到").performClick()

        assert(clickedAction == "checkin")
    }

    @Test
    fun testHomeScreen_clickAiChat_triggersAction() {
        var clickedAction: String? = null

        composeTestRule.setContent {
            HomeScreen(onAction = { action -> clickedAction = action })
        }

        composeTestRule.onNodeWithText("AI咨询").performClick()

        assert(clickedAction == "ai_chat")
    }

    @Test
    fun testHomeScreen_clickMyBooking_triggersAction() {
        var clickedAction: String? = null

        composeTestRule.setContent {
            HomeScreen(onAction = { action -> clickedAction = action })
        }

        composeTestRule.onNodeWithText("我的预约").performClick()

        assert(clickedAction == "my_booking")
    }

    @Test
    fun testHomeScreen_clickNotification_triggersAction() {
        var clickedAction: String? = null

        composeTestRule.setContent {
            HomeScreen(onAction = { action -> clickedAction = action })
        }

        composeTestRule.onNodeWithText("通知提醒").performClick()

        assert(clickedAction == "notification")
    }

    @Test
    fun testHomeScreen_clickSettings_triggersAction() {
        var clickedAction: String? = null

        composeTestRule.setContent {
            HomeScreen(onAction = { action -> clickedAction = action })
        }

        composeTestRule.onNodeWithText("偏好设置").performClick()

        assert(clickedAction == "settings")
    }
}
