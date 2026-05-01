package com.example.scylier.istudyspot

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.scylier.istudyspot.ui.screen.MoreScreen
import org.junit.Rule
import org.junit.Test

class MoreScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testMoreScreen_displaysTitle() {
        composeTestRule.setContent {
            MoreScreen(onAction = {})
        }

        composeTestRule.onNodeWithText("更多功能").assertExists()
    }

    @Test
    fun testMoreScreen_displaysGroupHeaders() {
        composeTestRule.setContent {
            MoreScreen(onAction = {})
        }

        composeTestRule.onNodeWithText("预约管理").assertExists()
        composeTestRule.onNodeWithText("学习数据").assertExists()
        composeTestRule.onNodeWithText("积分中心").assertExists()
        composeTestRule.onNodeWithText("其他").assertExists()
    }

    @Test
    fun testMoreScreen_displaysBookingRecordItem() {
        composeTestRule.setContent {
            MoreScreen(onAction = {})
        }

        composeTestRule.onNodeWithText("预约记录").assertExists()
    }

    @Test
    fun testMoreScreen_displaysViolationRecordItem() {
        composeTestRule.setContent {
            MoreScreen(onAction = {})
        }

        composeTestRule.onNodeWithText("违规记录").assertExists()
    }

    @Test
    fun testMoreScreen_displaysStudyStatisticsItem() {
        composeTestRule.setContent {
            MoreScreen(onAction = {})
        }

        composeTestRule.onNodeWithText("学习统计").assertExists()
    }

    @Test
    fun testMoreScreen_displaysAchievementItem() {
        composeTestRule.setContent {
            MoreScreen(onAction = {})
        }

        composeTestRule.onNodeWithText("成就徽章").assertExists()
    }

    @Test
    fun testMoreScreen_dispointsPointsItems() {
        composeTestRule.setContent {
            MoreScreen(onAction = {})
        }

        composeTestRule.onNodeWithText("积分兑换").assertExists()
        composeTestRule.onNodeWithText("积分明细").assertExists()
    }

    @Test
    fun testMoreScreen_displaysHelpItem() {
        composeTestRule.setContent {
            MoreScreen(onAction = {})
        }

        composeTestRule.onNodeWithText("帮助中心").assertExists()
    }

    @Test
    fun testMoreScreen_displaysFeedbackItem() {
        composeTestRule.setContent {
            MoreScreen(onAction = {})
        }

        composeTestRule.onNodeWithText("意见反馈").assertExists()
    }

    @Test
    fun testMoreScreen_displaysAboutItem() {
        composeTestRule.setContent {
            MoreScreen(onAction = {})
        }

        composeTestRule.onNodeWithText("关于我们").assertExists()
    }

    @Test
    fun testMoreScreen_displaysLogoutItem() {
        composeTestRule.setContent {
            MoreScreen(onAction = {})
        }

        composeTestRule.onNodeWithText("退出登录").assertExists()
    }

    @Test
    fun testMoreScreen_clickBookingRecord_triggersAction() {
        var clickedAction: String? = null

        composeTestRule.setContent {
            MoreScreen(onAction = { action -> clickedAction = action })
        }

        composeTestRule.onNodeWithText("预约记录").performClick()

        assert(clickedAction == "预约记录")
    }

    @Test
    fun testMoreScreen_clickHelpCenter_triggersAction() {
        var clickedAction: String? = null

        composeTestRule.setContent {
            MoreScreen(onAction = { action -> clickedAction = action })
        }

        composeTestRule.onNodeWithText("帮助中心").performClick()

        assert(clickedAction == "帮助中心")
    }

    @Test
    fun testMoreScreen_clickLogout_triggersAction() {
        var clickedAction: String? = null

        composeTestRule.setContent {
            MoreScreen(onAction = { action -> clickedAction = action })
        }

        composeTestRule.onNodeWithText("退出登录").performClick()

        assert(clickedAction == "退出登录")
    }

    @Test
    fun testMoreScreen_clickFeedback_triggersAction() {
        var clickedAction: String? = null

        composeTestRule.setContent {
            MoreScreen(onAction = { action -> clickedAction = action })
        }

        composeTestRule.onNodeWithText("意见反馈").performClick()

        assert(clickedAction == "意见反馈")
    }

    @Test
    fun testMoreScreen_clickStudyStatistics_triggersAction() {
        var clickedAction: String? = null

        composeTestRule.setContent {
            MoreScreen(onAction = { action -> clickedAction = action })
        }

        composeTestRule.onNodeWithText("学习统计").performClick()

        assert(clickedAction == "学习统计")
    }
}
