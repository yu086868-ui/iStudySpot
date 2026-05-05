package com.example.scylier.istudyspot

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.scylier.istudyspot.ui.screen.RegisterScreen
import org.junit.Rule
import org.junit.Test

class RegisterScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testRegisterScreen_displaysTitle() {
        composeTestRule.setContent {
            RegisterScreen(onRegister = { _, _, _, _ -> })
        }

        composeTestRule.onNodeWithText("注册账号").assertExists()
    }

    @Test
    fun testRegisterScreen_displaysUsernameField() {
        composeTestRule.setContent {
            RegisterScreen(onRegister = { _, _, _, _ -> })
        }

        composeTestRule.onNodeWithText("用户名").assertExists()
    }

    @Test
    fun testRegisterScreen_displaysNicknameField() {
        composeTestRule.setContent {
            RegisterScreen(onRegister = { _, _, _, _ -> })
        }

        composeTestRule.onNodeWithText("昵称").assertExists()
    }

    @Test
    fun testRegisterScreen_displaysPasswordField() {
        composeTestRule.setContent {
            RegisterScreen(onRegister = { _, _, _, _ -> })
        }

        composeTestRule.onNodeWithText("密码").assertExists()
    }

    @Test
    fun testRegisterScreen_displaysConfirmPasswordField() {
        composeTestRule.setContent {
            RegisterScreen(onRegister = { _, _, _, _ -> })
        }

        composeTestRule.onNodeWithText("确认密码").assertExists()
    }

    @Test
    fun testRegisterScreen_displaysRegisterButton() {
        composeTestRule.setContent {
            RegisterScreen(onRegister = { _, _, _, _ -> })
        }

        composeTestRule.onNodeWithText("注册").assertExists()
    }

    @Test
    fun testRegisterScreen_inputUsername() {
        composeTestRule.setContent {
            RegisterScreen(onRegister = { _, _, _, _ -> })
        }

        composeTestRule.onNodeWithText("用户名")
            .performTextInput("testuser")

        composeTestRule.onNodeWithText("testuser").assertExists()
    }

    @Test
    fun testRegisterScreen_inputNickname() {
        composeTestRule.setContent {
            RegisterScreen(onRegister = { _, _, _, _ -> })
        }

        composeTestRule.onNodeWithText("昵称")
            .performTextInput("测试昵称")
    }

    @Test
    fun testRegisterScreen_inputPassword() {
        composeTestRule.setContent {
            RegisterScreen(onRegister = { _, _, _, _ -> })
        }

        composeTestRule.onNodeWithText("密码")
            .performTextInput("password123")
    }

    @Test
    fun testRegisterScreen_inputConfirmPassword() {
        composeTestRule.setContent {
            RegisterScreen(onRegister = { _, _, _, _ -> })
        }

        composeTestRule.onNodeWithText("确认密码")
            .performTextInput("password123")
    }

    @Test
    fun testRegisterScreen_clickRegister_triggersCallback() {
        var registerClicked = false
        var capturedUsername = ""
        var capturedPassword = ""
        var capturedConfirmPassword = ""
        var capturedNickname = ""

        composeTestRule.setContent {
            RegisterScreen(
                onRegister = { username, password, confirmPassword, nickname ->
                    registerClicked = true
                    capturedUsername = username
                    capturedPassword = password
                    capturedConfirmPassword = confirmPassword
                    capturedNickname = nickname
                }
            )
        }

        composeTestRule.onNodeWithText("用户名").performTextInput("testuser")
        composeTestRule.onNodeWithText("昵称").performTextInput("测试用户")
        composeTestRule.onNodeWithText("密码").performTextInput("password123")
        composeTestRule.onNodeWithText("确认密码").performTextInput("password123")

        composeTestRule.onNodeWithText("注册").performClick()

        assert(registerClicked)
        assertEquals("testuser", capturedUsername)
        assertEquals("password123", capturedPassword)
        assertEquals("password123", capturedConfirmPassword)
        assertEquals("测试用户", capturedNickname)
    }
}
