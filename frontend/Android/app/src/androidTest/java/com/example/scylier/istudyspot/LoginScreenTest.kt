package com.example.scylier.istudyspot

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.scylier.istudyspot.ui.screen.LoginScreen
import org.junit.Rule
import org.junit.Test

class LoginScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testLoginScreen_displaysTitle() {
        composeTestRule.setContent {
            LoginScreen(
                onLogin = { _, _ -> },
                onRegisterClick = {}
            )
        }

        composeTestRule.onNodeWithText("登录").assertExists()
    }

    @Test
    fun testLoginScreen_displaysUsernameField() {
        composeTestRule.setContent {
            LoginScreen(
                onLogin = { _, _ -> },
                onRegisterClick = {}
            )
        }

        composeTestRule.onNodeWithText("用户名").assertExists()
    }

    @Test
    fun testLoginScreen_displaysPasswordField() {
        composeTestRule.setContent {
            LoginScreen(
                onLogin = { _, _ -> },
                onRegisterClick = {}
            )
        }

        composeTestRule.onNodeWithText("密码").assertExists()
    }

    @Test
    fun testLoginScreen_displaysLoginButton() {
        composeTestRule.setContent {
            LoginScreen(
                onLogin = { _, _ -> },
                onRegisterClick = {}
            )
        }

        composeTestRule.onNodeWithText("登录").assertExists()
    }

    @Test
    fun testLoginScreen_displaysRegisterLink() {
        composeTestRule.setContent {
            LoginScreen(
                onLogin = { _, _ -> },
                onRegisterClick = {}
            )
        }

        composeTestRule.onNodeWithText("没有账号？立即注册").assertExists()
    }

    @Test
    fun testLoginScreen_inputUsername() {
        composeTestRule.setContent {
            LoginScreen(
                onLogin = { _, _ -> },
                onRegisterClick = {}
            )
        }

        composeTestRule.onNodeWithText("用户名")
            .performTextInput("testuser")

        composeTestRule.onNodeWithText("testuser").assertExists()
    }

    @Test
    fun testLoginScreen_inputPassword() {
        composeTestRule.setContent {
            LoginScreen(
                onLogin = { _, _ -> },
                onRegisterClick = {}
            )
        }

        composeTestRule.onNodeWithText("密码")
            .performTextInput("password123")
    }

    @Test
    fun testLoginScreen_clickRegister_triggersCallback() {
        var registerClicked = false

        composeTestRule.setContent {
            LoginScreen(
                onLogin = { _, _ -> },
                onRegisterClick = { registerClicked = true }
            )
        }

        composeTestRule.onNodeWithText("没有账号？立即注册").performClick()

        assert(registerClicked)
    }
}
