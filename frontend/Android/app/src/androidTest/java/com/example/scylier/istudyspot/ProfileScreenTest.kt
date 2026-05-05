package com.example.scylier.istudyspot

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.scylier.istudyspot.ui.screen.ProfileScreen
import org.junit.Rule
import org.junit.Test

class ProfileScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testProfileScreen_displaysAvatar() {
        composeTestRule.setContent {
            ProfileScreen(
                onAvatarClick = {},
                onOrderListClick = {}
            )
        }

        composeTestRule.onNodeWithContentDescription("头像").assertExists()
    }

    @Test
    fun testProfileScreen_displaysDefaultUsername() {
        composeTestRule.setContent {
            ProfileScreen(
                onAvatarClick = {},
                onOrderListClick = {}
            )
        }

        composeTestRule.onNodeWithText("未登录").assertExists()
    }

    @Test
    fun testProfileScreen_displaysClickToLogin() {
        composeTestRule.setContent {
            ProfileScreen(
                onAvatarClick = {},
                onOrderListClick = {}
            )
        }

        composeTestRule.onNodeWithText("点击登录").assertExists()
    }

    @Test
    fun testProfileScreen_displaysPhoneLabel() {
        composeTestRule.setContent {
            ProfileScreen(
                onAvatarClick = {},
                onOrderListClick = {}
            )
        }

        composeTestRule.onNodeWithText("手机号: 未设置").assertExists()
    }

    @Test
    fun testProfileScreen_displaysEmailLabel() {
        composeTestRule.setContent {
            ProfileScreen(
                onAvatarClick = {},
                onOrderListClick = {}
            )
        }

        composeTestRule.onNodeWithText("邮箱: 未设置").assertExists()
    }

    @Test
    fun testProfileScreen_displaysMyOrders() {
        composeTestRule.setContent {
            ProfileScreen(
                onAvatarClick = {},
                onOrderListClick = {}
            )
        }

        composeTestRule.onNodeWithText("我的订单").assertExists()
    }

    @Test
    fun testProfileScreen_clickAvatar_triggersCallback() {
        var avatarClicked = false

        composeTestRule.setContent {
            ProfileScreen(
                onAvatarClick = { avatarClicked = true },
                onOrderListClick = {}
            )
        }

        composeTestRule.onNodeWithContentDescription("头像").performClick()

        assert(avatarClicked)
    }

    @Test
    fun testProfileScreen_clickMyOrders_triggersCallback() {
        var orderListClicked = false

        composeTestRule.setContent {
            ProfileScreen(
                onAvatarClick = {},
                onOrderListClick = { orderListClicked = true }
            )
        }

        composeTestRule.onNodeWithText("我的订单").performClick()

        assert(orderListClicked)
    }
}
