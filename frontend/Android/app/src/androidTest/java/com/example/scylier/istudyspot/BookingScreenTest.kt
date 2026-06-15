package com.example.scylier.istudyspot

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.scylier.istudyspot.ui.screen.BookingScreen
import org.junit.Rule
import org.junit.Test

class BookingScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testBookingScreen_displaysTitle() {
        composeTestRule.setContent {
            BookingScreen(
                studyRoomName = "图书馆自习室",
                seatPosition = "1-1",
                pricePerHour = 10.0,
                onBook = { _, _, _ -> }
            )
        }

        composeTestRule.onNodeWithText("预约座位").assertExists()
    }

    @Test
    fun testBookingScreen_displaysStudyRoomName() {
        composeTestRule.setContent {
            BookingScreen(
                studyRoomName = "图书馆自习室",
                seatPosition = "1-1",
                pricePerHour = 10.0,
                onBook = { _, _, _ -> }
            )
        }

        composeTestRule.onNodeWithText("自习室: 图书馆自习室").assertExists()
    }

    @Test
    fun testBookingScreen_displaysSeatPosition() {
        composeTestRule.setContent {
            BookingScreen(
                studyRoomName = "图书馆自习室",
                seatPosition = "1-1",
                pricePerHour = 10.0,
                onBook = { _, _, _ -> }
            )
        }

        composeTestRule.onNodeWithText("座位: 1-1").assertExists()
    }

    @Test
    fun testBookingScreen_displaysPrice() {
        composeTestRule.setContent {
            BookingScreen(
                studyRoomName = "图书馆自习室",
                seatPosition = "1-1",
                pricePerHour = 10.0,
                onBook = { _, _, _ -> }
            )
        }

        composeTestRule.onNodeWithText("单价: ¥10.0/小时").assertExists()
    }

    @Test
    fun testBookingScreen_displaysStartTimeField() {
        composeTestRule.setContent {
            BookingScreen(
                studyRoomName = "图书馆自习室",
                seatPosition = "1-1",
                pricePerHour = 10.0,
                onBook = { _, _, _ -> }
            )
        }

        composeTestRule.onNodeWithText("开始时间 (如: 2024-01-01 09:00)").assertExists()
    }

    @Test
    fun testBookingScreen_displaysEndTimeField() {
        composeTestRule.setContent {
            BookingScreen(
                studyRoomName = "图书馆自习室",
                seatPosition = "1-1",
                pricePerHour = 10.0,
                onBook = { _, _, _ -> }
            )
        }

        composeTestRule.onNodeWithText("结束时间 (如: 2024-01-01 12:00)").assertExists()
    }

    @Test
    fun testBookingScreen_displaysBookingTypeField() {
        composeTestRule.setContent {
            BookingScreen(
                studyRoomName = "图书馆自习室",
                seatPosition = "1-1",
                pricePerHour = 10.0,
                onBook = { _, _, _ -> }
            )
        }

        composeTestRule.onNodeWithText("预订类型 (hour/half_day/day)").assertExists()
    }

    @Test
    fun testBookingScreen_displaysBookButton() {
        composeTestRule.setContent {
            BookingScreen(
                studyRoomName = "图书馆自习室",
                seatPosition = "1-1",
                pricePerHour = 10.0,
                onBook = { _, _, _ -> }
            )
        }

        composeTestRule.onNodeWithText("确认预约").assertExists()
    }

    @Test
    fun testBookingScreen_buttonDisabledWhenFieldsEmpty() {
        composeTestRule.setContent {
            BookingScreen(
                studyRoomName = "图书馆自习室",
                seatPosition = "1-1",
                pricePerHour = 10.0,
                onBook = { _, _, _ -> }
            )
        }

        composeTestRule.onNodeWithText("确认预约").assertIsNotEnabled()
    }

    @Test
    fun testBookingScreen_inputStartTime() {
        composeTestRule.setContent {
            BookingScreen(
                studyRoomName = "图书馆自习室",
                seatPosition = "1-1",
                pricePerHour = 10.0,
                onBook = { _, _, _ -> }
            )
        }

        composeTestRule.onNodeWithText("开始时间 (如: 2024-01-01 09:00)")
            .performTextInput("2024-01-01 09:00")
    }

    @Test
    fun testBookingScreen_inputEndTime() {
        composeTestRule.setContent {
            BookingScreen(
                studyRoomName = "图书馆自习室",
                seatPosition = "1-1",
                pricePerHour = 10.0,
                onBook = { _, _, _ -> }
            )
        }

        composeTestRule.onNodeWithText("结束时间 (如: 2024-01-01 12:00)")
            .performTextInput("2024-01-01 12:00")
    }

    @Test
    fun testBookingScreen_buttonEnabledWhenFieldsFilled() {
        composeTestRule.setContent {
            BookingScreen(
                studyRoomName = "图书馆自习室",
                seatPosition = "1-1",
                pricePerHour = 10.0,
                onBook = { _, _, _ -> }
            )
        }

        composeTestRule.onNodeWithText("开始时间 (如: 2024-01-01 09:00)")
            .performTextInput("2024-01-01 09:00")
        composeTestRule.onNodeWithText("结束时间 (如: 2024-01-01 12:00)")
            .performTextInput("2024-01-01 12:00")

        composeTestRule.onNodeWithText("确认预约").assertIsEnabled()
    }

    @Test
    fun testBookingScreen_clickBookButton_triggersCallback() {
        var bookClicked = false
        var capturedStartTime = ""
        var capturedEndTime = ""
        var capturedBookingType = ""

        composeTestRule.setContent {
            BookingScreen(
                studyRoomName = "图书馆自习室",
                seatPosition = "1-1",
                pricePerHour = 10.0,
                onBook = { startTime, endTime, bookingType ->
                    bookClicked = true
                    capturedStartTime = startTime
                    capturedEndTime = endTime
                    capturedBookingType = bookingType
                }
            )
        }

        composeTestRule.onNodeWithText("开始时间 (如: 2024-01-01 09:00)")
            .performTextInput("2024-01-01 09:00")
        composeTestRule.onNodeWithText("结束时间 (如: 2024-01-01 12:00)")
            .performTextInput("2024-01-01 12:00")
        composeTestRule.onNodeWithText("预订类型 (hour/half_day/day)")
            .performTextInput("hour")

        composeTestRule.onNodeWithText("确认预约").performClick()

        assert(bookClicked)
        assertEquals("2024-01-01 09:00", capturedStartTime)
        assertEquals("2024-01-01 12:00", capturedEndTime)
        assertEquals("hour", capturedBookingType)
    }
}
